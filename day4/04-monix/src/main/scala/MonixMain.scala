package com.example

import model.data.TablePlant
import model.{BatchAggregate, BatchModulo, DataType, Env, Nothing, RawFile, Tree}
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

  // First implementation, only for table1: By hand
  def runSync(): TablePlant = {
    import model.RuntimeExecutors._
    val result1: Env = t1RawFile.executeSync()
    val result12: Env = t1Batch12Modulo.executeSync(result1)
    val result13: Env = t1Batch13Modulo.executeSync(result1)
    val dataForAggregation: Env = EnvDataForAggregation(Seq(result12, result13))
    val result14: Env = t1Batch14Aggregate.executeSync(dataForAggregation)
    result14 match { case EnvDataModulo(Some(tablePlant)) => tablePlant }
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
    tResult14.map { case EnvDataModulo(Some(tablePlant)) => tablePlant }
  }
  def runAsyncFromTree(tree: Tree[DataType]): Task[TablePlant] = {
    ???
  }
  def main(args: Array[String]): Unit = {
    val treeTable1 = Tree[DataType](t1RawFile)
      .addChildren(Seq(
        Tree[DataType](t1Batch12Modulo)
          .addChild(Tree[DataType](t1Batch14Aggregate)),
        Tree[DataType](t1Batch13Modulo)
          .addChild(Tree[DataType](t1Batch14Aggregate))
      ))

    val batch25: Tree[DataType] = Tree(BatchAggregate(batchName = "Batch25"))
    val treeTable2 =
      Tree[DataType](RawFile("table2.csv"))
        .addChildren(Seq(
          Tree[DataType](BatchModulo(batchName = "Batch22", modulo = 3, offset = 0))
            .addChild(batch25),
          Tree[DataType](BatchModulo(batchName = "Batch23", modulo = 3, offset = 1))
            .addChild(batch25),
          Tree[DataType](BatchModulo(batchName = "Batch24", modulo = 3, offset = 2))
            .addChild(batch25)
        ))
    val tree = Tree[DataType](Nothing)
      .addChildren(Seq(
        treeTable1,
        treeTable2)
      )
    val resultSync = runSync()

    import monix.execution.Scheduler.Implicits.global
    val resultAsync = Await.result(runAsync().runToFuture, Duration("5 seconds"))
    assert(resultSync == resultAsync)
    println("ok between sync and async")
    val resultAsyncFromTree = Await.result(runAsyncFromTree(tree).runToFuture, Duration("5 seconds"))
    assert(resultSync == resultAsyncFromTree)
    println("ok between assync and asyncFromTree")
  }
}
