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

    log.info(s"Building, packaging, and base64 encoding browser extension: $browserExtension")

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

    val encodedBrowserExtension =
      (Compile / resourceManaged).value / "browser-extensions" / browserExtension

    IO.withTemporaryDirectory { tmpDir =>
      val packagedBrowserExtension = tmpDir / s"$browserExtension.crx"

      IO.zip(browserExtensionFiles, packagedBrowserExtension, None)

      IO.write(
        encodedBrowserExtension,
        Base64.getEncoder.encodeToString(IO.readBytes(packagedBrowserExtension))
      )
    }

    log.info(
      s"Browser extension built, packaged, and encoded ${encodedBrowserExtension.relativeTo(baseDirectory.value).getOrElse(encodedBrowserExtension)}"
    )

    // We encode the browser extension because it makes using it simpler, otherwise we'd have to unzip
    // the files into a temporary directory, whereas when it's base64 encoded we can just read it when
    // initializing the browser
    encodedBrowserExtension
  }
}
