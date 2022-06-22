import com.example.{ Cat, Dog }

object Main {

  def main(args: Array[String]): Unit = {
    import com.example.JsonConverter._

    val dog       = Dog("rex")
    val dogToJson = new DogToJson(dog)
    println(dogToJson.toJson)

    // with implicit class
    val cat = Cat("tiger")
    println(cat.toJson)

    /*
    Type class derivation is a way to automatically generate given instances for type classes
    which satisfy some simple condition
    => Shapeless in Scala 2
    => Scala 3 derives keyword

    aim: avoid boilerplate code (equal Eq for example...)
   */

  }
}
