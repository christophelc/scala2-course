package com.example

object Box3 {
  def putAnimalInBox[A <: Animal](a: Animal): Box2[Animal] = Box2Impl(Some(a))
}
