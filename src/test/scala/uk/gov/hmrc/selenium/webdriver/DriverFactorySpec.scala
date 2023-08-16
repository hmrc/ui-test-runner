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

import org.mockito.scalatest.MockitoSugar
import org.openqa.selenium.MutableCapabilities
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.edge.EdgeOptions
import org.openqa.selenium.firefox.FirefoxOptions
import org.openqa.selenium.remote.RemoteWebDriver
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.slf4j.LoggerFactory
import uk.gov.hmrc.util.LogCapturing

import scala.io.Source

class DriverFactorySpec extends AnyWordSpec with Matchers with MockitoSugar with LogCapturing {

  trait Setup {
    val driverFactory: DriverFactory = new DriverFactory
  }

  "DriverFactory" should {

    "return default Chrome options" in new Setup {
      val options: ChromeOptions                   = driverFactory.chromeOptions()
      val accessibilityAssessmentExtension: String =
        Source.fromResource("extensions/chrome/accessibility-assessment").getLines().mkString

      options.asMap().get("browserName")         shouldBe "chrome"
      options
        .asMap()
        .get("goog:chromeOptions")
        .toString                                shouldBe s"{args=[--remote-allow-origins=*], extensions=[$accessibilityAssessmentExtension]}"
      options.asMap().get("se:downloadsEnabled") shouldBe true
    }

    "return default Edge options" in new Setup {
      val options: EdgeOptions                     = driverFactory.edgeOptions()
      val accessibilityAssessmentExtension: String =
        Source.fromResource("extensions/edge/accessibility-assessment").getLines().mkString

      options.asMap().get("browserName")         shouldBe "MicrosoftEdge"
      options
        .asMap()
        .get("ms:edgeOptions")
        .toString                                shouldBe s"{args=[--remote-allow-origins=*], extensions=[$accessibilityAssessmentExtension]}"
      options.asMap().get("se:downloadsEnabled") shouldBe true
    }

    "return default Firefox options" in new Setup {
      val options: FirefoxOptions = driverFactory.firefoxOptions()

      options.asMap().get("browserName")                 shouldBe "firefox"
      options.asMap().get("moz:firefoxOptions").toString shouldBe "{}"
      options.asMap().get("se:downloadsEnabled")         shouldBe true
    }

    "return log message that accessibility assessment not available when using Firefox" in {
      withCaptureOfLoggingFrom(LoggerFactory.getLogger("uk.gov.hmrc.selenium.webdriver.DriverFactory")) { logEvents =>
        System.setProperty("browser", "firefox")

        val remoteWebDriver              = mock[RemoteWebDriver]
        val driverFactory: DriverFactory = spy(new DriverFactory, lenient = true)
        doReturn(remoteWebDriver).when(driverFactory).remoteWebDriver(any[MutableCapabilities])

        driverFactory.initialise()

        logEvents                                should not be empty
        logEvents.headOption.map(_.getMessage) shouldBe Some(
          "Accessibility assessment: Not available for Firefox"
        )
      }
    }
  }

}
