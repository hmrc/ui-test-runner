lazy val library = (project in file("."))
  .disablePlugins(JUnitXmlReportPlugin) // Required to prevent https://github.com/scalatest/scalatest/issues/1427
  .settings(
    name := "ui-test-runner",
    version := "0.24.0",
    scalaVersion := "2.13.13",
    libraryDependencies ++= Dependencies.compile
  )
