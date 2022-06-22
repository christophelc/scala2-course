package com.example

import org.scalatest.{ FlatSpec, MustMatchers }

import java.time.Instant
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ Await, Future }
import scala.concurrent.duration._
import scala.language.postfixOps

class MainTest extends FlatSpec with MustMatchers {

  case class Batch(idx: Int, timeExecutionStart: Long)

  def runBatch(batch: Batch): Batch = {
    Thread.sleep(1000)
    batch.copy(timeExecutionStart = Instant.now().toEpochMilli())
  }

  "Two batches run in serial" must "require more than 2 seconds" in {
    val nBatch  = 2
    val batches = (1 to nBatch).map(i => Batch(idx = i, timeExecutionStart = 0))
    val start   = Instant.now()
    batches.foreach(runBatch)
    val end     = Instant.now()
    val delayMs = end.toEpochMilli - start.toEpochMilli
    assert(delayMs > 2000)
  }
  "Two batches run in serial with Future initialized on the fly" must "require more than 2 seconds" in {
    val batch1 = Batch(idx = 1, timeExecutionStart = 0)
    val batch2 = Batch(idx = 1, timeExecutionStart = 0)
    val start  = Instant.now()
    val fResult = for {
      b1 <- Future(runBatch(batch1))
      b2 <- Future(runBatch(batch2))
    } yield Seq(b1, b2)
    val result = Await.result(fResult, 5 second)
    assert(result.size == 2)
    val end     = Instant.now()
    val delayMs = end.toEpochMilli - start.toEpochMilli
    assert(delayMs > 2000)
  }
  "Two batches run in parallel with pre-initialized Future" must "require less than 2 seconds" in {
    val batch1  = Batch(idx = 1, timeExecutionStart = 0)
    val batch2  = Batch(idx = 1, timeExecutionStart = 0)
    val start   = Instant.now()
    val fBatch1 = Future(runBatch(batch1))
    val fBatch2 = Future(runBatch(batch2))
    val fResult = for {
      b1 <- fBatch1
      b2 <- fBatch2
    } yield Seq(b1, b2)
    val result = Await.result(fResult, 5 second)
    assert(result.size == 2)
    val end     = Instant.now()
    val delayMs = end.toEpochMilli - start.toEpochMilli
    assert(delayMs < 2000)
  }
  "Two batches run in parallel with joined Future" must "require less than 2 seconds" in {
    val nBatch   = 2
    val batches  = (1 to nBatch).map(i => Batch(idx = i, timeExecutionStart = 0))
    val start    = Instant.now()
    val fBatches = Future.sequence(batches.map(b => Future(runBatch(b))))
    val result   = Await.result(fBatches, 5 seconds)
    val end      = Instant.now()
    val delayMs  = end.toEpochMilli - start.toEpochMilli
    assert(delayMs < 2000)
  }

  "Reducing future result" must "be 0" in {
    val f = (0 to 4).map(i => Future(i))
    // bad: do nothing ! See BetterFutureTesting
    Future.reduceLeft(f)((r1, r2) => r1 + r2).map(finalResut => assert(finalResut == 0))
  }
}
