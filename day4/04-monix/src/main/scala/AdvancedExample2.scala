import controlflow.model.DataType.RawFile
import controlflow.model.RuntimeExecutors.EnvDataModulo
import controlflow.model.data.TablePlant
import controlflow.model._
import monix.eval.Task
import monix.reactive.Observable

import scala.annotation.nowarn
import scala.concurrent.Await
import scala.concurrent.duration.Duration

/**
 *                (TableA)                  (TableB)
 *                  |                       |
 *               RawFileA________          RawFileB____
 *               /       \       \        /       |     \
 *            BatchA1 BatchA2 BatchA3  BatchB1 BatchB2 BatchB3
 *                  \     /  \ /           \    |     /
 *                 BatchA12  BatchA23        BatchB4
 *                        \ /
 *                     BatchA123
 *
 * Vertices at the same level will be executed in //
 * Vertices parent / child link is executed in serial
 *
 * RawFile1, RawFile2: read csv table 1, table2
 * Batch12, Batch13: apply Modulo2
 * Batch22, Batch23, Batch24: apply Modulo3
 * Batch14, Batch25: reduce by applying  (pct_rate_of_growth +1) * (pct_rate_of_growth + 1)
 */
object AdvancedExample2 {
  val t1RawFile: RawFile = RawFile("table1.csv")
  val t1Batch1Modulo: BatchModulo = BatchModulo(batchName = "BatchA1", modulo = 5, offset = 0)
  val t1Batch2Modulo: BatchModulo = BatchModulo(batchName = "BatchA2", modulo = 5, offset = 1, delaySec = Some(1))
  val t1Batch3Modulo: BatchModulo = BatchModulo(batchName = "BatchA3", modulo = 5, offset = 2, delaySec = Some(2))
  val t1Batch12Aggregate: BatchAggregate = BatchAggregate(batchName = "BatchA12")
  val t1Batch23Aggregate: BatchAggregate = BatchAggregate(batchName = "BatchA23")
  val t1Batch123Aggregate: BatchAggregate = BatchAggregate(batchName = "BatchA123")

  val t2RawFile: RawFile = RawFile("table2.csv")
  val t2BatchB1Modulo: BatchModulo = BatchModulo(batchName = "BatchB1", modulo = 3, offset = 0)
  val t2BatchB2Modulo: BatchModulo = BatchModulo(batchName = "BatchB2", modulo = 3, offset = 1)
  val t2BatchB3Modulo: BatchModulo = BatchModulo(batchName = "BatchB3", modulo = 3, offset = 2)
  val t2BatchB4Aggregate: BatchAggregate = BatchAggregate(batchName = "BatchB4")

  private def env2TablePlant(env: Env): TablePlant = {
    env match { case EnvDataModulo(Some(tablePlant)) => tablePlant }
  }

  // First implementation, only for table1: By hand
  def runSync(): TablePlant = {
    import controlflow.model.RuntimeExecutors._
    val result1: Env = t1RawFile.executeSync()
    val resultA1: Env = t1Batch1Modulo.executeSync(result1)
    val resultA2: Env = t1Batch2Modulo.executeSync(result1)
    val resultA3: Env = t1Batch3Modulo.executeSync(result1)
    val dataForAggregation12: Env = EnvDataForAggregation(Seq(resultA1, resultA2))
    val dataForAggregation23: Env = EnvDataForAggregation(Seq(resultA2, resultA3))
    val result12: Env = t1Batch12Aggregate.executeSync(dataForAggregation12)
    val result13: Env = t1Batch23Aggregate.executeSync(dataForAggregation23)
    val dataForAggregation123: Env = EnvDataForAggregation(Seq(result12, result13))
    val result123: Env = t1Batch123Aggregate.executeSync(dataForAggregation123)
    env2TablePlant(result123)
  }

  // 2nd implementation, with Task
  def runAsync(): Task[TablePlant] = {
    import controlflow.model.RuntimeExecutors._
    val tResult: Task[Env] = t1RawFile.executeAsync()
    val tResult1: Task[Env] = tResult.flatMap(result1 => t1Batch1Modulo.executeAsync(result1))
    val tResult2: Task[Env] = tResult.flatMap(result1 => t1Batch2Modulo.executeAsync(result1))
    val tResult3: Task[Env] = tResult.flatMap(result1 => t1Batch3Modulo.executeAsync(result1))
    val tDataForAggregation12: Task[Env] = Task.parSequence(Seq(
      tResult1,
      tResult2
    ))
      .map(envs => EnvDataForAggregation(envs))
    val tResult12: Task[Env] = tDataForAggregation12.flatMap(dataForAggregation => t1Batch12Aggregate.executeAsync(dataForAggregation))
    val tDataForAggregation23: Task[Env] = Task.parSequence(Seq(
      tResult2,
      tResult3
    ))
      .map(envs => EnvDataForAggregation(envs))
    val tResult23: Task[Env] = tDataForAggregation23.flatMap(dataForAggregation => t1Batch23Aggregate.executeAsync(dataForAggregation))
    val tDataForAggregation123: Task[Env] = Task.parSequence(Seq(
      tResult12,
      tResult23
    ))
      .map(envs => EnvDataForAggregation(envs))
    val tResult123: Task[Env] = tDataForAggregation123.flatMap(dataForAggregation => t1Batch123Aggregate.executeAsync(dataForAggregation))
    tResult123.map(env2TablePlant)
  }

