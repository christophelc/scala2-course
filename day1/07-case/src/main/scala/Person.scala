package com.example

// val peopleMax: Int = 100 // invalid

package object Human {
  val peopleMax: Int = 100 // must be embedded in a package.

  trait Person {
    val name: String
  }
  case class Man(override val name: String)   extends Person
  case class Woman(override val name: String) extends Person
}
