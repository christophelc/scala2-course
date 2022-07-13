package model

import model.DataType.RawFile
import model.RuntimeExecutors.EnvDataForAggregation
import monix.eval.Task
import monix.reactive.Observable

object TreeRuntime {
  private def envObservable(data: DataType, env: Env): Observable[Env] = {
    import model.RuntimeExecutors._
    data match {
      case rawFile: RawFile => rawFile.observable()
      case batch: BatchModulo  => batch.observable(env)
      case batch: BatchAggregate => batch.observable(env)
      case _ => Observable(env) // do nothing
    }
  }

  case class StateComputa(envs: Map[String, Observable[Env]] = Map(),
                          computed: Map[String, Observable[Env]] = Map())
  /**
   * @param tree a tree structure except for the leaf which make it a graph
   * @return List of resulting leaves to compute
   */
  def treeToObservable(tree: Tree[DataType]): Seq[Observable[Env]] = {
    val vertices: Iterable[Vertice[DataType]] = tree.traverse(tree.findRoot)
    val childToParents: Map[String, Seq[Edge]] = tree.edges.groupBy(_.destId).filter(_._2.nonEmpty)
    val result: StateComputa = vertices.foldLeft(StateComputa())((state, vertice) => {
      childToParents.getOrElse(vertice.id, Nil).map(_.srcId) match {
        case Nil => StateComputa(
          envs = Map(vertice.id -> Observable(Env.Empty)),
          computed = Map(vertice.id -> Observable(Env.Empty)))
        case Seq(parentId) =>
          val newEnv: Observable[Env] = state.computed(parentId).flatMap(env => envObservable(vertice.data, env))
          state.copy(
            envs = (state.envs ++ Map(vertice.id -> newEnv)) - parentId,
            computed = state.computed ++ Map(vertice.id -> newEnv)
          )
        case parentsId =>
          val parentsObs: Seq[Observable[Env]] = parentsId.map(childId => state.computed(childId))
          val zipObsEnv: Observable[Env] = parentsObs.tail.foldLeft(parentsObs.head)((acc, obs) =>
            acc.zip(obs).map(env => EnvDataForAggregation(Seq(env._1, env._2)).flatten))
          val newEnv: Observable[Env] = zipObsEnv.flatMap(env => envObservable(vertice.data, env))
            state.copy(
              envs = (state.envs ++ Map(vertice.id -> newEnv)).filterKeys(id => !parentsId.contains(id)),
              computed = state.computed ++ Map(vertice.id -> newEnv)
            )
          }
      })
    result.envs.values.toSeq
  }

  def executeFromId(id: String, tree: Tree[Observable[Env]]): Seq[Task[Env]] = {
    val leaves: Seq[Vertice[Observable[Env]]] = tree.findLeaves.flatMap(id => tree.vertices.find(_.id == id))
    leaves.map(vertice => vertice.data.firstL)
  }
  def execute(tree: Tree[DataType]): Seq[Task[Env]] = {
    val observables: Seq[Observable[Env]] = treeToObservable(tree)
    observables.map(_.firstL)
  }
}