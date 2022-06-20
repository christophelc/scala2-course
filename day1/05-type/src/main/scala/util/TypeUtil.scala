package com.example.util

object TypeUtil {
  def isAnyVal(x: Any): Boolean =
    ???
  def printIsAnyVal(x: Any): Unit = {
    val b = (isAnyVal(x))
    println(s"$x of type ${x.getClass} is not of super type AnyVal: $b")
  }
}
