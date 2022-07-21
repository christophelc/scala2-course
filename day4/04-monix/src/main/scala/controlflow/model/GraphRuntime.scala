package controlflow.model

import controlflow.model.DataType.RawFile
import controlflow.model.RuntimeExecutors.EnvDataForAggregation
import monix.eval.{Fiber, Task}

// More general than TreeRuntime
object GraphRuntime {
  private def envObservable(data: DataType, env: Env): Env = {
    import controlflow.model.RuntimeExecutors._
    data match {
      case rawFile: RawFile => rawFile.executeSync()
      case batch: BatchModulo  => batch.executeSync(env)
      case batch: BatchAggregate => batch.executeSync(env)
      case _ => env // do nothing
    }
  }

  case class StateComputa(envs: Map[String, Task[Env]] = Map(),
                          computed: Map[String, Task[Fiber[Env]]] = Map())

  // first implementation by hand/ TODO: generalize that to any sequence of tasks
  private def computeSync(tasks: Seq[Task[Fiber[Env]]]): Task[Env] = {
    tasks.size match {
      case 2 =>
        for {
          f1 <- tasks.head
          f2 <- tasks.tail.head
          fSync <- Task.parZip2(f1.join, f2.join).map(env => EnvDataForAggregation(Seq(env._1, env._2)): Env)
        } yield fSync
      case 3 =>
        for {
          f1 <- tasks.head
          f2 <- tasks(1)
          f3 <- tasks(2)
          fSync <- Task.parZip3(f1.join, f2.join, f3.join).map(env => EnvDataForAggregation(Seq(env._1, env._2, env._3)): Env)
        } yield fSync
      case 4 =>
        for {
          f1 <- tasks.head
          f2 <- tasks(1)
          f3 <- tasks(2)
          f4 <- tasks(3)
          fSync <- Task.parZip4(f1.join, f2.join, f3.join, f4.join).map(env => EnvDataForAggregation(Seq(env._1, env._2, env._3, env._4)): Env)
        } yield fSync
      case 5 =>
        for {
          f1 <- tasks.head
          f2 <- tasks(1)
          f3 <- tasks(2)
          f4 <- tasks(3)
          f5 <- tasks(4)
          fSync <- Task.parZip5(f1.join, f2.join, f3.join, f4.join, f5.join).map(env => EnvDataForAggregation(Seq(env._1, env._2, env._3, env._4, env._5)): Env)
        } yield fSync
      case 6 =>
        for {
          f1 <- tasks.head
          f2 <- tasks(1)
          f3 <- tasks(2)
          f4 <- tasks(3)
          f5 <- tasks(4)
          f6 <- tasks(5)
          fSync <- Task.parZip6(f1.join, f2.join, f3.join, f4.join, f5.join, f6.join).map(env => EnvDataForAggregation(Seq(env._1, env._2, env._3, env._4, env._5, env._6)): Env)
        } yield fSync
      case _ => throw new RuntimeException("Joining more than 6 tasks is not supported.")
    }
  }
  /**
   * @param tree a tree structure except for the leaf which make it a graph
   * @return List of resulting leaves to compute
   */
  def treeToTask(tree: Tree[DataType]): Seq[Task[Env]] = {
    val vertices: Iterable[Vertice[DataType]] = tree.traverse(tree.findRoot)
    val childToParents: Map[String, Seq[Edge]] = tree.edges.groupBy(_.destId).filter(_._2.nonEmpty)
    val result: StateComputa = vertices.foldLeft(StateComputa())((state, vertice) => {
      childToParents.getOrElse(vertice.id, Nil).map(_.srcId) match {
        case Nil => StateComputa(
          envs = Map(vertice.id -> Task(Env.Empty: Env)),
          computed = Map(vertice.id -> Task(Env.Empty: Env).start))
        case Seq(parentId) =>
          val newEnv = for {
            fComputed <- state.computed(parentId)
            newEnv <- fComputed.join.map(env => envObservable(vertice.data, env))
          } yield {
            newEnv
          }
          state.copy(
            envs = (state.envs ++ Map(vertice.id -> newEnv)) - parentId,
            computed = state.computed ++ Map(vertice.id -> newEnv.start)
          )
        case parentsId  =>
          val parentsEnv: Seq[Task[Fiber[Env]]] = parentsId.map(childId => state.computed(childId))
          val newEnv: Task[Env] = computeSync(parentsEnv).map(env => envObservable(vertice.data, env))
          state.copy(
            envs = (state.envs ++ Map(vertice.id -> newEnv)).filterKeys(id => !parentsId.contains(id)),
            computed = state.computed ++ Map(vertice.id -> newEnv.start)
          )
      }
    })
    result.envs.values.toSeq
  }

  def schedule(tree: Tree[DataType]): Seq[Task[Env]] = {
    val tasks: Seq[Task[Env]] = treeToTask(tree)
    tasks
  }
}
