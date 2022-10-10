import com.example.{ Cat, Dog }

import com.example.Show

object Main {

  def exampleTypeClass(): Unit = {
    import com.example.Person
    import com.example.SerializerInstance._

    val person = Person("John", "Doe")

    // first way to call show
    println(Show.show(person))
    println(Show.show(person.firstname))

    // second way to call show
    import com.example.Show._
    println(person.show)
    println(person.firstname.show)
  }

  def main(args: Array[String]): Unit = {
    import com.example.JsonConverter._

    val dog       = Dog("rex")
    val dogToJson = new DogToJson(dog)
    println(dogToJson.toJson)

    // with implicit class
    val cat = Cat("tiger")
    println(cat.toJson)

    // more detailed example
    println()
    println("type class example")
    exampleTypeClass()

    /*
    Type class derivation is a way to automatically generate given instances for type classes
    which satisfy some simple condition
    => Shapeless in Scala 2
    => Scala 3 derives keyword

    aim: avoid boilerplate code (equal Eq for example...)
   */

  }
}
