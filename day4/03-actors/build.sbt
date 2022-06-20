import Dependencies._

name := "FirstApp"

version := "0.1-snapshot"

scalaVersion := "2.12.12"

lazy val root = (project in file("."))
 .settings(
   scalafmtOnCompile := true,
 )
  .configs(IntegrationTest)
  .settings(Defaults.itSettings)

libraryDependencies ++=
    scalatest ++
    actors ++
    enumeratum ++
    logger

Compile/run := Defaults.runTask(Compile / fullClasspath, Compile/run/mainClass, Compile/run/runner).evaluated

scalacOptions ++= Seq(
  "-Ywarn-unused",
  "-Ywarn-dead-code",
  "-Ywarn-value-discard",
  "-Ypartial-unification"
)

javacOptions ++= Seq("-source", "1.8", "-target", "1.8", "-Xlint")

fork / run := true
