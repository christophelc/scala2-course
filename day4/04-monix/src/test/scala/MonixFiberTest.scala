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

 "Synchro of 1, 2 and 3" must "give 12 and 23 and then 1223" in {
  val t1: Task[Fiber[Int]] = Task(1).start
  val t2: Task[Fiber[Int]] = Task(2).start
  val t3: Task[Fiber[Int]] = Task(3).start
  // with for comprehension
  val t1223: Task[Int] = for {
   f1 <- t1
   f2 <- t2
   f3 <- t3
   r12 <- Task.parZip2(f1.join, f2.join).map(t2 => t2._1 * t2._2)
   r23 <- Task.parZip2(f2.join, f3.join).map(t2 => t2._1 * t2._2)
  } yield {
   r12 * r23
  }
  val r1223 = Await.result(t1223.runToFuture, 2.seconds)
  r1223 mustBe 12

  // with flatMap
  val t1223b: Task[Int] = t1.flatMap(f1 => t2.flatMap(f2 => t3.flatMap(f3 => {
   val t12: Task[Int] = Task.parZip2(f1.join, f2.join).map(v12 => v12._1 * v12._2)
   val t23: Task[Int] = Task.parZip2(f2.join, f3.join).map(v23 => v23._1 * v23._2)
   t12.flatMap(r12 => t23.map(r23 => r12 * r23))
  })))
  val r1223b = Await.result(t1223b.runToFuture, 2.seconds)
  r1223b mustBe 12
 }
}