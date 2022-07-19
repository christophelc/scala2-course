import com.axa.go.wax.MonixSpec
import monix.eval.{Fiber, Task}
import monix.execution.CancelableFuture

import scala.concurrent.Await
import scala.concurrent.duration._

class MonixFiberTest extends MonixSpec {
 import monix.execution.Scheduler.Implicits.global

 "Fiber" must "display string" in {
  val launchMissiles = Task("Missiles launched!")
  val runToBunker = Task("Run Lola run!")

  val rslt: Task[String] = for {
   fiber <- launchMissiles.start
   run <- runToBunker.onErrorHandleWith { error =>
    // Retreat failed, cancel launch (maybe we should
    // have retreated to our bunker before the launch?)
    fiber.cancel.flatMap(_ => Task.raiseError(error))
   }
   aftermath <- fiber.join
  } yield {
   Seq(aftermath, run).mkString(System.lineSeparator)
  }
  val f: CancelableFuture[String] = rslt.runToFuture
  f.foreach(println)
 }

 "Fiber" must "manage race condition" in {

  val ta: Task[Int] = Task(1 + 1).delayExecution(1.second)
  val tb: Task[Int] = Task(10).delayExecution(1.second)
  val raceTask: Task[Either[(Int, Fiber[Int]), (Fiber[Int], Int)]] = Task.racePair(ta, tb)
  val raceTaskF: CancelableFuture[Either[(Int, Fiber[Int]), (Fiber[Int], Int)]] = raceTask.runToFuture

  val race: CancelableFuture[Task[String]] = raceTaskF.map {
   case Left((a, fiber)) =>
    fiber.cancel.flatMap { _ =>
     Task.eval(s"A succeeded: $a")
    }
   case Right((fiber, b)) =>
    fiber.cancel.flatMap { _ =>
     Task.eval(s"B succeeded: $b")
    }
  }
  val f = Await.result(race, 2.seconds)
  val f2 = Await.result(f.runToFuture, 2.seconds)
  Seq("A succeeded: 2", "B succeeded: 10") must contain(f2)
 }
}