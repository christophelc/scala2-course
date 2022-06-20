package com.example

object MonadFunctor {
  def succ(i: Int): Seq[Int]                   = Seq(i, i + 1)
  def mapSucc(source: Seq[Int]): Seq[Seq[Int]] = source.map(succ)
  def flatmapSucc(source: Seq[Int]): Seq[Int]  = source.flatMap(succ)
  def flatmap2Succ(source: Seq[Int]): Seq[Int] = source.map(succ).flatten
}

object Transform {
  def transformations(n: Int) = Seq(mul(n)(_), mul2(n)(_))

  def mul(n: Int)(x: Int): Int = x * n
  def mul2(n: Int)(x: Int): Option[Int] = n match {
    case 0 => None
    case _ => Some(x * n)
  }
}
