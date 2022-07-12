package model

import com.example.MonixMain.env2TablePlant
import model.DataType.RawFile
import model.data.TablePlant
import monix.eval.Task

object TreeRuntime {
  private def envExecute(data: DataType, env: Env): Task[Env] = {
    import model.RuntimeExecutors._
    data match {
      case rawFile: RawFile => rawFile.executeAsync()
      case batch: BatchModulo  => batch.executeAsync(env)
      case batch: BatchAggregate => batch.executeAsync(env)
      case _ => Task(env) // do nothing
    }
  }

  case class TreeWithEnv(tree: Tree[Task[Env]], taskEnv: Task[Env])

  def buildTreeTask(vertice: Vertice[DataType],
                    taskEnv: Task[Env],
                    tree: Tree[DataType],
                    treeTask: Tree[Task[Env]]): Tree[Task[Env]] = {
    val childrenId: Seq[String] = tree.edges.filter(edge => edge.srcId ==  vertice.id).map(_.destId)
    val children: Seq[Vertice[DataType]] = tree.vertices.filter(vertice => childrenId.contains(vertice.id))
    children match {
      case Nil => treeTask
      case _ =>
        children.foldLeft(treeTask)((currentTree, child) => {
          val childTaskEnv: Task[Env] = taskEnv.flatMap(env => envExecute(child.data, env))
          buildTreeTask(
            vertice = child,
            taskEnv = childTaskEnv,
            tree = tree,
            treeTask = currentTree.addChild(
              fromId = vertice.id,
              id = child.id,
              data = childTaskEnv)
          )
        })
    }
  }
  /**
   * For simplicity, we will consider that our point of synchronization can only occur at the end of the processing
   * Thus we start to schedule our jobs from this point of synchronization.
   *
   * @param tree a tree structure except for the leaf which make it a graph
   * @param syncId id of the node which has multiple parents
   * @return Task ready to compute the Env embedding the result
   */
  def createTaskFromSync(tree: Tree[DataType])(syncId: String): Task[Env] = {
    val root: Vertice[DataType] = tree.vertices.find(_.id == "root").getOrElse(throw new RuntimeException("No 'root' id in the tree"))
    val treeOfTasks: Tree[Task[Env]] = buildTreeTask(
      vertice = root,
      taskEnv = Task(Env.Empty),
      tree = tree,
      treeTask = Tree.root[Task[Env]](id = root.id, data = Task(Env.Empty))
    )
    assert(tree.edges == treeOfTasks.edges)
    ???
  }
}
