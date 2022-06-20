package com.example.util

object TypeUtil {
  // see https://blog.bruchez.name/posts/generalized-type-constraints-in-scala
  def isAnyVal[T](x: T)(implicit evidence: T <:< AnyVal = null) = evidence != null

  def printIsAnyVal(x: Any): Unit = {
    val b = (isAnyVal(x))
    println(s"$x of type ${x.getClass} is not of super type AnyVal: $b")
  }
}
