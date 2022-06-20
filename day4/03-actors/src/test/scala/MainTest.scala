package com.example

import akka.actor.typed.ActorSystem
import com.example.model.actor.{ SunActor, SunStatus }
import org.scalatest.{ FlatSpec, MustMatchers }
import akka.actor.testkit.typed.CapturedLogEvent
import akka.actor.testkit.typed.Effect._
import akka.actor.testkit.typed.scaladsl.BehaviorTestKit
import akka.actor.testkit.typed.scaladsl.TestInbox
import akka.actor.typed._
import org.slf4j.event.Level

class MainTest extends FlatSpec with MustMatchers {

  "Behaviour actor" must "start and terminate without error" in {
    val sunActorSystem = ActorSystem(SunActor.sunActor, "SunActor")
    sunActorSystem ! SunStatus.Day
    Thread.sleep(3000)
    sunActorSystem.terminate()
  }

  "Behaviour actor" must "change state" in {
    val testKit = BehaviorTestKit(SunActor.sunActor)
    testKit.run(SunStatus.Day)
    testKit.logEntries() must be(
      Seq(
        CapturedLogEvent(
          Level.INFO,
          "Receive 'Day'"
        ),
        CapturedLogEvent(
          Level.INFO,
          "Night -> Day"
        )
      )
    )
  }
}
