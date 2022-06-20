package com.example

import animal.{ Animal, Dog }
import util.TypeUtil._

object SparkMain {

  def main(args: Array[String]): Unit =
    println(isAnyVal(3.0))
}
