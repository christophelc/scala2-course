package com.example

sealed trait DoorState
sealed trait Open   extends DoorState
sealed trait Closed extends DoorState

case class Door[State <: DoorState]() {
  def open(implicit ev: State =:= Closed) = Door[Open]()
  def close(implicit ev: State =:= Open)  = Door[Closed]()
}
