import Dependencies._

name := "Spark-standalone"

version := "0.1-snapshot"

scalaVersion := "2.12.12"
val circeVersion = "0.11.1"
val sparkVersion = "3.0.1"
val enumeratumVersion = "1.5.13"
val silencerVersion = "1.7.1"


resolvers += Resolver.sonatypeRepo("releases")
addCompilerPlugin("org.scalamacros" %% "paradise" % "2.1.1" cross CrossVersion.full)

libraryDependencies ++= Seq(
  compilerPlugin("com.github.ghik" % "silencer-plugin" % silencerVersion cross CrossVersion.full),
  "com.github.ghik" % "silencer-lib" % silencerVersion % Provided cross CrossVersion.full
)
libraryDependencies ++=
  spark ++
    delta ++
    monix ++
    scalatest ++
    hadoopForLocalExecution

Compile / run:= Defaults.runTask(fullClasspath in Compile, mainClass in (Compile, run), runner in (Compile, run)).evaluated

assembly / mainClass:= Some("com.example.SparkMain")

scalacOptions ++= Seq(
  "-Ywarn-unused",
  "-Ywarn-dead-code",
  "-Ywarn-value-discard",
  "-Ypartial-unification"
)

javacOptions ++= Seq("-source", "1.8", "-target", "1.8", "-Xlint")

assembly / assemblyMergeStrategy := {
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case x => MergeStrategy.first
}

initialize := {
  val _ = initialize.value
  if (sys.props("java.specification.version") != "1.8")
    sys.error("Java 8 is required for this project.")
}

//assembly / assemblyMergeStrategy := {
//  case PathList("META-INF", "services", "org.Apache.hadoop.fs.FileSystem") => MergeStrategy.filterDistinctLines
//  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
//  case x => MergeStrategy.first
//  case x =>
//    val oldStrategy = (assembly / assemblyMergeStrategy).value
//    oldStrategy(x)
//}

fork / run := true
