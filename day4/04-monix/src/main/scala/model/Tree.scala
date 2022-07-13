package model

case class Vertice[T](id: String, data: T)
case class Edge(srcId: String, destId: String)

object Tree {
  def root[T](id: String, data: T) = Tree[T](vertices = Seq(Vertice(id, data)))
}
case class Tree[T](val vertices: Seq[Vertice[T]] = Nil, val edges: Seq[Edge] = Nil) {
  val pointSyncs: Map[String, Seq[String]] = edges.groupBy(_.destId).filter(_._2.size >= 2).mapValues(_.map(_.srcId))

  // quick implementation
  def findRoot: Vertice[T] = {
    require(edges.nonEmpty) // at leas one child
    val rootId = edges.map(_.srcId)
      .distinct
      .diff(edges.map(_.destId))
      .head
    vertices.find(_.id == rootId).getOrElse(throw new RuntimeException("never reached"))
  }
  def findLeaves: Seq[String] = vertices.map(_.id).diff(edges.map(_.srcId))
  def findParents(id: String): Seq[String] = {
    edges.filter(_.destId == id).map(_.srcId)
  }
  def findChildren(id: String): Seq[String] = edges.filter(_.srcId == id).map(_.destId)
  def addChild(fromId: String, id: String, data: T): Tree[T] = {
    require(vertices.exists(_.id == fromId), s"id $fromId does not exist")
    val vertice = Vertice[T](id = id, data = data)
    vertices.find(_.id == id).fold(
      this.copy(vertices = vertices :+ vertice, edges = edges :+ Edge(fromId, id))
    )(_ => this.copy(edges = edges :+ Edge(fromId, id)))
  }
  case class TraverseState(iter: Iterable[Vertice[T]], computed: Seq[String] = Nil)

  /**
   * List vertices in an order where required tasks are first computed
   * @param currentV current vertice to compute
   * @param computed already computed vertices
   * @return List of vertices ordered for easy computation
   */
  def traverse(currentV: Vertice[T], computed: Seq[String] = Nil): Iterable[Vertice[T]] = {
    findChildren(currentV.id)
      .flatMap(childId => vertices.find(_.id == childId))
      .filter(vertice => !pointSyncs.contains(vertice.id) || pointSyncs(vertice.id).forall(parentId => computed.contains(parentId))) match {
      case Nil => Iterable(currentV)
      case children => children.foldLeft(TraverseState(iter = Iterable(currentV)))((state, vertice) =>
        state.copy(
          iter = state.iter ++ traverse(vertice, state.computed :+ vertice.id),
          computed = state.computed :+ vertice.id)).iter
    }
  }
}