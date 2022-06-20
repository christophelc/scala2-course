package com.example.model.actor

import akka.actor.typed.Behavior
import akka.actor.typed.javadsl.Behaviors
import com.example.model.actor.SunStatus.{ Day, Night }
import enumeratum.{ Enum, EnumEntry }

sealed trait SunStatus extends EnumEntry
object SunStatus extends Enum[SunStatus] {
  def values = findValues

  case object Day   extends SunStatus
  case object Night extends SunStatus
}

object SunActor {
  val sunActor: Behavior[SunStatus] = Behaviors.setup { context =>
    var state: SunStatus = Night

    Behaviors.receiveMessage {
      case Day =>
        context.getLog.info("Receive 'Day'")
        if (state == SunStatus.Day) {
          context.getLog.info("No change")
          Behaviors.same
        } else {
          if (state == Day) {
            context.getLog.info("Day -> Night")
            state = Night
            Behaviors.same
          } else {
            context.getLog.info("Night -> Day")
            state = Day
            Behaviors.same
          }
        }
    }
  }
}
