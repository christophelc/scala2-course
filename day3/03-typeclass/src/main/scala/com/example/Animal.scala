package com.example

// important: removing Animal here does not change anything ! typeclass is not about class inheritance

// data
sealed trait Animal
final case class Dog(name: String)  extends Animal
final case class Cat(name: String)  extends Animal
final case class Bird(name: String) extends Animal

///////////////////////////////////////////////////
// Behaviour API
trait ToJson[T] {
  def toJson: String
}

// Behaviour implementation for Dog
object JsonConverter {
  final class DogToJson(dog: Dog) extends ToJson[Dog] {
    override def toJson: String =
      s"""{
         | 'type': 'dog',
         | 'name' : '${dog.name}'
         |}""".stripMargin
  }

  // Behaviour implementation for Cat
  implicit final class CatToJson(cat: Cat) extends ToJson[Cat] {
    override def toJson: String =
      s"""{
         | 'type': 'cat',
         | 'name' : '${cat.name}'
         |}""".stripMargin
  }
}
