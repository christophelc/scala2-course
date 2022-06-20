package com.example

import org.scalatest.{ FlatSpec, MustMatchers }

class MainTest extends FlatSpec with MustMatchers {

  "Matches function" must "work" in {
    val regexpAsString = "[0-9]+"
    assert("123".matches(regexpAsString))
    assert(!"abc".matches(regexpAsString))
  }

  "Regex findAllIn" must "work" in {
    val regex = "[0-9]+".r // scala.util.matching.Regex
    assert(regex.findAllIn("123 456").toSeq == Seq("123", "456"))
  }
  "Extract pattern" must "work" in {
    val regex         = "([0-9]+)-([0-9]+)".r // scala.util.matching.Regex
    val regex(d1, d2) = "123-456"
    assert((d1, d2) == ("123", "456"))
  }
}
