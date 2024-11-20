import sbt.*

object Dependencies {

  val compile: Seq[ModuleID] = Seq(
    "ch.qos.logback"              % "logback-classic" % "1.5.6",
    "com.typesafe"                % "config"          % "1.4.3",
    "com.typesafe.scala-logging" %% "scala-logging"   % "3.9.5",
    "com.vladsch.flexmark"        % "flexmark-all"    % "0.64.8",
    "org.scalatest"              %% "scalatest"       % "3.2.19",
    "org.seleniumhq.selenium"     % "selenium-java"   % "4.24.0"
  )
}
