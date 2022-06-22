package com.example.util

import org.scalatest.{ FlatSpec, MustMatchers }
import TypeUtil._

class UdfDogName(val name: String) extends AnyVal

class TypeUtilTest extends FlatSpec with MustMatchers {

  "AnyVal types" must "be checked as AnyVal" in {
    // primitives types
    val xInt: Int       = 3
    val xFloat: Float   = 3.0f
    val xDouble: Double = 3.0

    assert(isAnyVal(xInt))
    assert(isAnyVal(xFloat))
    assert(isAnyVal(xDouble))
  }
  "BigDecimal" must "not be of type AnyVal" in {
    val xBigDecimal: BigDecimal = BigDecimal("123.45")
    assert(!isAnyVal(xBigDecimal))
  }

  "Tuple type" must "be checked as not AnyVal" in {
    val t = (1, 2)
    assert(!isAnyVal(t))
  }

  "user defined anyval" must "be check as AnyVal" in {
    val dogName: UdfDogName = new UdfDogName("Rex")
    assert(isAnyVal(dogName))
  }
}
