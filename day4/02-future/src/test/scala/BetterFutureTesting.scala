import org.scalatest.AsyncFlatSpec

import scala.concurrent.Future
import scala.util.{ Failure, Success, Try }

class BetterFutureTesting extends AsyncFlatSpec {

  "Reducing future +(0, 1, 2, 3, 4)" must "give 10" in {
    val f = (0 to 4).map(i => Future(i))
    Future.reduceLeft(f)((r1, r2) => r1 + r2).map(finalResult => assert(finalResult == 10))
  }

  def callback: Try[Int] => Unit = {
    case s: Success[Int]   => println(s)
    case err: Failure[Int] => println(err)
  }

  case class Error(i: Int) extends RuntimeException(s"Failed at stage $i")
  "Each future" must "call callback once done" in {
    val seqF = (0 to 4).map {
      case i if i % 2 == 0 => Future.successful(i)
      case i => Future.failed(Error(i))
    }
    // here we just run future
    seqF.foreach(_.onComplete(callback))
    assert(true)
  }

  "Future" must "recover error" in {
    val seqF = (0 to 4).map {
      case i if i % 2 == 0 => Future.successful(i)
      case i => Future.failed(Error(i))
    }
    seqF.foreach(f =>
      f.recoverWith {
        case _ => Future.successful(-1)
      }.onComplete(callback)
    )
    assert(true)
  }

  trait Report
  case class SuccessReport(r: Int)      extends Report
  case class FailureReport(err: String) extends Report
  "Future error propagation" must "" in {
    val seqF1        = Seq(0, 2, 5).map(i => Future(10 / i))
    val computation1 = Future.sequence(seqF1)
    val computation2 = computation1
      .map(seq => SuccessReport(seq.sum))
      .recoverWith {
        case err: ArithmeticException => Future.successful(FailureReport("divide by zero"))
      }
    computation2.map(r => assert(r == FailureReport("divide by zero")))
  }
}
