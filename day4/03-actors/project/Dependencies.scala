import sbt._

object Dependencies {

	val AkkaVersion = "2.6.19"
	val actors = Seq(
		"com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion,
		"com.typesafe.akka" %% "akka-actor-testkit-typed" % AkkaVersion % Test
	)
	val enumeratumVersion = "1.5.13"
	val enumeratum = Seq(
		"com.beachape" %% "enumeratum" % enumeratumVersion,
		"com.beachape" %% "enumeratum-circe" % enumeratumVersion
	)
	val scalatest =  Seq(
		"org.scalatest" %% "scalatest" % "3.0.8" % "it, test"
	)

  val LogbackVersion = "1.2.9"
  val logger = Seq(
    "com.typesafe.akka" %% "akka-slf4j" % AkkaVersion,
    "ch.qos.logback" % "logback-classic" % LogbackVersion
  )

}
