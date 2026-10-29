import sbt.*

object Dependencies {

  val compile: Seq[ModuleID] = Seq(
    "ch.qos.logback"              % "logback-classic" % "1.6.5",
    "com.typesafe"                % "config"          % "1.4.9",
    "com.typesafe.scala-logging" %% "scala-logging"   % "3.9.6",
    "com.vladsch.flexmark"        % "flexmark-all"    % "0.64.8",
    "org.scalatest"              %% "scalatest"       % "3.2.20",
    "org.seleniumhq.selenium"     % "selenium-java"   % "4.47.0",
    "org.mockito"                 % "mockito-core"    % "5.24.0" % Test
  )
}
