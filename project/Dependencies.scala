import sbt._

object Dependencies {

  val compile: Seq[ModuleID] = Seq(
    "com.typesafe"                % "config"        % "1.4.2",
    "com.typesafe.play"          %% "play-json"     % "2.9.4",
    "com.typesafe.scala-logging" %% "scala-logging" % "3.9.5",
    "com.vladsch.flexmark"        % "flexmark-all"  % "0.62.2",
    "org.scalatest"              %% "scalatest"     % "3.2.16",
    "org.seleniumhq.selenium"     % "selenium-java" % "4.9.1",
    "org.slf4j"                   % "slf4j-simple"  % "1.7.36"
  )

}
