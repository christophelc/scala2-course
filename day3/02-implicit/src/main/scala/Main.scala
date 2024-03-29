import com.example.BlockingTask

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.{ Failure, Success }

object Main {

  def contextBound(): Unit = {
    def foo[A](seq: Seq[A])(implicit x: scala.math.Ordering[A]): Seq[A] =
      seq.sorted(x)
    def foo2[A: scala.math.Ordering](seq: Seq[A]): Seq[A] =
      seq.sorted

    def foo3[A: scala.math.Ordering](seq: Seq[A]): Seq[A] = {
      val order = implicitly[scala.math.Ordering[A]] // just for example
      seq.sorted(order)
    }

    println(foo(Seq(4, 3, 2)))
    println(foo2(Seq(4, 3, 2)))
    println(foo3(Seq(4, 3, 2)))
  }

  def stringExtgension(): Unit = {
    import com.example.StringUtils._
    println("abc".bracket)
  }

  def phantomType(): Unit = {
    import com.example.{ Closed, Door, Open }

    val door = Door[Open]().close
    door match {
      case _: Door[Closed] => println(s"Door closed")
      case _               => println("Never reached !")
    }
    door.open match {
      case _: Door[Open] => println(s"Door opened")
      case _             => println("Never reached !")
    }

    // not compile
    //Door[Closed]().close
    //Door[Open]().open
  }

  def main(args: Array[String]): Unit = {
    import scala.concurrent.ExecutionContext.Implicits.global

    val task = BlockingTask("task1")
    // Await only for example here
    Await.ready(task.compute, 3 seconds).value.get match {
      case Success(r) => println(s"computed: $r")
      case Failure(t) => t.printStackTrace()
    }

    println()
    println("Context bound")
    contextBound()

    println()
    println("String extension")
    stringExtgension()

    println()
    println("phantom type")
    phantomType()
    println()
  }
}
