package com.example

import model.DataType.RawFile
import model.RuntimeExecutors.EnvDataModulo
import model.TreeRuntime.createTaskFromSync
import model.data.TablePlant
import model.{BatchAggregate, BatchModulo, DataType, Env, Tree, Vertice}
import monix.eval.Task

import scala.concurrent.Await
import scala.concurrent.duration.Duration

/**
 *                (Table1)                  (Table2)
 *                  |                       |
 *               RawFile1                   RawFile2____
 *               /       \                /       |     \
 *              Batch12 Batch13.        Batch22 Batch23 Batch24
 *                  \.    /                  \    |     /
 *                   Batch14                    Batch25
 *
 * Vertices at the same level will be executed in //
 * Vertices parent / child link is executed in serial
 *
 * RawFile1, RawFile2: read csv table 1, table2
 * Batch12, Batch13: apply Modulo2, Modulo3:
 * Batch14, Batch25: reduce by applying  (pct_rate_of_growth +1) * (pct_rate_of_growth + 1)
 */
object MonixMain {
  val t1RawFile: RawFile = RawFile("table1.csv")
  val t1Batch12Modulo: BatchModulo = BatchModulo(batchName = "Batch12", modulo = 2, offset = 0)
  val t1Batch13Modulo: BatchModulo = BatchModulo(batchName = "Batch13", modulo = 2, offset = 1)
  val t1Batch14Aggregate: BatchAggregate = BatchAggregate(batchName = "Batch14")

  private def env2TablePlant(env: Env): TablePlant = {
    env match { case EnvDataModulo(Some(tablePlant)) => tablePlant }
  }

  // First implementation, only for table1: By hand
  def runSync(): TablePlant = {
    import model.RuntimeExecutors._
    val result1: Env = t1RawFile.executeSync()
    val result12: Env = t1Batch12Modulo.executeSync(result1)
    val result13: Env = t1Batch13Modulo.executeSync(result1)
    val dataForAggregation: Env = EnvDataForAggregation(Seq(result12, result13))
    val result14: Env = t1Batch14Aggregate.executeSync(dataForAggregation)
    env2TablePlant(result14)
  }

  // 2nd implementation, with Task
  def runAsync(): Task[TablePlant] = {
    import model.RuntimeExecutors._
    val tResult1: Task[Env] = t1RawFile.executeAsync()
    val tResult12: Task[Env] = tResult1.flatMap(result1 => t1Batch12Modulo.executeAsync(result1))
    val tResult13: Task[Env] = tResult1.flatMap(result1 => t1Batch13Modulo.executeAsync(result1))
    val tDataForAggregation: Task[Env] = Task.parSequence(Seq(
      tResult12,
      tResult13
    ))
      .map(envs => EnvDataForAggregation(envs))
    val tResult14: Task[Env] = tDataForAggregation.flatMap(dataForAggregation => t1Batch14Aggregate.executeAsync(dataForAggregation))
    tResult14.map(env2TablePlant)
  }
  def runAsyncFromTreeForAllTables(tree: Tree[DataType]): Task[Seq[TablePlant]] = {
    // we consider having only one syncPoint per table here.
    val syncPointPerTable = tree.findVerticeWithSeveralParent
    println(syncPointPerTable)
    val tasks: Seq[Task[Env]] = syncPointPerTable.map(createTaskFromSync(tree))
    Task.sequence(tasks.map(task => task.map(env2TablePlant)))
  }

  def main(args: Array[String]): Unit = {
    val t2Batch14Aggregate = BatchAggregate(batchName = "Batch25")

    val tree: Tree[DataType] = Tree.root[DataType](id = "root", data = DataType.Nothing)
      .addChild(fromId = "root", id = "t1RawFile",  data = t1RawFile)
      .addChild(fromId = "t1RawFile", id = "batch12", data = t1Batch12Modulo)
      .addChild(fromId = "t1RawFile", id = "batch13", data = t1Batch13Modulo)
      .addChild(fromId = "batch12", id = "batch14", data= t1Batch14Aggregate)
      .addChild(fromId = "batch13", id = "batch14", data= t1Batch14Aggregate)

      .addChild(fromId = "root", id = "t2RawFile",  data = RawFile("table2.csv"))
      .addChild(fromId = "t2RawFile", id = "batch22", data = BatchModulo(batchName = "Batch22", modulo = 3, offset = 0))
      .addChild(fromId = "t2RawFile", id = "batch23", data = BatchModulo(batchName = "Batch22", modulo = 3, offset = 1))
      .addChild(fromId = "t2RawFile", id = "batch24", data = BatchModulo(batchName = "Batch22", modulo = 3, offset = 2))
      .addChild(fromId = "batch22", id = "batch25", data= t2Batch14Aggregate)
      .addChild(fromId = "batch23", id = "batch25", data= t2Batch14Aggregate)
      .addChild(fromId = "batch24", id = "batch25", data= t2Batch14Aggregate)
    val resultSync = runSync()

    import monix.execution.Scheduler.Implicits.global
    val resultAsync = Await.result(runAsync().runToFuture, Duration("5 seconds"))
    assert(resultSync == resultAsync)
    println("ok between sync and async")

    val resultAsyncFromTree = Await.result(runAsyncFromTreeForAllTables(tree).runToFuture, Duration("5 seconds"))
    assert(resultAsyncFromTree.contains(resultSync))
    println("ok between assync and asyncFromTree")
  }
}
