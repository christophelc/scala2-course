package com.example.model

import java.time.{ Clock, LocalDateTime }

case class Plane(id: Int)
case class Flight(flightNo: String, flightStatus: FlightStatus, plane: Plane)

case class TowerControl(town: Town, flights: Seq[Flight] = Nil)(clock: Clock) {
  // first implementation
  private def updateFlight(flightNo: String): Flight =
    flights.find(flight => flight.flightNo == flightNo) match {
      case None => throw new RuntimeException(s"No flight $flightNo found")
      case Some(flight) =>
        flight.flightStatus match {
          case FlightStatus.Flying(_, toTown, _) =>
            flight.copy(
              flightStatus = FlightStatus.NotFlying(location = toTown)
            )
          case _ => throw new RuntimeException(s"£Internal error.  The flight $flightNo should be flying.")
        }
    }
  def schedule(time: LocalDateTime): Plane = Plane(
    id = flights.map(_.plane.id).max + 1
  )
  def flightDone(flightNo: String) =
    this.copy(
      town = this.town,
      flights = flights.filter(_.flightNo != flightNo) :+ updateFlight(flightNo)
    )(clock)
  def planesFlying: Seq[Plane]    = flights.filter(_.flightStatus.isFlying).map(_.plane)
  def planesAvailable: Seq[Plane] = flights.filter(!_.flightStatus.isFlying).map(_.plane)
}
