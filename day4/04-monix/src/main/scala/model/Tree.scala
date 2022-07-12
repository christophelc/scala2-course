package model

case class Vertice[T](id: String, data: T)
case class Edge(srcId: String, destId: String)

object Tree {
  def root[T](id: String, data: T) = Tree[T](vertices = Seq(Vertice(id, data)))
}
case class Tree[T](val vertices: Seq[Vertice[T]] = Nil, val edges: Seq[Edge] = Nil) {
  def findVerticeWithSeveralParent: Seq[String] = edges.groupBy(_.destId).filter(_._2.size >= 2).keys.toSeq
  def addChild(fromId: String, id: String, data: T): Tree[T] = {
    require(vertices.exists(_.id == fromId), s"id $fromId does not exist")
    val vertice = Vertice[T](id = id, data = data)
    vertices.find(_.id == id).fold(
      this.copy(vertices = vertices :+ vertice, edges = edges :+ Edge(fromId, id))
    )(_ => this.copy(edges = edges :+ Edge(fromId, id)))
  }
}