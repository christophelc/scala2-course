import com.axa.go.wax.MonixSpec
import monix.reactive.Observable

class MonixObservableTest extends MonixSpec {

  import monix.execution.Scheduler.Implicits.global

  "Observable.evalOnce" must "memoize return result" in {
    val obs = Observable.evalOnce {
      println("Effect");
      "Hello!"
    }
    // obs: monix.reactive.Observable[String] = EvalOnceObservable@3233e694
    val task = obs.foreachL(println)
    // task: monix.eval.Task[Unit] = Task.Async$1782722529

    task.runToFuture
    //=> Effect
    //=> Hello!

    // Result was memoized on the first run!
    task.runToFuture.foreach(println)
    //=> Hello!

    assert(true)
  }

  "Observable.evalOnce with flatMap" must "memoize initial return result" in {
    val obs = Observable.evalOnce {
      println("Effect2: should appear only once");
      "Hello2!"
    }
    val obs2 = Observable.evalOnce {
      println("effect obs2")
      "result:2"
    }
    val obs3 = Observable.evalOnce {
      println("effect: obs3")
      "result:3"
    }
    val obsF2 = obs.flatMap(_ => obs2)
    val obsF3 = obs.flatMap(_ => obs3)
    val taskF2 = obsF2.foreachL(println)
    val taskF3 = obsF3.foreachL(println)
    taskF2.runToFuture
    taskF2.runToFuture.foreach(println)
    println("----")
    taskF3.runToFuture
    taskF3.runToFuture.foreach(println)
  }
}