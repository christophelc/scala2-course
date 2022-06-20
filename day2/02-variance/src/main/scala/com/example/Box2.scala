package com.example

trait Box2[+T] {
  def peek: Option[T]
}

case class Box2Impl[+T](maybeContent: Option[T] = None) extends Box2[T] {
  override def peek: Option[T] = maybeContent
}
