import sbt.*

object Dependencies {

  val compile: Seq[ModuleID] = Seq(
    "com.typesafe"                % "config"        % "1.4.3",
    "com.typesafe.play"          %% "play-json"     % "2.10.4",
    "com.typesafe.scala-logging" %% "scala-logging" % "3.9.5",
    "com.vladsch.flexmark"        % "flexmark-all"  % "0.64.8",
    "org.scalatest"              %% "scalatest"     % "3.2.18",
    "org.seleniumhq.selenium"     % "selenium-java" % "4.18.1",
    "org.slf4j"                   % "slf4j-simple"  % "2.0.9"
  )

}
