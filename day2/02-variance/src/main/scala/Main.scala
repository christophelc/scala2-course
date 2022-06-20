import com.example.{ Animal, Box, Box2, Box2Impl, Box3, BoxImpl, Cat, Dog, WildDog }

object Main {

  /**
   * First POC
   */
  def firstImplementation: Unit = {
    val dog = Dog("Rex")
    val cat = Cat("Tiger")

    val boxDog                  = BoxImpl[Animal]().put(dog)
    val boxCat                  = BoxImpl[Animal]().put(cat)
    val boxes: Seq[Box[Animal]] = Seq(boxDog, boxCat) // OK
    println(boxes)
    println(boxDog.put(cat)) // This should not even compile

    // another problem
    val boxDog2 = BoxImpl[Dog](Some(dog))
    val boxCat2 = BoxImpl[Cat](Some(cat))
    val boxes2  = Seq(boxDog2, boxCat2) // looks good since it compiles but...
    // not valid in fact if we look closer to the inferred type
    // found   : Seq[com.example.BoxImpl[_ >: com.example.Cat with com.example.Dog <: Product with Serializable with com.example.Animal]]
    //val boxes2: Box[Animal] = Seq(boxDog2, boxCat2)
    println(boxes2)
  }

  /**
   * Fix step by step
   */
  def secondImplementation: Unit = {
    val dog                      = Dog("Rex")
    val cat                      = Cat("Tiger")
    val boxDog                   = Box2Impl[Dog](Some(dog))
    val boxCat                   = Box2Impl[Cat](Some(cat))
    val boxes: Seq[Box2[Animal]] = Seq(boxDog, boxCat)
    println(boxes)
  }
  def thirdImplementation: Unit = {
    val dog                      = Dog("Rex")
    val cat                      = Cat("Tiger")
    val boxDog                   = Box3.putAnimalInBox(dog)
    val boxCat                   = Box3.putAnimalInBox(cat)
    val boxes: Seq[Box2[Animal]] = Seq(boxDog, boxCat)
    println(boxes)
  }

  def finalImplementation: Unit = {}
  def main(args: Array[String]): Unit = {
    // We have two types of animals: Dog and Cat
    // We want to define Box for Cat and Box for Dog
    // We can mix CatBox and DogBox
    // A CatBox is not compatible with a DogBox

    // first POC
    println("First implementation")
    firstImplementation

    // How to fix ?
    // first add covariance
    println()
    println("Second implementation")
    secondImplementation

    println()
    println("Third implementation")
    // put all together
    thirdImplementation

    // see too the interesting discussion here:
    // https://stackoverflow.com/questions/43180310/covariant-type-a-occurs-in-contravariant-position-in-type-a-of-value-a

    println()
    val fDog: Animal => String = (a: Animal) => a.sound
    // T: consumed, U :produced
    //  trait Function1[-T, +U] {
    //    def apply(x: T): U
    //  }
    println("fDog: Animal => String = (a: Animal) => a.sound")
    println(fDog(Dog("Rex")))
    println(fDog(new WildDog("Rex")))

    println("fDogWild: WildDog => String = (d: WildDog) => d.sound")
    val fDogWild: WildDog => String = (d: WildDog) => d.sound
    println(fDogWild(new WildDog("Rex")))
    println(fDogWild(Dog("Rex")))
    // T2: WildDog <: T1: Dog, U1 = String
    //  => fDogWild <: fDog
  }
}
