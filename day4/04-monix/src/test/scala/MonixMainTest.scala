import com.axa.go.wax.MonixSpec
import monix.eval.Task

import scala.concurrent.Await
import monix.eval._
import monix.execution.Cancelable
import monix.execution.Scheduler.Implicits.global

import scala.concurrent.Await
import scala.concurrent.duration._

class MonixMainTest extends MonixSpec {

  "Task(1+1)" must "equals 2" in {
    // from official documentation
    val task = Task {
      1 + 1
    }
    val future = task.runToFuture
    val r = Await.result(future, 5.seconds)
    assert(r == 2)

  }
  "Tick" must "produce 5 elements" in {
    // extracted from official documentation
    import monix.reactive._
    // Nothing happens here, as observable is lazily
    // evaluated only when the subscription happens!`
    val observer = new TestObserver[Long]
    val tick: Observable[Long] = {
      Observable.interval(100.millisecond)
        // common filtering and mapping
        .filter(_ % 2 == 0)
        .map(_ * 2)
        // any respectable Scala type has flatMap, w00t!
        .flatMap(x => Observable.fromIterable(Seq(x, x)))
        // only take the first 5 elements, then stop
        .take(5)
        // to print the generated events to console
        .dump("Out")
    }
    // Execution happens here, after subscribe
    val cancelable: Cancelable = tick.subscribe(observer)
    // 0: Out-->0
    // 1: Out-->0
    // 2: Out-->4
    // 3: Out-->4
    // 4: Out-->8
    // 5: Out completed

    Thread.sleep(500)
    // Or maybe we change our mind
    cancelable.cancel()
    observer.received mustBe Vector(0, 0, 4, 4, 8)
  }
}