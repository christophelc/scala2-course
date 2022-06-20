import com.example.{ Dog, Show, ShowSpecific, VectorInt }

object SparkMain {

  def selfType: Unit = {
    val dog = Dog("Rex")
    dog.show()
    val me      = dog.me
    val meAgain = dog.meAgain
    println(me)
    println(meAgain)
  }
  def thisType: Unit = {
    val dslInit = new Show("1")
    dslInit.build.show()
    val dsl2Init = new ShowSpecific("2")
    dsl2Init.build.show()
  }

  def main(args: Array[String]): Unit = {
    selfType
    println()
    thisType
  }
}
