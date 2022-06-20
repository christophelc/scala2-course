package com.example.model.animal

import enumeratum.{ Enum, EnumEntry }

// example: https://www.baeldung.com/scala/enumeratum
sealed trait Country extends EnumEntry

object Country extends Enum[Country] {
  case object Germany extends Country
  case object India   extends Country
  override val values = findValues
}
