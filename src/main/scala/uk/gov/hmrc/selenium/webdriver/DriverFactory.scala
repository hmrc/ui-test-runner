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
    options.addEncodedExtensions(accessibilityAssessmentExtension("chrome"))
    options.setCapability("se:downloadsEnabled", true)
    options
  }

  private[webdriver] def edgeOptions(): EdgeOptions = {
    val options: EdgeOptions = new EdgeOptions
    options.addEncodedExtensions(accessibilityAssessmentExtension("edge"))
    options.setCapability("se:downloadsEnabled", true)
    options
  }

  private[webdriver] def firefoxOptions(): FirefoxOptions = {
    val options: FirefoxOptions = new FirefoxOptions
    options.setCapability("se:downloadsEnabled", true)
    options
  }

  private def remoteWebDriver(capabilities: MutableCapabilities): RemoteWebDriver = {
    val remoteAddress: String   = "http://localhost:4444"
    val driver: RemoteWebDriver = new RemoteWebDriver(new URL(remoteAddress), capabilities)

    logger.info(s"Browser: ${driver.getCapabilities.getBrowserName} ${driver.getCapabilities.getBrowserVersion}")
    driver
  }

  private def accessibilityAssessmentExtension(browser: String): String =
    Source.fromResource(s"extensions/$browser/accessibility-assessment").getLines().mkString

}

private case class DriverFactoryException(exception: String) extends RuntimeException(exception)
