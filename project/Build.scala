import sbt._
import Keys._

object ProjectBuild extends Build {

  val baseSettings = Defaults.defaultSettings ++ Seq(
    organization := "org.skyluc",
    version      := "0.1.0-SNAPSHOT",
    scalaVersion := "2.10.0",
    resolvers += "spray repo" at "http://repo.spray.io",
    libraryDependencies ++= Seq(
      "io.spray" % "spray-can" % "1.1-M7",
      "com.typesafe.akka" %% "akka-actor" % "2.1.0",
      "com.typesafe.akka" %% "akka-slf4j" % "2.1.0",
      "org.scalatest" % "scalatest_2.10" % "2.0.M5b" % "test",
      "ch.qos.logback" % "logback-classic" % "0.9.28" % "runtime"
    ),
    sourceDirectories in Compile <<= baseDirectory ( (base : File) => Seq( base / "src" / "main" / "scala")),
    sourceDirectories in Test <<= baseDirectory ( (base : File) => Seq( base / "src" / "test" / "scala")),
    mainClass := Some("org.skyluc.deluge.Server"),
    fork in run := true,
    connectInput in run := true
  )

  lazy val project = Project ("deluge", file("."), settings = baseSettings)

}
