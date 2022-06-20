package com.example.model.actor

import akka.actor.{ Actor, ActorLogging, ActorRef, ActorSystem, PoisonPill, Props }
import com.example.model.actor.TowerControlActor.Kill
import com.example.model.actor.TowerControlActors.{ system, KillAll, ListTowers }
import com.example.model.{ TowerControl, Town }

import java.time.Clock

object TowerControlActors {
  val system = ActorSystem("towerControl")

  def apply(clock: Clock): ActorRef = {
    val towns: Seq[Town] = Town.values
    system.actorOf(Props(new TowerControlsActor(towns)(clock)), "towerControlsActor")
  }

  case object KillAll
  case object ListTowers
}

class TowerControlsActor(towns: Seq[Town])(clock: Clock) extends Actor with ActorLogging {
  var towerControls: Map[Town, ActorRef] = buildTowerControls()

  def buildTowerControls(): Map[Town, ActorRef] = {
    val towerActors: Map[Town, ActorRef] =
      towns
        .map(town => town -> context.actorOf(TowerControlActor(town)(clock)))
        .toMap
    towerActors
  }

  def receive: PartialFunction[Any, Unit] = {
    case ListTowers =>
      log.info(s"""
                  |Received 'ListTowers' command.
                  |Listing tower controls:
                  |${towerControls.keys.toSeq.sortBy(_.entryName)}
                  |""".stripMargin)
    case KillAll =>
      //towerControls.values.foreach(actor => context.stop(actor))
      // with an explicit Kill message
      towerControls.values.foreach(actor => actor ! Kill)
      system.terminate()
  }
}
