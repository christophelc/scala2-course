package com.example.model

import enumeratum.{ Enum, EnumEntry }

sealed trait Town extends EnumEntry
object Town extends Enum[Town] {
  def values = findValues

  case object Paris     extends Town
  case object Lyon      extends Town
  case object Marseille extends Town
  case object Lille     extends Town
  case object Nantes    extends Town
}
