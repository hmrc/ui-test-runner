import scala.sys.process.Process
import java.util.Base64
import sbt.Keys._

lazy val library = (project in file("."))
  .disablePlugins(JUnitXmlReportPlugin) // Required to prevent https://github.com/scalatest/scalatest/issues/1427
  .settings(
    name := "ui-test-runner",
    majorVersion := 0,
    scalaVersion := "2.13.13",
    crossScalaVersions := Seq("2.13.13", "3.3.3"),
    libraryDependencies ++= Dependencies.compile,
    Compile / resourceGenerators += packageBrowserExtensions.taskValue
  )

val packageBrowserExtensions = Def.task {
  val log = streams.value.log

  val browserExtensions = IO.listFiles(baseDirectory.value / "browser-extensions").toSeq

  browserExtensions.map { browserExtensionSource =>
    val browserExtension = browserExtensionSource.name

    log.info(s"Building and packaging browser extension: $browserExtension")

    val buildScript = browserExtensionSource / "build.sh"
    if (buildScript.exists) {
      log.info("Running build script")
      val exitCode = Process(buildScript.absolutePath, browserExtensionSource).!
      if (exitCode != 0) {
        throw new MessageOnlyException(s"Failed to build $browserExtension browser extension")
      }
    } else {
      log.info("No build script found")
    }

    val browserExtensionBuild = browserExtensionSource / "dist"

    val browserExtensionFiles = IO.listFiles(browserExtensionBuild).map { file =>
      (file, browserExtensionBuild.toURI.relativize(file.toURI).getPath)
    }

    val packagedBrowserExtension =
      (Compile / resourceManaged).value / "browser-extensions" / s"$browserExtension.crx"

    IO.zip(browserExtensionFiles, packagedBrowserExtension, None)

    log.info(
      s"Browser extension built and packaged ${packagedBrowserExtension.relativeTo(baseDirectory.value).get}"
    )

    packagedBrowserExtension
  }
}
