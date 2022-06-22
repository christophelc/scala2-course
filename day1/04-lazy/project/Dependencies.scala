import sbt._

object Dependencies {

	val scalatest =  Seq(
		"org.scalatest" %% "scalatest" % "3.0.8" % "it, test"
	)

	val log4j = Seq(
		"log4j" % "log4j" % "1.2.17"
	)

}
