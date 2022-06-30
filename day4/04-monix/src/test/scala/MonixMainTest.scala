import com.axa.go.wax.MonixSpec
import monix.eval.Task

import scala.concurrent.Await
import monix.execution.{Cancelable, Scheduler}
import monix.execution.Scheduler.Implicits.global
import scala.concurrent.duration._

/**
 * see first example from official documentation
 * all the other examples are extracted from:
 *  https://levelup.gitconnected.com/asynchronous-boundary-in-monix-task-900995ba8a28
 */
class MonixMainTest extends MonixSpec {

  lazy val sc1 = Scheduler.singleThread("single") // backed by SingleThreadExecutor
  lazy val sc2 = Scheduler.fixedPool("fixed", poolSize = 10) // backed by FixedThreadExecutor
  lazy val sc3 = Scheduler.computation(parallelism = 10, name="cpu-bound") // backed by ForkJoinPool
  lazy val scIo = Scheduler.io("io-bound") // backed by CachedThreadPool

  val printCurrentThread: Task[Unit] = Task { println(s"current thread: ${Thread.currentThread().getName}") }
  def lightTask(name: String, n: Int): Task[Int] =
    printCurrentThread *> Task.eval {
      println(s"task: $name")
      n
    }

  def heavyTask(name: String, n: Int): Task[Int] =
    printCurrentThread *> Task.eval {
      println(s"task: $name, number: $n")
      Thread.sleep(1000)
      n
    }

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

  "Scheduler" must "be able to shift context" in {
    val task = Task.shift *> printCurrentThread *> Task.shift(scIo) *> printCurrentThread
    val future = task.runToFuture
    Await.result(future, 5.seconds)
    // current thread: scala-execution-context-global-14
    // current thread: blocking-15
    assert(true)
  }

  "Three tasks" must "executed in the main thread" in {
    def program(): Task[Int] = {
      for {
        num1 <- lightTask("light1", 1)
        res1 <- heavyTask("heavy1", num1)
        res2 <- lightTask("light2", 2)
      } yield res1 + res2
    }
    program().runSyncUnsafe()
    assert(true)
  }

  "heavyTask1 and light2 tasks" must "be run in the default thread pool contrary to light1 that is executed in main thread" in {
    def programAsync(): Task[Int] = {
      for {
        num1 <- lightTask("light1", 1)
        // <-> res1 <- Task.shift *> heavyTask("heavy1", num1)
        // <-> res1 <- Task.evalAsync(heavyTask("heavy1", num1))
        res1 <- heavyTask("heavy1", num1).executeAsync // shift before execution to the default context
        res2 <- lightTask("light2", 2)
      } yield res1 + res2
    }
    // main thread is not on the default thread pool
    programAsync().runSyncUnsafe()
    assert(true)
  }

  "heavyTask1 and light2 tasks" must "be run in io-bound thread pool contrary to light1 task executed in main thread" in {
    def programWithAsyncBoundary(): Task[Int] = {
      for {
        num1 <- lightTask("light1", 1).asyncBoundary(scIo) // shift after execution to the blocking thread pool
        res1 <- heavyTask("heavy1", num1)
        res2 <- lightTask("light2", 2) // light task running in the block thread pool too !
      } yield res1 + res2
    }
    programWithAsyncBoundary().runSyncUnsafe()
  }

  "heavyTask1" must "be run in io bound thread pool whereas light1 is in main thread and light2 is in default thread pool" in {
    def programWithAsyncBoundaryShiftBack(): Task[Int] = {
      for {
        num1 <- lightTask("light1", 1).asyncBoundary(scIo)
        res1 <- heavyTask("heavy1", num1)
        _ <- Task.shift
        res2 <- lightTask("light2", 2)
      } yield res1 + res2
    }
    def programWithAsyncBoundaryShiftBack2(): Task[Int] = {
      for {
        num1 <- lightTask("light1", 1).asyncBoundary(scIo)
        res1 <- heavyTask("heavy1", num1).asyncBoundary
        res2 <- lightTask("light2", 2)
      } yield res1 + res2
    }
    programWithAsyncBoundaryShiftBack2().runSyncUnsafe()
  }

  "Heavy1 and heavy 2" must "be run in io bound thread pool" in {
    def programWithExecuteOn(): Task[Int] = {
      for {
        num1 <- lightTask("light1", 1) // main
        res1 <- heavyTask("heavy1", num1).executeOn(scIo) // blocking-13
        num2 <- lightTask("light2", 2) // scala-execution-context-14
        res2 <- heavyTask("heavy2", num2).executeOn(scIo) // blocking-13
      } yield res1 + res2
    }
    programWithExecuteOn().runSyncUnsafe()
  }

  "Two task" must "be executed in parallel" in {
    def programParallel()(implicit sc: Scheduler): Task[Int] = {
      val p1 = lightTask("light1", 1).flatMap { num1 =>
        heavyTask("heavy1", num1).executeOn(scIo)
      }
      val p2 = lightTask("light1", 2).flatMap { num2 =>
        heavyTask("heavy2", num2).executeOn(scIo)
      }
      Task.parMap2(p1, p2)(_ + _)
    }
    assert(programParallel().runSyncUnsafe() == 3)

  }
}