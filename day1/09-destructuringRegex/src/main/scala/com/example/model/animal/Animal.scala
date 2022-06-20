package com.example.model.animal

sealed trait Animal
case class Dog(name: String) extends Animal
case class Cat(name: String) extends Animal

// see we added 'val' keywork to make fields readable
class Bird(val name: String, val location: String) extends Animal
object Bird {
  def apply(name: String): Bird = new Bird(name = name, location = "my house")

  def unapply(bird: Bird): Option[(String, String)] = Some(bird.name, bird.location)
}
