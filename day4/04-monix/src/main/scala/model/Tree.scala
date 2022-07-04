package model

case class Tree[T](val data: T, val children: Seq[Tree[T]] = Nil) {
  def addChild(node: Tree[T]): Tree[T] = this.copy(children = children :+ node)
  def addChildren(children: Seq[Tree[T]]): Tree[T] = this.copy(children = this.children ++ children)
}