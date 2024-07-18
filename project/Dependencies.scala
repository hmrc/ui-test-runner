import sbt.*

object Dependencies {
  val hmrcArtifactReleases                    = "hmrc-releases"
  val accessibilityAssessmentExtensionVersion = "1.0.0"

  val compile: Seq[ModuleID] = Seq(
    "ch.qos.logback"              % "logback-classic" % "1.5.4",
    "com.typesafe"                % "config"          % "1.4.3",
    "com.typesafe.scala-logging" %% "scala-logging"   % "3.9.5",
    "com.vladsch.flexmark"        % "flexmark-all"    % "0.64.8",
    "org.scalatest"              %% "scalatest"       % "3.2.18",
    "org.seleniumhq.selenium"     % "selenium-java"   % "4.18.1"
  )
}
