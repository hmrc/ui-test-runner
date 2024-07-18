import Dependencies.{accessibilityAssessmentExtensionVersion, hmrcArtifactReleases}
import sbt.*
import Keys.*

import java.net.URL
import java.io.File
import java.nio.file.Files
import java.util.zip.ZipInputStream

// Define the zip file URL and the target directory
val accessibilityAssessmentExtensionZipFile =
  s"accessibility-assessment-extension-$accessibilityAssessmentExtensionVersion.zip"
val zipUrl                                  =
  s"https://artefacts.tax.service.gov.uk/artifactory/$hmrcArtifactReleases" +
    s"/uk/gov/hmrc/accessibility-assessment-extension/" +
    s"$accessibilityAssessmentExtensionVersion/" +
    accessibilityAssessmentExtensionZipFile
val targetDir                               = "src/main/resources"

// Task to download the zip file
val downloadZip = taskKey[File]("Downloads the accessibility-assessment-extension zip file")

downloadZip := {
  val url     = new URL(zipUrl)
  val zipFile = new File(targetDir, accessibilityAssessmentExtensionZipFile)
  if (!zipFile.exists()) {
    val in = url.openStream()
    try
      Files.copy(in, zipFile.toPath)
    finally
      in.close()
  }
  zipFile
}

// Task to extract the zip file
val extractZip = taskKey[Seq[File]]("Extracts the accessibility-assessment-extension zip file")

extractZip := {
  val zipFile    = downloadZip.value
  val extractDir = new File(targetDir, "accessibility-assessment-extension")
  if (!extractDir.exists()) extractDir.mkdirs()

  val zipStream = new ZipInputStream(Files.newInputStream(zipFile.toPath))
  try
    Stream.continually(zipStream.getNextEntry).takeWhile(_ != null).foreach { entry =>
      if (!entry.isDirectory) {
        val outputFile   = new File(extractDir, entry.getName)
        val outputStream = Files.newOutputStream(outputFile.toPath)
        try
          Stream.continually(zipStream.read()).takeWhile(_ != -1).foreach(outputStream.write)
        finally
          outputStream.close()
      }
    }
  finally {
    val zipFile = new File(targetDir, accessibilityAssessmentExtensionZipFile)
    if (zipFile.exists()) {
      zipFile.delete()
    }
    zipStream.close()
  }

  extractDir.listFiles().toSeq
}

lazy val library = (project in file("."))
  .disablePlugins(JUnitXmlReportPlugin) // Required to prevent https://github.com/scalatest/scalatest/issues/1427
  .settings(
    name := "ui-test-runner",
    majorVersion := 0,
    scalaVersion := "2.13.13",
    crossScalaVersions := Seq("2.13.13", "3.3.3"),
    libraryDependencies ++= Dependencies.compile,
    (Compile / unmanagedClasspath) ++= extractZip.value.map(Attributed.blank)
  )
