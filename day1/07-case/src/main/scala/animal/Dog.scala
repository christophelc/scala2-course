package com.example.animal

import com.example.Human.Person

// According to the context, we define the case class with attributes:
// Can a dog be a wild dog ? In wild, does it have a name ? Or it does only represent a Group ?
// See Ontology here and functional specification.
//case class Dog(owner: Option[Person], name: Option[String]) extends Animal => too generic
trait Dog                            extends Animal
case class WildDog(location: String) extends Dog

// When owner is known, does it mean we don't know the owner or sth else ?
case class BredDog(owner: Option[Person] = None, name: String)

// case object MyDog extends BredDog("Rex") // problem here !

object BredDog {
  val rex = BredDog(name = "Rex")
}
