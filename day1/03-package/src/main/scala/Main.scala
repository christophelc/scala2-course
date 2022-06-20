package com.example

import animal.{ Animal, Dog }

object SparkMain {

  def main(args: Array[String]): Unit = {
    val dog = new Dog()
    println(dog)
    println(s"Population max: ${Animal.animalMax}")
    val aMan = new Human.Man()
    println(s"A man is $aMan")
  }
}
