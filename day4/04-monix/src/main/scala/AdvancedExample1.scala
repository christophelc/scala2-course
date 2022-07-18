import controlflow.model.{BatchAggregate, BatchModulo, DataType, Env, Tree, TreeRuntime}
import controlflow.model.DataType.RawFile
import controlflow.model.RuntimeExecutors.EnvDataModulo
import controlflow.model.data.TablePlant
import monix.eval.Task
import monix.reactive.Observable

import scala.annotation.nowarn
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
 * Batch12, Batch13: apply Modulo2
 * Batch22, Batch23, Batch24: apply Modulo3
 * Batch14, Batch25: reduce by applying  (pct_rate_of_growth +1) * (pct_rate_of_growth + 1)
 */
object AdvancedExample1 {
  val t1RawFile: RawFile = RawFile("table1.csv")
  val t1Batch12Modulo: BatchModulo = BatchModulo(batchName = "Batch12", modulo = 2, offset = 0)
  val t1Batch13Modulo: BatchModulo = BatchModulo(batchName = "Batch13", modulo = 2, offset = 1)
  val t1Batch14Aggregate: BatchAggregate = BatchAggregate(batchName = "Batch14")

  val t2RawFile: RawFile = RawFile("table2.csv")
  val t2Batch22Modulo: BatchModulo = BatchModulo(batchName = "Batch22", modulo = 2, offset = 0)
  val t2Batch23Modulo: BatchModulo = BatchModulo(batchName = "Batch23", modulo = 2, offset = 1)
  val t2Batch24Modulo: BatchModulo = BatchModulo(batchName = "Batch24", modulo = 2, offset = 1)
  val t2Batch25Aggregate: BatchAggregate = BatchAggregate(batchName = "Batch25")

  private def env2TablePlant(env: Env): TablePlant = {
    env match { case EnvDataModulo(Some(tablePlant)) => tablePlant }
  }

  // First implementation, only for table1: By hand
  def runSync(): TablePlant = {
    import controlflow.model.RuntimeExecutors._
    val result1: Env = t1RawFile.executeSync()
    val result12: Env = t1Batch12Modulo.executeSync(result1)
    val result13: Env = t1Batch13Modulo.executeSync(result1)
    val dataForAggregation: Env = EnvDataForAggregation(Seq(result12, result13))
    val result14: Env = t1Batch14Aggregate.executeSync(dataForAggregation)
    env2TablePlant(result14)
  }

  // 2nd implementation, with Task
  def runAsync(): Task[TablePlant] = {
    import controlflow.model.RuntimeExecutors._
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

  def checkTreeTraverse(tree: Tree[DataType]): Unit = {
    val seq = tree.traverse(tree.findRoot).map(_.id).toSeq
    val expected = Seq(
      "root",
      "t1RawFile",
      "batch12",
      "batch13",
      "batch14",
      "t2RawFile",
      "batch22",
      "batch23",
      "batch24",
      "batch25"
    )
    assert(seq == expected)
  }
  // a step further with Observable
  def runAsyncReactive(): Task[TablePlant] = {
    import controlflow.model.RuntimeExecutors._
    val oResult1: Observable[Env] = t1RawFile.observable()

    // flatMap when only one parent dependendcy
    val oResult12: Observable[Env] = oResult1.flatMap(result1 => t1Batch12Modulo.observable(result1))
    val oResult13: Observable[Env] = oResult1.flatMap(result1 => t1Batch13Modulo.observable(result1))
    // zip when there are at least two parent dependencies
    val oDataForAggregation: Observable[Env] = oResult12.zip(oResult13).map(envs => EnvDataForAggregation(Seq(envs._1, envs._2)))
    val oResult14: Observable[Env] = oDataForAggregation.flatMap(tDataForAggregation => t1Batch14Aggregate.observable(tDataForAggregation))
    oResult14.firstL.map(env2TablePlant)
  }
  def runAsyncReactiveT2(): Task[TablePlant] = {
    import controlflow.model.RuntimeExecutors._
    val oResult2: Observable[Env] = t2RawFile.observable()

    // flatMap when only one parent dependendcy
    val oResult22: Observable[Env] = oResult2.flatMap(result1 => t2Batch22Modulo.observable(result1))
    val oResult23: Observable[Env] = oResult2.flatMap(result1 => t2Batch23Modulo.observable(result1))
    val oResult24: Observable[Env] = oResult2.flatMap(result1 => t2Batch24Modulo.observable(result1))
    // zip when there are at least two parent dependencies: zip3 like here
    val oDataForAggregation: Observable[Env] = oResult22.zip(oResult23).zip(oResult24).map(envs =>
      EnvDataForAggregation(Seq(envs._1._1, envs._1._2, envs._2)))
    val oResult25: Observable[Env] = oDataForAggregation.flatMap(tDataForAggregation => t2Batch25Aggregate.observable(tDataForAggregation))
    oResult25.firstL.map(env2TablePlant)
  }

  def runAsyncFromTreeForAllTables(tree: Tree[DataType]): Task[Seq[TablePlant]] = {
    // convert observable to Task (we lose EvalOnce)
    val tasks: Seq[Task[Env]] = TreeRuntime.schedule(tree).map(_.firstL)
    Task.sequence(tasks.map(task => task.map(env2TablePlant)))
  }

  def run(@nowarn args: Array[String]): Unit = {
    val tree: Tree[DataType] = Tree.root[DataType](id = "root", data = DataType.Nothing)
      .addChild(fromId = "root", id = "t1RawFile",  data = t1RawFile)
      .addChild(fromId = "t1RawFile", id = "batch12", data = t1Batch12Modulo)
      .addChild(fromId = "t1RawFile", id = "batch13", data = t1Batch13Modulo)
      .addChild(fromId = "batch12", id = "batch14", data= t1Batch14Aggregate)
      .addChild(fromId = "batch13", id = "batch14", data= t1Batch14Aggregate)

      .addChild(fromId = "root", id = "t2RawFile",  data = t2RawFile)
      .addChild(fromId = "t2RawFile", id = "batch22", data = t2Batch22Modulo)
      .addChild(fromId = "t2RawFile", id = "batch23", data = t2Batch23Modulo)
      .addChild(fromId = "t2RawFile", id = "batch24", data = t2Batch24Modulo)
      .addChild(fromId = "batch22", id = "batch25", data= t2Batch25Aggregate)
      .addChild(fromId = "batch23", id = "batch25", data= t2Batch25Aggregate)
      .addChild(fromId = "batch24", id = "batch25", data= t2Batch25Aggregate)
    val resultSync = runSync()

    import monix.execution.Scheduler.Implicits.global
    val resultAsync = Await.result(runAsync().runToFuture, Duration("5 seconds"))
    assert(resultSync == resultAsync)
    println("ok between sync and async")
    checkTreeTraverse(tree)
    val resultAsyncReactive = Await.result(runAsyncReactive().runToFuture, Duration("5 seconds"))
    assert(resultAsyncReactive == resultSync)
    val resultAsyncReactive25 = Await.result(runAsyncReactiveT2().runToFuture, Duration("5 seconds"))
    println("Result for table2 (asyncReactive)")
    println(resultAsyncReactive25)
    // final implementation
    val resultAsyncFromTree = Await.result(runAsyncFromTreeForAllTables(tree).runToFuture, Duration("5 seconds"))
    println()
    println("Result (2 tables) with general algorithm:")
    resultAsyncFromTree.foreach(println)
    assert(resultAsyncFromTree.contains(resultSync))
    println("ok between assync and asyncFromTree")
  }
}
