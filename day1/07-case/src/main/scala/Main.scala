package com.example

import animal.{ Animal, BredDog, Dog }
import BredDog.rex
import Human._

object SparkMain {

  def main(args: Array[String]): Unit = {
    // case class
    val mary: Person = Woman(name = "Mary")
    val bob: Person  = Man(name = "Bob")
    val anotherDog   = rex.copy(owner = Some(mary), name = "Tirex")
    println(s"First dog is $rex and onther dog is $anotherDog")
    println(anotherDog == rex)
    val rex2 = BredDog(name = "Rex")
    println(rex2 == rex)

    type DogsPerPerson = (Person, Seq[BredDog])
    // tuple
    val dogsPerOwner: Seq[DogsPerPerson] = Seq(
      (mary -> Seq(anotherDog)),
      (bob  -> Seq(rex, rex2))
    )
    println(dogsPerOwner)

    // find person for whom we have forgot fullfilling the owner propertty
    val missingPropertyFor = dogsPerOwner.filter(personAndDogs => personAndDogs._2.exists(dog => dog.owner.isEmpty))
    println(missingPropertyFor)
  }
}
