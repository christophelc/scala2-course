package com.example.model.actor

import akka.actor.{ Actor, ActorLogging, ActorRef, ActorSystem, PoisonPill, Props }
import com.example.model.actor.TowerControlActor.{ Kill, ScheduleFlight }
import com.example.model.{ TowerControl, Town }

import java.time.Clock

object TowerControlActor {
  case class ScheduleFlight(destination: Town, time: java.time.LocalDateTime)
  case object Kill

  def apply(town: Town)(clock: Clock): Props =
    Props(new TowerControlActor(TowerControl(town)(clock)))
}

class TowerControlActor(var towerControl: TowerControl) extends Actor with ActorLogging {
  log.info(s"TowerControlActor ${towerControl.town} on")

  def receive: PartialFunction[Any, Unit] = {
    case ScheduleFlight(destination: Town, time: java.time.LocalDateTime) =>
      towerControl = towerControl
      log.info(s"received  $destination $time")

    case Kill =>
      println(s"Killing TowerControlActor ${towerControl.town}")
      val sender = this.sender()
      self ! PoisonPill
  }

}
