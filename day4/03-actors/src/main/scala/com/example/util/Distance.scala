package com.example.util

import com.example.model.Town

object Distance {
  // in real life, get from a bdd
  // the request can fail
  def fromParis(t: Town): Int = {
    val d = Seq(
      Town.Paris     -> 0,
      Town.Lyon      -> 500,
      Town.Lille     -> 300,
      Town.Nantes    -> 300,
      Town.Marseille -> 800
    )
    // first implementation
    require(d.map(_._1).contains(t))
    d.find(_._1 == t).map(_._2).getOrElse(0)
  }

  def betweenTowns(t1: Town, t2: Town): Int =
    // approximation
    if (t1 == t2) 0 else fromParis(t1) + fromParis(t2)
}
