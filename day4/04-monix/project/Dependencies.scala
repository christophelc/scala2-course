import sbt._

object Dependencies {

	val scalatest =  Seq(
		"org.scalatest" %% "scalatest" % "3.0.8" % "test"
	)

	val log4j = Seq(
		"log4j" % "log4j" % "1.2.17"
	)

	val sparkVersion = "3.0.1"

	val spark = Seq(
		"org.apache.spark" %% "spark-core" % sparkVersion % "provided",
		"org.apache.spark" %% "spark-sql" % sparkVersion % "provided",
	)

	val delta =  Seq(
		"io.delta" %% "delta-core" % "0.7.0" % "provided"
	)

	val monix = Seq(
		"io.monix" %% "monix" % "3.4.0"
	)

	val hadoopForLocalExecution = Seq(
		"org.apache.hadoop" % "hadoop-hdfs" % sparkVersion % "provided",
		"org.apache.hadoop" % "hadoop-common" %  sparkVersion % "provided",
		"org.apache.hadoop" % "hadoop-hdfs-client" %  sparkVersion % "provided",
	)

}
