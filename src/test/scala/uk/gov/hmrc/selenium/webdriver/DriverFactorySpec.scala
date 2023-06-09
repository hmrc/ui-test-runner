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
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import scala.io.Source

class DriverFactorySpec extends AnyWordSpec with Matchers {

  trait Setup {
    val driverFactory: DriverFactory = new DriverFactory
  }

  "DriverFactory" should {

    "return default Chrome options" in new Setup {
      val options: ChromeOptions   = driverFactory.chromeOptions(None)
      val encodedExtension: String =
        Source.fromResource("extensions/chrome/accessibility-assessment").getLines().mkString

      options.asMap().get("browserName") shouldBe "chrome"
      options
        .asMap()
        .get("goog:chromeOptions")
        .toString                        shouldBe s"{args=[--remote-allow-origins=*], extensions=[$encodedExtension]}"
    }

    "return merged Chrome options" in new Setup {
      val capabilities = new ChromeOptions

      capabilities.setAcceptInsecureCerts(true)
      capabilities.addArguments("--headless=new")

      val options: ChromeOptions   = driverFactory.chromeOptions(Some(capabilities))
      val encodedExtension: String =
        Source.fromResource("extensions/chrome/accessibility-assessment").getLines().mkString

      options.asMap().get("acceptInsecureCerts") shouldBe true
      options.asMap().get("browserName")         shouldBe "chrome"
      options
        .asMap()
        .get("goog:chromeOptions")
        .toString                                shouldBe s"{args=[--remote-allow-origins=*, --headless=new], extensions=[$encodedExtension]}"
    }

    "return default Edge options" in new Setup {
      val options: EdgeOptions     = driverFactory.edgeOptions(None)
      val encodedExtension: String = Source.fromResource("extensions/edge/accessibility-assessment").getLines().mkString

      options.asMap().get("browserName") shouldBe "MicrosoftEdge"
      options
        .asMap()
        .get("ms:edgeOptions")
        .toString                        shouldBe s"{args=[--remote-allow-origins=*], extensions=[$encodedExtension]}"
    }

    "return merged Edge options" in new Setup {
      val capabilities = new EdgeOptions

      capabilities.setAcceptInsecureCerts(true)
      capabilities.addArguments("--headless=new")

      val options: EdgeOptions     = driverFactory.edgeOptions(Some(capabilities))
      val encodedExtension: String = Source.fromResource("extensions/edge/accessibility-assessment").getLines().mkString

      options.asMap().get("acceptInsecureCerts") shouldBe true
      options.asMap().get("browserName")         shouldBe "MicrosoftEdge"
      options
        .asMap()
        .get("ms:edgeOptions")
        .toString                                shouldBe s"{args=[--remote-allow-origins=*, --headless=new], extensions=[$encodedExtension]}"
    }

    "return default Firefox options" in new Setup {
      val options: FirefoxOptions = driverFactory.firefoxOptions(None)

      options.asMap().get("browserName")                 shouldBe "firefox"
      options.asMap().get("moz:firefoxOptions").toString shouldBe "{}"
    }

    "return merged Firefox options" in new Setup {
      val capabilities = new FirefoxOptions

      capabilities.setAcceptInsecureCerts(true)
      capabilities.addArguments("-headless")

      val options: FirefoxOptions = driverFactory.firefoxOptions(Some(capabilities))

      options.asMap().get("acceptInsecureCerts")         shouldBe true
      options.asMap().get("browserName")                 shouldBe "firefox"
      options.asMap().get("moz:firefoxOptions").toString shouldBe "{args=[-headless]}"
    }

  }

}
