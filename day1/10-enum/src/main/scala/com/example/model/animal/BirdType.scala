package com.example.model.animal

// Don't do this: see https://www.baeldung.com/scala/enumeratum
object BirdType extends Enumeration {
  type BirdType = Value

  val Eagle, Owl = Value
}
