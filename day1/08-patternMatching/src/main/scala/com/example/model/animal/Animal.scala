package com.example.model.animal

sealed trait Animal
case class Dog(name: String) extends Animal
case class Cat(name: String) extends Animal

class Bird(name: String, location: String) extends Animal
object Bird {
  def apply(name: String): Bird = new Bird(name = name, location = "my house")
}
