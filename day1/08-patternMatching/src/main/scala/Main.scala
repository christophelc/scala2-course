import com.example.model.animal.{ Animal, Bird, Cat, Dog }

object SparkMain {

  def whichAnimalAmI(a: Animal): String =
    a match {
      case _: Bird => "bird"
      case _: Dog  => "dog"
      case _: Cat  => "cat"
    }
  def extractName(a: Animal): String =
    a match {
      case Dog(name) => name
      case Cat(name) => name
      // case Bird(name) => name // problem here
    }
  def main(args: Array[String]): Unit = {
    val bird  = new Bird("owl", "garden")
    val bird2 = Bird("eagle")
    println(bird)
    println(whichAnimalAmI(bird))
    println(bird2)
    println(whichAnimalAmI(bird2))
  }
}
