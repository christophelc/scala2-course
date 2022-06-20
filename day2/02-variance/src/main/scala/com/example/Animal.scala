package com.example

// see https://slides.com/petrabierleutgeb/polymorphism-in-scala-scaladays19/fullscreen

trait Animal {
  def sound: String
}
case class Cat(name: String) extends Animal {
  override def sound = "miaou"
}
class WildDog(name: String) extends Animal {
  override def sound: String = "woawoa"
}

case class Dog(name: String) extends WildDog(name) {
  override def sound: String = "woa"
}
