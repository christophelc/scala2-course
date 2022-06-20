import cats.data.Kleisli
import cats.implicits._

import org.scalatest.{ FlatSpec, MustMatchers }

import scala.util.{ Failure, Success, Try }

class KleisliTest extends FlatSpec with MustMatchers {
  {

    val mul: Int => Int = _ * 2
    val div: Int => Int = 10 / _

    type FTryInt = Int => Try[Int]
    val mulWithErrors: FTryInt = x => Try(x * 2)
    val divWithErrors: FTryInt = x => Try(10 / x)

    // f compose g
    def composeWithErrors(f: FTryInt, g: FTryInt): FTryInt = x => {
      val r1 = g(x)
      r1 match {
        case Success(v)        => f(v)
        case fail @ Failure(_) => fail
      }
    }

    "Function" must "be composable" in {
      val result = (mul compose div)(10)
      assert(result == 2)
    }

    "Function with error management" must "be composable" in {
      val result = composeWithErrors(mulWithErrors, divWithErrors)(10)
      assert(result == Success(2))

      val result2 = for {
        r1 <- divWithErrors(10)
        r2 <- mulWithErrors(r1)
      } yield r2
      assert(result2 == Success(2))
    }

    "xx" must "xx" in {
      val kleisliCombine_2: Kleisli[Try, Int, Int] = Kleisli(divWithErrors) andThen Kleisli(mulWithErrors)
      assert(kleisliCombine_2.run(10) == Success(2))
    }
  }
}
