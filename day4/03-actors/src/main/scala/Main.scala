package com.example

import com.example.model.actor.TowerControlActors
import com.example.model.actor.TowerControlActors.{ KillAll, ListTowers }

import java.time.Clock

object Main {

  def main(args: Array[String]): Unit = {
    val clock              = Clock.systemUTC()
    val towerControlsActor = TowerControlActors(clock)

    towerControlsActor ! ListTowers
    Thread.sleep(3000)
    println("Destroying actors")
    towerControlsActor ! KillAll
    Thread.sleep(3000)
    println("Done")
  }
}
