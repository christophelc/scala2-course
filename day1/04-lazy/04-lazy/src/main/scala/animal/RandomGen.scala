package com.example

import scala.util.Random

class RandomGen(max: Int, size: Int) {
  lazy val r = new Random()

  def gen: Seq[Int] = for (_ <- 1 until size) yield math.abs(r.nextInt()) % max
}
