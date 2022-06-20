package com.example

trait Box[T] {
  def put(t: T): Box[T]
  def peek: Option[T]
}

// basic implementation for example only
case class BoxImpl[T](maybeContent: Option[T] = None) extends Box[T] {
  override def put(t: T): Box[T] = BoxImpl(Some(t))
  override def peek: Option[T]   = maybeContent
}
