package com.example

import org.scalatest.{ FlatSpec, MustMatchers }

class MainTest extends FlatSpec with MustMatchers {

  "Number multiplication" must "use implicit context" in {
    implicit val ctx = Context("Ctx1", 10)
    val n1: Number   = Number(3)
    val n2: Number   = Number(4)
    assert(n1.mul(n2) == Number(n1.a * n2.a * 10))
  }
}
