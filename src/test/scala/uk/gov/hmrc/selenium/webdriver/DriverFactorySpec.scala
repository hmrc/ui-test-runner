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

import com.typesafe.config.ConfigFactory
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.edge.EdgeOptions
import org.openqa.selenium.firefox.FirefoxOptions
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import java.util.Base64

class DriverFactorySpec extends AnyWordSpec with Matchers with BeforeAndAfterEach {

  trait Setup {
    val driverFactory: DriverFactory = new DriverFactory
    val downloadDirectory            = s"${System.getProperty("user.dir")}/target/browser-downloads"
  }

  override def afterEach(): Unit = {
    System.clearProperty("accessibility.assessment")
    System.clearProperty("security.assessment")
    System.clearProperty("browser.option.headless")
    ConfigFactory.invalidateCaches()
  }

  "DriverFactory" should {
    val accessibilityAssessmentExtension: String = Base64.getEncoder.encodeToString(
      getClass.getResourceAsStream("/browser-extensions/chromium-accessibility-assessment.crx").readAllBytes
    )

    "return default Chrome options" in new Setup {
      val options: ChromeOptions = driverFactory.chromeOptions()

      options.asMap().get("browserName")         shouldBe "chrome"
      options.asMap().get("acceptInsecureCerts") shouldBe true
      options
        .asMap()
        .get("goog:chromeOptions")
        .toString                                shouldBe s"{args=[--headless=new, --no-sandbox, --disable-setuid-sandbox, --disable-search-engine-choice-screen, --disable-features=OptimizationGuideModelDownloading,OptimizationHintsFetching,OptimizationTargetPrediction,OptimizationHints, --disable-features=MediaRouter], extensions=[$accessibilityAssessmentExtension], prefs={download.default_directory=$downloadDirectory}}"
    }

    "return Chrome options when accessibility assessment is disabled" in new Setup {
      System.setProperty("accessibility.assessment", "false")
      ConfigFactory.invalidateCaches()

      val options: ChromeOptions = driverFactory.chromeOptions()

      options.asMap().get("browserName")         shouldBe "chrome"
      options.asMap().get("acceptInsecureCerts") shouldBe true
      options
        .asMap()
        .get("goog:chromeOptions")
        .toString                                shouldBe s"{args=[--headless=new, --no-sandbox, --disable-setuid-sandbox, --disable-search-engine-choice-screen, --disable-features=OptimizationGuideModelDownloading,OptimizationHintsFetching,OptimizationTargetPrediction,OptimizationHints, --disable-features=MediaRouter], extensions=[], prefs={download.default_directory=$downloadDirectory}}"
    }

    "return Chrome options when security assessment is enabled" in new Setup {
      System.setProperty("security.assessment", "true")
      ConfigFactory.invalidateCaches()

      val options: ChromeOptions = driverFactory.chromeOptions()

      options.asMap().get("browserName")         shouldBe "chrome"
      options.asMap().get("acceptInsecureCerts") shouldBe true
      options.asMap().get("proxy").toString      shouldBe "Proxy(manual, http=localhost:11000, ssl=localhost:11000)"
      options
        .asMap()
        .get("goog:chromeOptions")
        .toString                                shouldBe s"{args=[--headless=new, --no-sandbox, --disable-setuid-sandbox, --disable-search-engine-choice-screen, --disable-features=OptimizationGuideModelDownloading,OptimizationHintsFetching,OptimizationTargetPrediction,OptimizationHints, --disable-features=MediaRouter], extensions=[$accessibilityAssessmentExtension], prefs={download.default_directory=$downloadDirectory}}"
    }

    "return Chrome logging preferences when browser logging is enabled" in new Setup {
      System.setProperty("browser.logging", "true")
      ConfigFactory.invalidateCaches()

      val options: ChromeOptions = driverFactory.chromeOptions()

      options.asMap().get("browserName") shouldBe "chrome"
      options
        .asMap()
        .get("goog:loggingPrefs")
        .toString                          should include("org.openqa.selenium.logging.LoggingPreferences")
    }

    "return no Chrome logging preferences when browser logging is disabled" in new Setup {
      System.setProperty("browser.logging", "false")
      ConfigFactory.invalidateCaches()

      val options: ChromeOptions = driverFactory.chromeOptions()

      options.asMap().get("browserName")               shouldBe "chrome"
      Option(options.asMap().get("goog:loggingPrefs")) shouldBe None
    }

    "return Chrome options when browser option headless is disabled" in new Setup {
      System.setProperty("browser.option.headless", "false")
      ConfigFactory.invalidateCaches()

      val options: ChromeOptions = driverFactory.chromeOptions()

      options.asMap().get("browserName")         shouldBe "chrome"
      options.asMap().get("acceptInsecureCerts") shouldBe true
      options
        .asMap()
        .get("goog:chromeOptions")
        .toString                                shouldBe s"{args=[--disable-search-engine-choice-screen, --disable-features=OptimizationGuideModelDownloading,OptimizationHintsFetching,OptimizationTargetPrediction,OptimizationHints, --disable-features=MediaRouter], extensions=[$accessibilityAssessmentExtension], prefs={download.default_directory=$downloadDirectory}}"
    }

    "set BiDi capability when enabled in config for Chrome" in new Setup {
      System.setProperty("bidi", "true")
      ConfigFactory.invalidateCaches()

      val options: ChromeOptions = driverFactory.chromeOptions()

      // Chrome: BiDi uses webSocketUrl = true
      options.asMap().get("webSocketUrl") shouldBe true
    }

    "not set BiDi capability when disabled in config for Chrome" in new Setup {
      System.setProperty("bidi", "false")
      ConfigFactory.invalidateCaches()

      val options: ChromeOptions = driverFactory.chromeOptions()

      // Should not include BiDi capability
      Option(options.asMap().get("webSocketUrl")) shouldBe None
    }

    "return default Edge options" in new Setup {
      val options: EdgeOptions = driverFactory.edgeOptions()

      options.asMap().get("browserName")         shouldBe "MicrosoftEdge"
      options.asMap().get("acceptInsecureCerts") shouldBe true
      options
        .asMap()
        .get("ms:edgeOptions")
        .toString                                shouldBe s"{args=[--headless=new, --no-sandbox, --disable-setuid-sandbox], extensions=[$accessibilityAssessmentExtension], prefs={download.default_directory=$downloadDirectory}}"
    }

    "return Edge options when accessibility assessment is disabled" in new Setup {
      System.setProperty("accessibility.assessment", "false")
      ConfigFactory.invalidateCaches()

      val options: EdgeOptions = driverFactory.edgeOptions()

      options.asMap().get("browserName")         shouldBe "MicrosoftEdge"
      options.asMap().get("acceptInsecureCerts") shouldBe true
      options
        .asMap()
        .get("ms:edgeOptions")
        .toString                                shouldBe s"{args=[--headless=new, --no-sandbox, --disable-setuid-sandbox], extensions=[], prefs={download.default_directory=$downloadDirectory}}"
    }

    "return Edge options when security assessment is enabled" in new Setup {
      System.setProperty("security.assessment", "true")
      ConfigFactory.invalidateCaches()

      val options: EdgeOptions = driverFactory.edgeOptions()

      options.asMap().get("browserName")         shouldBe "MicrosoftEdge"
      options.asMap().get("acceptInsecureCerts") shouldBe true
      options.asMap().get("proxy").toString      shouldBe "Proxy(manual, http=localhost:11000, ssl=localhost:11000)"
      options
        .asMap()
        .get("ms:edgeOptions")
        .toString                                shouldBe s"{args=[--headless=new, --no-sandbox, --disable-setuid-sandbox], extensions=[$accessibilityAssessmentExtension], prefs={download.default_directory=$downloadDirectory}}"
    }

    "return Edge options when browser option headless is disabled" in new Setup {
      System.setProperty("browser.option.headless", "false")
      ConfigFactory.invalidateCaches()

      val options: EdgeOptions = driverFactory.edgeOptions()

      options.asMap().get("browserName")         shouldBe "MicrosoftEdge"
      options.asMap().get("acceptInsecureCerts") shouldBe true
      options
        .asMap()
        .get("ms:edgeOptions")
        .toString                                shouldBe s"{args=[], extensions=[$accessibilityAssessmentExtension], prefs={download.default_directory=$downloadDirectory}}"
    }

    "set BiDi capability when enabled in config for Edge" in new Setup {
      System.setProperty("bidi", "true")
      ConfigFactory.invalidateCaches()

      val options: EdgeOptions = driverFactory.edgeOptions()

      // Edge: BiDi uses webSocketUrl = true
      options.asMap().get("webSocketUrl") shouldBe true
    }

    "not set BiDi capability for Firefox regardless of config" in new Setup {
      System.setProperty("bidi", "true")
      ConfigFactory.invalidateCaches()

      val options: FirefoxOptions = driverFactory.firefoxOptions()

      // Firefox should not get webSocketUrl at all
      Option(options.asMap().get("webSocketUrl")) shouldBe None
    }

    "return default Firefox options" in new Setup {
      val options: FirefoxOptions = driverFactory.firefoxOptions()

      options.asMap().get("browserName")         shouldBe "firefox"
      options.asMap().get("acceptInsecureCerts") shouldBe true
      options
        .asMap()
        .get("moz:firefoxOptions")
        .toString                                shouldBe s"{args=[-headless], prefs={browser.download.dir=$downloadDirectory, browser.download.folderList=2, remote.active-protocols=3}}"
    }

    "return Firefox options when security assessment is enabled" in new Setup {
      System.setProperty("security.assessment", "true")
      ConfigFactory.invalidateCaches()

      val options: FirefoxOptions = driverFactory.firefoxOptions()

      options.asMap().get("browserName")         shouldBe "firefox"
      options.asMap().get("acceptInsecureCerts") shouldBe true
      options
        .asMap()
        .get("moz:firefoxOptions")
        .toString                                shouldBe s"{args=[-headless], prefs={browser.download.dir=$downloadDirectory, browser.download.folderList=2, network.proxy.allow_hijacking_localhost=true, remote.active-protocols=3}}"
      options.asMap().get("proxy").toString      shouldBe "Proxy(manual, http=localhost:11000, ssl=localhost:11000)"
    }

    "return Firefox options when browser option headless is disabled" in new Setup {
      System.setProperty("browser.option.headless", "false")
      ConfigFactory.invalidateCaches()

      val options: FirefoxOptions = driverFactory.firefoxOptions()

      options.asMap().get("browserName")         shouldBe "firefox"
      options.asMap().get("acceptInsecureCerts") shouldBe true
      options
        .asMap()
        .get("moz:firefoxOptions")
        .toString                                shouldBe s"{prefs={browser.download.dir=$downloadDirectory, browser.download.folderList=2, remote.active-protocols=3}}"
    }
  }
}
