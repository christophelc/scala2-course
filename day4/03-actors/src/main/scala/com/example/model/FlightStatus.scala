package com.example.model

import enumeratum.{ Enum, EnumEntry }

sealed trait FlightStatus extends EnumEntry {
  def isFlying: Boolean = false
}

object FlightStatus extends Enum[FlightStatus] {
  def values = findValues

  case class Flying(fromTown: Town, toTown: Town, distanceLeft: Int) extends FlightStatus {
    override def isFlying: Boolean = true
  }
  case class NotFlying(location: Town) extends FlightStatus
}
