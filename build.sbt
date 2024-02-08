lazy val library = (project in file("."))
  .disablePlugins(JUnitXmlReportPlugin) // Required to prevent https://github.com/scalatest/scalatest/issues/1427
  .settings(
    name := "ui-test-runner",
    version := "0.17.0",
    scalaVersion := "2.13.12",
    libraryDependencies ++= Dependencies.compile
  )
