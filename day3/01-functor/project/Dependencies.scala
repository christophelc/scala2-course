import sbt._

object Dependencies {

	val cats = Seq(
	  "org.typelevel" %% "cats-core" % "2.0.0"
	)

	val scalatest =  Seq(
		"org.scalatest" %% "scalatest" % "3.0.8" % "it, test"
	)

	val log4j = Seq(
		"log4j" % "log4j" % "1.2.17"
	)

}
