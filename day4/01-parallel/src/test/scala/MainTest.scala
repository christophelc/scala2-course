package com.example

import org.scalatest.{ FlatSpec, MustMatchers }

class MainTest extends FlatSpec with MustMatchers {

  def compute = Range(1, 100).sum

  "Seq collection mapping" must "be sequential" in {

    val v = Seq(1, 2, 3)
    val result = v.map { taskId =>
      val result = compute * taskId
      s"$taskId -> $result"
    }
    result must equal(
      Seq(
        "1 -> 4950",
        "2 -> 9900",
        "3 -> 14850"
      )
    )
  }
  "Parallel collection mapping" must "be done in parallel" in {
    val n = 100
    val v = (1 to n)
    val result = v.map { taskId =>
      val result = compute * taskId
      s"$taskId -> $result"
    }
    // we keep the order for map (result assembled in order)
    result must equal(
      (1 to n).map(i => s"$i -> ${i * 4950}")
    )
    // but be careful with not associative operations
    v.par.reduce(_ - _) must not be (v.reduce(_ - _))
  }

  "Parallel pool computation" must "be able to embed serial computation" in {
    def computation(pool: Seq[Int]): Int = pool.map(i => i % 10).reduceLeft(_ - _)

    val n        = 100
    val poolSize = 10
    val pools    = (1 to n).grouped(poolSize).toSeq.par // a parallel collection of Seq
    val result   = pools.map(pool => computation(pool)) // pool is a Seq
    val expected = computation((1 to 10))
    assert(result.forall(_ == expected))
  }

}
