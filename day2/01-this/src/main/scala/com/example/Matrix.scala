package com.example

trait Matrix[T] {
  def cell(i: Int)(j: Int): T
  def +(m: Matrix[T]): Matrix[T]
}

// dummy impleementation
case class MatrixInt(rows: Seq[Vector[Int]] = Nil) extends Matrix[Int] {
  override def cell(i: Int)(j: Int): Int      = ???
  override def +(m: Matrix[Int]): Matrix[Int] = ???
}
