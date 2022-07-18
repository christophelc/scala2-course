import com.axa.go.wax.MonixSpec
import monix.eval.{Coeval, Task}

import scala.concurrent.Await
import monix.execution.Scheduler
import monix.execution.schedulers.TestScheduler
import monix.catnap.MVar

import scala.util.{Failure, Success}
import scala.concurrent.duration._
import scala.language.postfixOps

class MonixSyncTest extends MonixSpec {
  // example from monix.io

  implicit val scheduler: Scheduler = TestScheduler()

  "Coeval" must "be lazy" in {
    // many builder: zip, map, flatMap, sequence, traverse, restartUntil
    val coeval = Coeval {
      println("Effect!")
      "Hello!"
    }
    // Coeval has lazy behavior, so nothing
    // happens until being evaluated:
    assert(coeval.value == "Hello!")
  }

  "Coeval evaluation" must "be a Try of the result" in {
    val coeval = Coeval {
      println("Effect!")
      "Hello!"
    }
    coeval.runTry match {
      case Success(value) =>
        println(value)
      case Failure(ex) =>
        System.err.println(ex)
    }
    assert(coeval.runTry == Success("Hello!"))
  }
  "Coeval evalOnce" must "be like lazy val" in {
    var a = 0
    val coeval = Coeval.evalOnce { println("Effect"); a = a + 1; "Hello!" }
    // coeval: monix.eval.Coeval[String] = Once(<function0>)

    coeval.value
    coeval.value

    // Result was memoized on the first run!
    assert(a == 1)
  }

  "Coevel" must "be tail recursive" in {
    def fib(cycles: Int, a: BigInt = 0, b: BigInt = 1): Coeval[BigInt] = {
      Coeval.eval(cycles > 0).flatMap {
        case true =>
          fib(cycles-1, b, a+b)
        case false =>
          Coeval.now(b)
      }
    }
    val coeval: Coeval[BigInt] = fib(cycles = 5)
    assert(coeval.value == BigInt(8))
  }
  "Mutual recursive call" must "be stack safe" in {
    def odd(n: Int): Coeval[Boolean] =
      Coeval.eval(n == 0).flatMap {
        case true => Coeval.now(false)
        case false => even(n - 1)
      }

    def even(n: Int): Coeval[Boolean] =
      Coeval.eval(n == 0).flatMap {
        case true => Coeval.now(true)
        case false => odd(n - 1)
      }
    assert(even(1000000).value)
  }
  "Coeval as an applicative functor" must "run in parallel" in {
    val locationCoeval: Coeval[String] = Coeval.eval("Paris")
    val phoneCoeval: Coeval[String] = Coeval.eval("0102030405")
    val addressCoeval: Coeval[String] = Coeval.eval("123 Eval Avenue")

    val aggregate =
      Coeval.zip3(locationCoeval, phoneCoeval, addressCoeval).map {
        case (location, phone, address) => s"$location-$phone-$address"
      }
    aggregate.value mustBe("Paris-0102030405-123 Eval Avenue")
  }

  "MVar used to compute sum of 1 to 1000" must "equal 4950" in {
    final class MLock(mvar: MVar[Task, Unit]) {
      def acquire: Task[Unit] =
        mvar.take

      def release: Task[Unit] =
        mvar.put(())

      def greenLight[A](fa: Task[A]): Task[A] =
        for {
          _ <- acquire
          a <- fa.doOnCancel(release)
          _ <- release
        } yield a
    }

    object MLock {
      /** Builder. */
      def apply(): Task[MLock] =
        MVar[Task].of(()).map(v => new MLock(v))
    }
    def sum(state: MVar[Task, Int], list: List[Int]): Task[Int] =
      list match {
        case Nil => state.take
        case x :: xs =>
          state.take.flatMap { current =>
            state.put(current + x).flatMap(_ => sum(state, xs))
          }
      }
    val task =
      for {
        lock <- MLock()
        state <- MVar[Task].of(0)
        task = sum(state, (0 until 100).toList)
        r <- lock.greenLight(task)
      } yield r

    // Evaluate
    val f = task.runToFuture
    val r = Await.result(f, 5 seconds)
    r mustBe(4950)
  }
}
