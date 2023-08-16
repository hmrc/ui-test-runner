import sbt._

object Dependencies {

  val compile: Seq[ModuleID] = Seq(
    "com.typesafe"                % "config"        % "1.4.2",
    "com.typesafe.play"          %% "play-json"     % "2.9.4",
    "com.typesafe.scala-logging" %% "scala-logging" % "3.9.5",
    "com.vladsch.flexmark"        % "flexmark-all"  % "0.62.2",
    "org.scalatest"              %% "scalatest"     % "3.2.16",
    "org.seleniumhq.selenium"     % "selenium-java" % "4.11.0"
  )

  val test: Seq[ModuleID] = Seq(
    "ch.qos.logback" % "logback-classic"         % "0.9.28",
    "org.mockito"   %% "mockito-scala-scalatest" % "1.16.37" % Test
  )
}
