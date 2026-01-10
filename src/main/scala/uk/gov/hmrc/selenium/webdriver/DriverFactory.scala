/*
 * Copyright 2023 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.selenium.webdriver

import com.typesafe.scalalogging.LazyLogging
import org.openqa.selenium.{MutableCapabilities, Proxy, WebDriver}
import org.openqa.selenium.chrome.{ChromeDriver, ChromeOptions}
import org.openqa.selenium.edge.{EdgeDriver, EdgeOptions}
import org.openqa.selenium.firefox.{FirefoxDriver, FirefoxOptions}
import org.openqa.selenium.logging.{LogType, LoggingPreferences}
import uk.gov.hmrc.selenium.webdriver.DriverFactory.BrowserExtensions
import uk.gov.hmrc.uitestrunner.config.TestRunnerConfig

import java.io.File
import java.nio.file.{Files, StandardCopyOption}
import scala.jdk.CollectionConverters.MapHasAsJava

class DriverFactory extends LazyLogging {

  private val edgeBrowserVersion    = TestRunnerConfig.browserEdgeVersion
  private val firefoxBrowserVersion = TestRunnerConfig.browserFirefoxVersion
  private val chromeBrowserVersion  = TestRunnerConfig.browserChromeVersion

  def initialise(): WebDriver =
    sourceBrowserBinariesFromArtifactory {
      TestRunnerConfig.browserType match {
        case Some("chrome")  => new ChromeDriver(chromeOptions())
        case Some("edge")    => new EdgeDriver(edgeOptions())
        case Some("firefox") => new FirefoxDriver(firefoxOptions())
        case Some(browser)   => throw DriverFactoryException(s"Browser '$browser' is not supported.")
        case None            => throw DriverFactoryException("System property 'browser' is required but was not defined.")
      }
    }

  private def sourceBrowserBinariesFromArtifactory(initWebDriver: => WebDriver): WebDriver = {
    if (!TestRunnerConfig.downloadBrowsersFromArtifactory) {
      logger.info("Artifactory download disabled - using default browser binary sources")
      return initWebDriver
    }

    val userHasSetMirrorUrls =
      sys.props.contains("SE_BROWSER_MIRROR_URL") ||
        sys.props.contains("SE_DRIVER_MIRROR_URL") ||
        sys.env.contains("SE_BROWSER_MIRROR_URL") ||
        sys.env.contains("SE_DRIVER_MIRROR_URL")

    if (userHasSetMirrorUrls) {
      logger.info("User has already configured mirror URLs via system properties - skipping Artifactory configuration")
      return initWebDriver
    }

    val artifactoryBaseUrl = TestRunnerConfig.artifactoryBaseUrl

    if (!isArtifactoryHealthy(artifactoryBaseUrl)) {
      val errorMessage =
        """ERROR: Artefactory unreachable. Are you connected to VPN? Make sure your VPN connection is active""".stripMargin

      logger.error(errorMessage)
      throw DriverFactoryException("Artifactory unreachable. Are you connected to VPN?")
    }

    val (browserMirrorUrl, driverMirrorUrl) = TestRunnerConfig.browserType match {
      case Some("chrome")  =>
        (s"$artifactoryBaseUrl/chrome-browser/", s"$artifactoryBaseUrl/chrome-browser/")
      case Some("firefox") =>
        (s"$artifactoryBaseUrl/firefox-browser/", s"$artifactoryBaseUrl/firefox-browser/")
      case Some("edge")    =>
        (s"$artifactoryBaseUrl/edge-browser/", s"$artifactoryBaseUrl/edge-driver/")
      case _               =>
        (s"$artifactoryBaseUrl/chrome-browser/", s"$artifactoryBaseUrl/chrome-browser/")
    }

    val properties = Map(
      "SE_BROWSER_MIRROR_URL" -> browserMirrorUrl,
      "SE_DRIVER_MIRROR_URL"  -> driverMirrorUrl
    )

    logger.info(s"Configuring Artifactory mirror URLs:")
    logger.info(s"  Browser binary: $browserMirrorUrl")
    logger.info(s"  Driver binary: $driverMirrorUrl")

    try {
      properties.foreach { case (key, value) =>
        System.setProperty(key, value)
        logger.debug(s"Set system property: $key=$value")
      }
      initWebDriver
    } catch {
      case e: org.openqa.selenium.WebDriverException if isArtifactoryConnectionError(e) =>
        logger.error(
          """ERROR: Artefactory unreachable. Are you connected to VPN? Make sure your VPN connection is active""".stripMargin
        )
        throw e
    } finally {
      properties.keys.foreach(System.clearProperty)
      logger.debug("Cleared Artifactory mirror URL system properties")
    }
  }

  private def isArtifactoryHealthy(artifactoryBaseUrl: String): Boolean = {
    val healthcheck = s"$artifactoryBaseUrl/api/system/ping"
    logger.info(s"Checking Artifactory connectivity: $healthcheck")

    val isReachable = {
      val conn = new java.net.URL(healthcheck).openConnection().asInstanceOf[java.net.HttpURLConnection]
      conn.setConnectTimeout(1000)
      try {
        conn.getResponseCode
        true
      } catch {
        case _: java.net.SocketTimeoutException => false
        case _: java.net.UnknownHostException   => false
        case _: java.io.IOException             => false
      }
    }

    if (!isReachable) {
      logger.warn(s"$healthcheck is not reachable")
    } else {
      logger.info("Artifactory is reachable. Proceeding with driver initialization.")
    }

    isReachable
  }

  private def isArtifactoryConnectionError(e: org.openqa.selenium.WebDriverException): Boolean = {
    val message = Option(e.getMessage).getOrElse("")
    message.contains("artefacts.tax.service.gov.uk") ||
    message.contains("error sending request for url")
  }

  private[webdriver] def chromeOptions(): ChromeOptions = {
    val options: ChromeOptions = new ChromeOptions

    options.setBrowserVersion(chromeBrowserVersion)
    logger.info(s"Browser: ${options.getBrowserName} ${options.getBrowserVersion}")

    enableBiDi(options)
    browserLogging(options)
    accessibilityAssessment(options)
    securityAssessment(options)
    downloadDirectory(options)
    headless(options)
    options.addArguments("--disable-search-engine-choice-screen")
    options.addArguments(
      "--disable-features=OptimizationGuideModelDownloading,OptimizationHintsFetching,OptimizationTargetPrediction,OptimizationHints"
    )
    options.addArguments("--disable-features=MediaRouter")
    options.setAcceptInsecureCerts(true)

    options
  }

  private[webdriver] def edgeOptions(): EdgeOptions = {
    val options: EdgeOptions = new EdgeOptions

    options.setBrowserVersion(edgeBrowserVersion)
    logger.info(s"Browser: ${options.getBrowserName} ${options.getBrowserVersion}")

    enableBiDi(options)
    browserLogging(options)
    accessibilityAssessment(options)
    securityAssessment(options)
    downloadDirectory(options)
    headless(options)

    options.setAcceptInsecureCerts(true)

    options
  }

  private[webdriver] def firefoxOptions(): FirefoxOptions = {
    val options: FirefoxOptions = new FirefoxOptions

    options.setBrowserVersion(firefoxBrowserVersion)
    logger.info(s"Browser: ${options.getBrowserName} ${options.getBrowserVersion}")

    enableBiDi(options)
    browserLogging(options)
    accessibilityAssessment(options)
    securityAssessment(options)
    downloadDirectory(options)
    headless(options)

    options.setAcceptInsecureCerts(true)

    options
  }

  private def browserLogging(capabilities: MutableCapabilities): MutableCapabilities = {
    val browserName = capabilities.getBrowserName

    if (!TestRunnerConfig.anyLoggingEnabled) return capabilities

    val logPrefs = new LoggingPreferences()

    browserName match {
      case "chrome" =>
        if (TestRunnerConfig.browserLoggingEnabled)
          logPrefs.enable(LogType.BROWSER, TestRunnerConfig.browserLoggingLevel)
        if (TestRunnerConfig.driverLoggingEnabled)
          logPrefs.enable(LogType.DRIVER, TestRunnerConfig.driverLoggingLevel)
        if (TestRunnerConfig.performanceLoggingEnabled)
          logPrefs.enable(LogType.PERFORMANCE, TestRunnerConfig.performanceLoggingLevel)
        capabilities.setCapability("goog:loggingPrefs", logPrefs)
        logger.info(
          s"Browser logging (Chrome): browser=${TestRunnerConfig.browserLoggingEnabled}(${TestRunnerConfig.browserLoggingLevel}), driver=${TestRunnerConfig.driverLoggingEnabled}(${TestRunnerConfig.driverLoggingLevel}), performance=${TestRunnerConfig.performanceLoggingEnabled}(${TestRunnerConfig.performanceLoggingLevel})"
        )

      case "MicrosoftEdge" =>
        if (TestRunnerConfig.browserLoggingEnabled)
          logPrefs.enable(LogType.BROWSER, TestRunnerConfig.browserLoggingLevel)
        if (TestRunnerConfig.driverLoggingEnabled)
          logPrefs.enable(LogType.DRIVER, TestRunnerConfig.driverLoggingLevel)
        if (TestRunnerConfig.performanceLoggingEnabled)
          logPrefs.enable(LogType.PERFORMANCE, TestRunnerConfig.performanceLoggingLevel)
        capabilities.setCapability("ms:loggingPrefs", logPrefs)
        logger.info(
          s"Browser logging (Edge): browser=${TestRunnerConfig.browserLoggingEnabled}(${TestRunnerConfig.browserLoggingLevel}), driver=${TestRunnerConfig.driverLoggingEnabled}(${TestRunnerConfig.driverLoggingLevel}), performance=${TestRunnerConfig.performanceLoggingEnabled}(${TestRunnerConfig.performanceLoggingLevel})"
        )

      case "firefox" =>
        logger.warn("Browser logging: Not supported for Firefox")

      case _ =>
        logger.warn(s"Browser logging: Not supported for $browserName")
    }

    capabilities
  }

  private def accessibilityAssessment(capabilities: MutableCapabilities): MutableCapabilities = {
    val browserName = capabilities.getBrowserName

    if (TestRunnerConfig.accessibilityAssessmentEnabled)
      browserName match {
        case "chrome"        =>
          capabilities
            .asInstanceOf[ChromeOptions]
            .addExtensions(BrowserExtensions.chromiumAccessibilityAssessment)
          logger.info("Accessibility assessment: Enabled")
        case "MicrosoftEdge" =>
          capabilities
            .asInstanceOf[EdgeOptions]
            .addExtensions(BrowserExtensions.chromiumAccessibilityAssessment)
          logger.info("Accessibility assessment: Enabled")
        case _               =>
          logger.warn("Accessibility assessment: Not available for Firefox")
      }

    capabilities
  }

  private def enableBiDi(capabilities: MutableCapabilities): MutableCapabilities = {
    if (!TestRunnerConfig.biDiEnabled) {
      logger.info("BiDi not enabled via config.")
      return capabilities
    }

    val browser = capabilities.getBrowserName.toLowerCase

    // enables the WebSocket connection for bidirectional communication
    // https://www.selenium.dev/documentation/webdriver/bidi/
    capabilities.setCapability("webSocketUrl", true)
    logger.info(s"BiDi enabled for $browser (webSocketUrl=true).")
    logger.debug(s"Capabilities after BiDi config: ${capabilities.asMap()}")

    capabilities
  }

  private def securityAssessment(capabilities: MutableCapabilities): MutableCapabilities = {
    val browserName = capabilities.getBrowserName
    val proxy       = new Proxy()

    if (TestRunnerConfig.securityAssessmentEnabled) {
      proxy.setHttpProxy(TestRunnerConfig.zapHost)
      proxy.setSslProxy(TestRunnerConfig.zapHost)

      browserName match {
        case "chrome"        => proxy.setNoProxy("<-loopback>")
        case "MicrosoftEdge" => proxy.setNoProxy("<-loopback>")
        case "firefox"       =>
          capabilities.asInstanceOf[FirefoxOptions].addPreference("network.proxy.allow_hijacking_localhost", true)
          capabilities
            .asInstanceOf[FirefoxOptions]
            .addPreference("network.proxy.testing_localhost_is_secure_when_hijacked", true)
      }

      capabilities.setCapability("proxy", proxy)
      logger.info(s"Security assessment: Enabled (${TestRunnerConfig.zapHost})")
    }

    capabilities
  }

  private def headless(capabilities: MutableCapabilities): MutableCapabilities = {
    val browserName = capabilities.getBrowserName

    if (TestRunnerConfig.browserOptionHeadLessEnabled) {
      browserName match {
        case "chrome"        =>
          capabilities
            .asInstanceOf[ChromeOptions]
            .addArguments("--headless=new", "--no-sandbox", "--disable-setuid-sandbox")
        case "MicrosoftEdge" =>
          capabilities
            .asInstanceOf[EdgeOptions]
            .addArguments("--headless=new", "--no-sandbox", "--disable-setuid-sandbox")
        case "firefox"       => capabilities.asInstanceOf[FirefoxOptions].addArguments("-headless")
      }

      logger.info("Browser option (headless): Enabled")
    }

    capabilities
  }

  private def downloadDirectory(capabilities: MutableCapabilities): MutableCapabilities = {
    val browserName = capabilities.getBrowserName

    val downloadDirectory = s"${System.getProperty("user.dir")}/target/browser-downloads"
    val preferences       = Map("download.default_directory" -> downloadDirectory).asJava

    browserName match {
      case "chrome"        => capabilities.asInstanceOf[ChromeOptions].setExperimentalOption("prefs", preferences)
      case "MicrosoftEdge" => capabilities.asInstanceOf[EdgeOptions].setExperimentalOption("prefs", preferences)
      case "firefox"       =>
        capabilities.asInstanceOf[FirefoxOptions].addPreference("browser.download.folderList", 2)
        capabilities.asInstanceOf[FirefoxOptions].addPreference("browser.download.dir", downloadDirectory)
    }

    capabilities
  }

}

private case class DriverFactoryException(exception: String) extends RuntimeException(exception)

object DriverFactory {
  private object BrowserExtensions {
    lazy val chromiumAccessibilityAssessment: File = {
      val extractedBrowserExtension = File.createTempFile("chromium-accessibility-assessment", ".crx")
      extractedBrowserExtension.deleteOnExit()
      Files.copy(
        getClass.getResourceAsStream("/browser-extensions/chromium-accessibility-assessment.crx"),
        extractedBrowserExtension.toPath,
        StandardCopyOption.REPLACE_EXISTING
      )
      extractedBrowserExtension
    }
  }
}
