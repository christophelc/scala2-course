package com.example

import cats.data.Chain
import cats.data.Validated.{ Invalid, Valid }
import com.example.MonadFunctor.{ flatmapSucc, mapSucc, succ }
import com.example.Transform.{ mul, transformations }
import org.scalatest.{ FlatSpec, MustMatchers }

import scala.util.{ Success, Try }

class MainTest extends FlatSpec with MustMatchers {

  "The Seq monad" must "be composable" in {
    assert(succ(1) == Seq(1, 2))

    val source              = Seq(1, 2, 3)
    val resultNotComposable = mapSucc(source)
    assert(
      resultNotComposable == Seq(
        Seq(1, 2),
        Seq(2, 3),
        Seq(3, 4)
      )
    )
    val resultComposable = flatmapSucc(source)
    assert(
      resultComposable == Seq(
        1, 2, 2, 3, 3, 4
      )
    )
    val finalResult = mapSucc(resultComposable)
    assert(
      finalResult == Seq(
        Seq(1, 2),
        Seq(2, 3),
        Seq(2, 3),
        Seq(3, 4),
        Seq(3, 4),
        Seq(4, 5)
      )
    )
  }
  "Either monad" must "be composable" in {
    val a: Either[String, Int] = Right(1)
    val b: Either[String, Int] = Right(2)
    val result = for {
      result1 <- a
      result2 <- b
    } yield result1 + result2
    result must equal(Right(3))

    a.flatMap(result1 => b.map(result2 => result1 + result2)) must equal(Right(3))
  }
  "Try monad" must "be composable" in {
    val a: Try[Int] = Try(1)
    val b: Try[Int] = Try(2)
    val result = for {
      result1 <- a
      result2 <- b
    } yield (result1 + result2)
    result must equal(Success(3))

    val resultError = for {
      r <- Try(3 / 0)
    } yield r
    assert(resultError.isFailure)
  }

  "Option Monad applied to a function Int => Int" must "give an Option[Int => Int]" in {
    val maybef: Option[Int => Int] = Some(2).map(n => mul(n)(_))
    maybef.map(f => f(3)) must be(Some(6))
  }

  "Applicative functor" must "validate a valid input" in {
    val result = FormValidatorNec.validateForm(
      username = "Joe",
      firstName = "John",
      age = 21
    )
    result must be(
      Valid(
        RegistrationData(
          username = "Joe",
          firstname = "John",
          age = 21
        )
      )
    )
  }
  "Applicative functor" must "invalidate an invalid input" in {
    val result = FormValidatorNec.validateForm(
      username = "Joe_",
      firstName = "John_",
      age = 7
    )
    result must be(
      Invalid(
        Chain(
          UsernameHasSpecialCharacters,
          FirstNameHasSpecialCharacters,
          AgeIsInvalid
        )
      )
    )
  }
}
