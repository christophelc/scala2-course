package com.example

object SparkMain {

  def genSeq(max: Int): Seq[Int] = {
    require(max >= 0)
    println("Generating sequence...")
    1 until max
  }
  def main(args: Array[String]): Unit = {
    println("lazy list")
    lazy val a: Seq[Int] = genSeq(10)
    println("nothing yet happened")
    println(a)
    println("show it again: not evaluated twice")
    println(a)

    println()

    val b = genSeq(20)
    println("already displayed")
    println(b)

    println()

    println("def is evalated each time")
    def f: Seq[Int] = genSeq(10)
    println(f)
    println(f)

    println()

    val lazyList = (1 to 10)
    println(lazyList)

    println()

    val eagerList = (1 to 5).toList
    println(eagerList)

    println()

    // TODO: test for RandomGen: define & implement tests
    val random = new RandomGen(max = 10, size = 5)
    println(random.gen)
  }
}
