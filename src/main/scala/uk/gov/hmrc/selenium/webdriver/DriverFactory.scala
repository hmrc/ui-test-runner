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

import java.io.File
import java.nio.file.{Files, StandardCopyOption}
import java.util.logging.Level
import scala.jdk.CollectionConverters.MapHasAsJava

class DriverFactory extends LazyLogging {

  private val edgeBrowserVersion    = sys.env.getOrElse("BROWSER_VERSION", "126")
  private val firefoxBrowserVersion = sys.env.getOrElse("BROWSER_VERSION", "126")
  private val chromeBrowserVersion  = sys.env.getOrElse("BROWSER_VERSION", "126")

  def initialise(): WebDriver = {
    val browser = sys.props.get("browser").map(_.toLowerCase)

    browser match {
      case Some("chrome")  => new ChromeDriver(chromeOptions())
      case Some("edge")    => new EdgeDriver(edgeOptions())
      case Some("firefox") => new FirefoxDriver(firefoxOptions())
      case Some(browser)   => throw DriverFactoryException(s"Browser '$browser' is not supported.")
      case None            => throw DriverFactoryException("System property 'browser' is required but was not defined.")
    }
  }

  private[webdriver] def chromeOptions(): ChromeOptions = {
    val options: ChromeOptions = new ChromeOptions

    options.setBrowserVersion(chromeBrowserVersion)
    logger.info(s"Browser: ${options.getBrowserName} ${options.getBrowserVersion}")

    browserLogging(options)
    accessibilityAssessment(options)
    securityAssessment(options)
    downloadDirectory(options)
    headless(options)
    options.addArguments("--disable-search-engine-choice-screen")
    options.addArguments("--disable-features=OptimizationGuideModelDownloading,OptimizationHintsFetching,OptimizationTargetPrediction,OptimizationHints")
    options.addArguments("--disable-features=MediaRouter")
    options.setAcceptInsecureCerts(true)

    options
  }

  private[webdriver] def edgeOptions(): EdgeOptions = {
    val options: EdgeOptions = new EdgeOptions

    options.setBrowserVersion(edgeBrowserVersion)
    logger.info(s"Browser: ${options.getBrowserName} ${options.getBrowserVersion}")

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

    browserLogging(options)
    accessibilityAssessment(options)
    securityAssessment(options)
    downloadDirectory(options)
    headless(options)

    options.setAcceptInsecureCerts(true)

    options
  }

  private def browserLogging(capabilities: MutableCapabilities): MutableCapabilities = {
    val enabledLocal = sys.props.getOrElse("browser.logging", "false").toBoolean
    val enabledBuild = sys.env.getOrElse("BROWSER_LOGGING", "false").toBoolean
    val browserName  = capabilities.getBrowserName

    if (enabledLocal || enabledBuild) {
      browserName match {
        case "chrome" =>
          val logPrefs = new LoggingPreferences()
          logPrefs.enable(LogType.BROWSER, Level.ALL)
          capabilities
            .setCapability("goog:loggingPrefs", logPrefs)
          logger.info(s"Browser logging: Enabled")
        case _        =>
          logger.warn(s"Browser logging: Not available for $browserName")
      }
    }
    capabilities
  }

  private def accessibilityAssessment(capabilities: MutableCapabilities): MutableCapabilities = {
    val enabledLocal = sys.props.getOrElse("accessibility.assessment", "true").toBoolean
    val enabledBuild = sys.env.getOrElse("ACCESSIBILITY_ASSESSMENT", "false").toBoolean
    val browserName  = capabilities.getBrowserName

    if (enabledLocal || enabledBuild) {
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

    }

    capabilities
  }

  private def securityAssessment(capabilities: MutableCapabilities): MutableCapabilities = {
    val enabledLocal = sys.props.getOrElse("security.assessment", "false").toBoolean
    val enabledBuild = sys.env.getOrElse("SECURITY_ASSESSMENT", "false").toBoolean
    val browserName  = capabilities.getBrowserName
    val proxy        = new Proxy()

    if (enabledLocal || enabledBuild) {
      proxy.setHttpProxy("localhost:11000")
      proxy.setSslProxy("localhost:11000")

      browserName match {
        case "chrome"        => proxy.setNoProxy("<-loopback>")
        case "MicrosoftEdge" => proxy.setNoProxy("<-loopback>")
        case "firefox"       =>
          capabilities.asInstanceOf[FirefoxOptions].addPreference("network.proxy.allow_hijacking_localhost", true)
      }

      capabilities.setCapability("proxy", proxy)
      logger.info(s"Security assessment: Enabled (localhost:11000)")
    }

    capabilities
  }

  private def headless(capabilities: MutableCapabilities): MutableCapabilities = {
    val enabledLocal = sys.props.getOrElse("browser.option.headless", "true").toBoolean
    val enabledBuild = sys.env.getOrElse("BROWSER_OPTION_HEADLESS", "false").toBoolean
    val browserName  = capabilities.getBrowserName

    if (enabledLocal || enabledBuild) {
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
