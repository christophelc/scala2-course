package com.example.model.actor

import akka.actor.{ Actor, ActorLogging, Props }
import com.example.model.Plane

case object PlaneActor {
  def apply(plane: Plane): Props = Props(new PlaneActor(plane))
}

case class PlaneActor(plane: Plane) extends Actor with ActorLogging {
  case class AckScheduleFlight(planeId: String)

  def receive: PartialFunction[Any, Unit] =
    ???
}
