package com.example

// val peopleMax: Int = 100 // invalid

package object Human {
  val peopleMax: Int = 100 // must be embedded in a package.

  trait Person
  class Man   extends Person
  class Woman extends Person
}