  def checkTreeTraverse(tree: Tree[DataType]): Unit = {
    val seq = tree.traverse(tree.findRoot).map(_.id).toSeq
    val expected = Seq(
      "root",
      "t1RawFile",
      "batchA1",
      "batchA2",
      "batchA12",
      "batchA3",
      "batchA23",
      "batchA1223",
      "t2RawFile",
      "batchB1",
      "batchB2",
      "batchB3",
      "batchB4"
    )
    assert(seq == expected)
  }
  // a step further with Observable: onlyOnce here
  def runAsyncReactive(): Task[TablePlant] = {
    import controlflow.model.RuntimeExecutors._
    val oResultA: Observable[Env] = t1RawFile.observable()

    // flatMap when only one parent dependendcy
    val oResult1: Observable[Env] = oResultA.flatMap(result1 => t1Batch1Modulo.observable(result1))
    val oResult2: Observable[Env] = oResultA.flatMap(result1 => t1Batch2Modulo.observable(result1))
    val oResult3: Observable[Env] = oResultA.flatMap(result1 => t1Batch3Modulo.observable(result1))
    // zip when there are at least two parent dependencies
    val oDataForAggregation12: Observable[Env] = for ((r1, r2) <- oResult1.zip(oResult2)) yield EnvDataForAggregation(Seq(r1, r2))
    val oDataForAggregation23: Observable[Env] = for ((r2, r3) <- oResult2.zip(oResult3)) yield EnvDataForAggregation(Seq(r2, r3))
    val oResult12: Observable[Env] = oDataForAggregation12.flatMap(tDataForAggregation => t1Batch12Aggregate.observable(tDataForAggregation))
    val oResult23: Observable[Env] = oDataForAggregation23.flatMap(tDataForAggregation => t1Batch23Aggregate.observable(tDataForAggregation))
    val oDataForAggregation1223: Observable[Env] = for ((r12, r23) <- oResult12.zip(oResult23)) yield EnvDataForAggregation(Seq(r12, r23))
    val oResult1223: Observable[Env] = oDataForAggregation1223.flatMap(tDataForAggregation => t1Batch123Aggregate.observable(tDataForAggregation))
    oResult1223.firstL.map(env2TablePlant)
  }
  def runAsyncFromTreeForAllTables(tree: Tree[DataType]): Task[Seq[TablePlant]] = {
    val tasks: Seq[Task[Env]] = TreeRuntime.schedule(tree).map(_.firstL)
    Task.sequence(tasks.map(task => task.map(env2TablePlant)))
  }

  def run(@nowarn args: Array[String]): Unit = {
    val t2Batch4Aggregate = BatchAggregate(batchName = "BatchB4")

    val tree: Tree[DataType] = Tree.root[DataType](id = "root", data = DataType.Nothing)
      .addChild(fromId = "root", id = "t1RawFile",  data = t1RawFile)
      .addChild(fromId = "t1RawFile", id = "batchA1", data = t1Batch1Modulo)
      .addChild(fromId = "t1RawFile", id = "batchA2", data = t1Batch2Modulo)
      .addChild(fromId = "t1RawFile", id = "batchA3", data = t1Batch3Modulo)
      .addChild(fromId = "batchA1", id = "batchA12", data= t1Batch12Aggregate)
      .addChild(fromId = "batchA2", id = "batchA12", data= t1Batch12Aggregate)
      .addChild(fromId = "batchA2", id = "batchA23", data= t1Batch23Aggregate)
      .addChild(fromId = "batchA3", id = "batchA23", data= t1Batch23Aggregate)
      .addChild(fromId = "batchA12", id = "batchA1223", data= t1Batch123Aggregate)
      .addChild(fromId = "batchA23", id = "batchA1223", data= t1Batch123Aggregate)

      .addChild(fromId = "root", id = "t2RawFile",  data = t2RawFile)
      .addChild(fromId = "t2RawFile", id = "batchB1", data = t2BatchB1Modulo)
      .addChild(fromId = "t2RawFile", id = "batchB2", data = t2BatchB2Modulo)
      .addChild(fromId = "t2RawFile", id = "batchB3", data = t2BatchB3Modulo)
      .addChild(fromId = "batchB1", id = "batchB4", data= t2Batch4Aggregate)
      .addChild(fromId = "batchB2", id = "batchB4", data= t2Batch4Aggregate)
      .addChild(fromId = "batchB3", id = "batchB4", data= t2Batch4Aggregate)
    println("begin run sync")
    val resultSync = runSync()
    println("end run sync")
    println()

    println("begin run async")
    import monix.execution.Scheduler.Implicits.global
    val resultAsync = Await.result(runAsync().runToFuture, Duration("5 seconds"))
    println("end run async")
    println()
    assert(resultSync == resultAsync)
    println("ok between sync and async")
    println()
    println("check tree traverse...")
    checkTreeTraverse(tree)
    println("check tree traverse: done")
    println()
    println("------------------------------")
    println("asyncReactive: evalOnce here !")
    println("------------------------------")
    val resultAsyncReactive = Await.result(runAsyncReactive().runToFuture, Duration("5 seconds"))
    assert(resultAsyncReactive == resultSync)
    // final implementation
    val resultAsyncFromTree = Await.result(runAsyncFromTreeForAllTables(tree).runToFuture, Duration("5 seconds"))
    println()
    println("Result (2 tables) with general algorithm:")
    resultAsyncFromTree.foreach(println)
    assert(resultAsyncFromTree.contains(resultSync))
    println("ok between assync and asyncFromTree")
  }
}
