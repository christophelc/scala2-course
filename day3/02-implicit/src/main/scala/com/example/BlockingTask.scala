package com.example

import scala.concurrent.{ ExecutionContext, Future }

case class BlockingTask(name: String)(implicit val ec: ExecutionContext) {
  def compute: Future[Int] = Future.successful(3)
}
