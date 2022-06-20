package com.example

trait Vector[T] {
  def cell(i: Int): T
  def +(vector: Vector[T]): Vector[T]
}

// first implementation
case class VectorInt(elem: Seq[Int] = Nil) extends Vector[Int] {
  override def cell(i: Int): Int                   = ???
  override def +(vector: Vector[Int]): Vector[Int] = ???
}
