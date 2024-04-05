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

import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.edge.EdgeOptions
import org.openqa.selenium.firefox.FirefoxOptions
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import scala.io.Source

class DriverFactorySpec extends AnyWordSpec with Matchers with BeforeAndAfterEach {

  trait Setup {
    val driverFactory: DriverFactory = new DriverFactory
  }

  override def afterEach(): Unit = {
    System.clearProperty("accessibility.assessment")
    System.clearProperty("security.assessment")
    System.clearProperty("browser.option.headless")
  }

  "DriverFactory" should {

    "return default Chrome options" in new Setup {
      val options: ChromeOptions                   = driverFactory.chromeOptions()
      val accessibilityAssessmentExtension: String =
        Source.fromResource("extensions/chrome/accessibility-assessment").getLines().mkString

      options.asMap().get("browserName")         shouldBe "chrome"
      options.asMap().get("acceptInsecureCerts") shouldBe true
      options
        .asMap()
        .get("goog:chromeOptions")
        .toString                                shouldBe s"{args=[--headless=new, --no-sandbox, --disable-setuid-sandbox], extensions=[$accessibilityAssessmentExtension]}"
    }

    "return Chrome options when accessibility assessment is disabled" in new Setup {
      System.setProperty("accessibility.assessment", "false")

      val options: ChromeOptions = driverFactory.chromeOptions()

      options.asMap().get("browserName")         shouldBe "chrome"
      options.asMap().get("acceptInsecureCerts") shouldBe true
      options
        .asMap()
        .get("goog:chromeOptions")
        .toString                                shouldBe s"{args=[--headless=new, --no-sandbox, --disable-setuid-sandbox], extensions=[]}"
    }

    "return Chrome options when security assessment is enabled" in new Setup {
      System.setProperty("security.assessment", "true")

      val options: ChromeOptions = driverFactory.chromeOptions()
      val accessibilityAssessmentExtension: String =
        Source.fromResource("extensions/chrome/accessibility-assessment").getLines().mkString

      options.asMap().get("browserName")         shouldBe "chrome"
      options.asMap().get("acceptInsecureCerts") shouldBe true
      options.asMap().get("proxy").toString      shouldBe "Proxy(manual, http=localhost:11000, ssl=localhost:11000)"
      options
        .asMap()
        .get("goog:chromeOptions")
        .toString                                shouldBe s"{args=[--headless=new, --no-sandbox, --disable-setuid-sandbox], extensions=[$accessibilityAssessmentExtension]}"
    }

    "return Chrome options when browser option headless is disabled" in new Setup {
      System.setProperty("browser.option.headless", "false")

      val options: ChromeOptions                   = driverFactory.chromeOptions()
      val accessibilityAssessmentExtension: String =
        Source.fromResource("extensions/chrome/accessibility-assessment").getLines().mkString

      options.asMap().get("browserName")         shouldBe "chrome"
      options.asMap().get("acceptInsecureCerts") shouldBe true
      options
        .asMap()
        .get("goog:chromeOptions")
        .toString                                shouldBe s"{args=[], extensions=[$accessibilityAssessmentExtension]}"
    }

    "return default Edge options" in new Setup {
      val options: EdgeOptions                     = driverFactory.edgeOptions()
      val accessibilityAssessmentExtension: String =
        Source.fromResource("extensions/MicrosoftEdge/accessibility-assessment").getLines().mkString

      options.asMap().get("browserName")         shouldBe "MicrosoftEdge"
      options.asMap().get("acceptInsecureCerts") shouldBe true
      options
        .asMap()
        .get("ms:edgeOptions")
        .toString                                shouldBe s"{args=[--headless=new, --no-sandbox, --disable-setuid-sandbox], extensions=[$accessibilityAssessmentExtension]}"
    }

    "return Edge options when accessibility assessment is disabled" in new Setup {
      System.setProperty("accessibility.assessment", "false")

      val options: EdgeOptions = driverFactory.edgeOptions()

      options.asMap().get("browserName")         shouldBe "MicrosoftEdge"
      options.asMap().get("acceptInsecureCerts") shouldBe true
      options
        .asMap()
        .get("ms:edgeOptions")
        .toString                                shouldBe s"{args=[--headless=new, --no-sandbox, --disable-setuid-sandbox], extensions=[]}"
    }

    "return Edge options when security assessment is enabled" in new Setup {
      System.setProperty("security.assessment", "true")

      val options: EdgeOptions = driverFactory.edgeOptions()
      val accessibilityAssessmentExtension: String =
        Source.fromResource("extensions/MicrosoftEdge/accessibility-assessment").getLines().mkString


      options.asMap().get("browserName")         shouldBe "MicrosoftEdge"
      options.asMap().get("acceptInsecureCerts") shouldBe true
      options.asMap().get("proxy").toString      shouldBe "Proxy(manual, http=localhost:11000, ssl=localhost:11000)"
      options
        .asMap()
        .get("ms:edgeOptions")
        .toString                                shouldBe s"{args=[--headless=new, --no-sandbox, --disable-setuid-sandbox], extensions=[$accessibilityAssessmentExtension]}"
    }

    "return Edge options when browser option headless is disabled" in new Setup {
      System.setProperty("browser.option.headless", "false")

      val options: EdgeOptions = driverFactory.edgeOptions()
      val accessibilityAssessmentExtension: String =
        Source.fromResource("extensions/MicrosoftEdge/accessibility-assessment").getLines().mkString

      options.asMap().get("browserName")         shouldBe "MicrosoftEdge"
      options.asMap().get("acceptInsecureCerts") shouldBe true
      options
        .asMap()
        .get("ms:edgeOptions")
        .toString                                shouldBe s"{args=[], extensions=[$accessibilityAssessmentExtension]}"
    }

    "return default Firefox options" in new Setup {
      val options: FirefoxOptions = driverFactory.firefoxOptions()

      options.asMap().get("browserName")                 shouldBe "firefox"
      options.asMap().get("acceptInsecureCerts")         shouldBe true
      options.asMap().get("moz:firefoxOptions").toString shouldBe "{args=[-headless]}"
    }

    "return Firefox options when security assessment is enabled" in new Setup {
      System.setProperty("security.assessment", "true")

      val options: FirefoxOptions = driverFactory.firefoxOptions()

      options.asMap().get("browserName")         shouldBe "firefox"
      options.asMap().get("acceptInsecureCerts") shouldBe true
      options
        .asMap()
        .get("moz:firefoxOptions")
        .toString                                shouldBe "{args=[-headless], prefs={network.proxy.allow_hijacking_localhost=true}}"
      options.asMap().get("proxy").toString      shouldBe "Proxy(manual, http=localhost:11000, ssl=localhost:11000)"
    }

    "return Firefox options when browser option headless is disabled" in new Setup {
      System.setProperty("browser.option.headless", "false")

      val options: FirefoxOptions = driverFactory.firefoxOptions()

      options.asMap().get("browserName")                 shouldBe "firefox"
      options.asMap().get("acceptInsecureCerts")         shouldBe true
      options.asMap().get("moz:firefoxOptions").toString shouldBe "{}"
    }

  }

}
