import sbt._

object Dependencies {

	val enumeratumVersion = "1.5.13"
	val enumeratum = Seq(
	  "com.beachape" %% "enumeratum" % enumeratumVersion,
	  "com.beachape" %% "enumeratum-circe" % enumeratumVersion
	)
	val scalatest =  Seq(
		"org.scalatest" %% "scalatest" % "3.0.8" % "it, test"
	)

	val log4j = Seq(
		"log4j" % "log4j" % "1.2.17"
	)

}
