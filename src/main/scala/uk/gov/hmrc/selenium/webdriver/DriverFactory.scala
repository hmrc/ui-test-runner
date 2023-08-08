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
import org.openqa.selenium.MutableCapabilities
import org.openqa.selenium.Proxy
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.edge.EdgeOptions
import org.openqa.selenium.firefox.FirefoxOptions
import org.openqa.selenium.remote.RemoteWebDriver

import java.net.URL
import scala.io.Source

class DriverFactory extends LazyLogging {

  def initialise(): RemoteWebDriver = {
    val browser = sys.props.get("browser").map(_.toLowerCase)

    browser match {
      case Some("chrome")  => remoteWebDriver(chromeOptions())
      case Some("edge")    => remoteWebDriver(edgeOptions())
      case Some("firefox") => remoteWebDriver(firefoxOptions())
      case Some(browser)   => throw DriverFactoryException(s"Browser '$browser' is not supported.")
      case None            => throw DriverFactoryException("System property 'browser' is required but was not defined.")
    }
  }

  private[webdriver] def chromeOptions(): ChromeOptions = {
    val options: ChromeOptions = new ChromeOptions
    options.setCapability("se:downloadsEnabled", true)
    accessibilityAssessment(options)
    securityAssessment(options)
    options
  }

  private[webdriver] def edgeOptions(): EdgeOptions = {
    val options: EdgeOptions = new EdgeOptions
    options.setCapability("se:downloadsEnabled", true)
    accessibilityAssessment(options)
    securityAssessment(options)
    options
  }

  private[webdriver] def firefoxOptions(): FirefoxOptions = {
    val options: FirefoxOptions = new FirefoxOptions
    options.setCapability("se:downloadsEnabled", true)
    securityAssessment(options)
    options
  }

  private def remoteWebDriver(capabilities: MutableCapabilities): RemoteWebDriver = {
    val remoteAddress: String   = "http://localhost:4444"
    val driver: RemoteWebDriver = new RemoteWebDriver(new URL(remoteAddress), capabilities)

    logger.info(s"Browser: ${driver.getCapabilities.getBrowserName} ${driver.getCapabilities.getBrowserVersion}")
    driver
  }

  private def accessibilityAssessment(capabilities: MutableCapabilities): MutableCapabilities = {
    val enabled   = sys.props.getOrElse("accessibility.assessment", "true").toBoolean
    val browser   = capabilities.getBrowserName
    val extension = Source.fromResource(s"extensions/$browser/accessibility-assessment").getLines().mkString

    if (enabled) {
      browser match {
        case "chrome"        => capabilities.asInstanceOf[ChromeOptions].addEncodedExtensions(extension)
        case "MicrosoftEdge" => capabilities.asInstanceOf[EdgeOptions].addEncodedExtensions(extension)
      }
    }

    capabilities
  }

  private def securityAssessment(capabilities: MutableCapabilities): MutableCapabilities = {
    val enabled = sys.props.getOrElse("security.assessment", "false").toBoolean

    if (enabled) {
      val proxy = new Proxy()
      proxy.setHttpProxy("localhost:11000")
      proxy.setSslProxy("localhost:11000")

      capabilities.setCapability("acceptInsecureCerts", true)
      capabilities.setCapability("proxy", proxy)
    }

    capabilities
  }

}

private case class DriverFactoryException(exception: String) extends RuntimeException(exception)
