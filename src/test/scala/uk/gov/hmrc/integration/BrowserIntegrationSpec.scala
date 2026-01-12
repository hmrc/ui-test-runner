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

package uk.gov.hmrc.integration

import com.typesafe.config.ConfigFactory
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.firefox.FirefoxDriver
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import uk.gov.hmrc.selenium.webdriver.{Browser, Driver}

class BrowserIntegrationSpec extends AnyWordSpec with Matchers with BeforeAndAfterEach with Browser {

  override def afterEach(): Unit = {
    // Clear system properties
    System.clearProperty("browser")
    System.clearProperty("browser.version")
    System.clearProperty("browser.option.downloadFromArtifactory")
    System.clearProperty("SE_CHROME_MIRROR_URL")
    System.clearProperty("SE_CHROMEDRIVER_MIRROR_URL")
    System.clearProperty("SE_FIREFOX_MIRROR_URL")
    System.clearProperty("SE_GECKODRIVER_MIRROR_URL")
    System.clearProperty("ARTIFACTORY_URI")

    ConfigFactory.invalidateCaches()
  }

  "Browser" should {
    "start and quit Chrome browser with Artifactory enabled" in {
      System.setProperty("browser", "chrome")
      System.setProperty("browser.version", "136")
      System.setProperty("browser.option.downloadFromArtifactory", "true")
      ConfigFactory.invalidateCaches()

      startBrowser()

      Driver.instance.asInstanceOf[ChromeDriver].getSessionId                  shouldNot be(null)
      Driver.instance.asInstanceOf[ChromeDriver].getCapabilities.getBrowserName shouldBe "chrome"

      quitBrowser()

      Driver.instance.asInstanceOf[ChromeDriver].getSessionId shouldBe null
    }

    "start Chrome browser with Artifactory disabled" in {
      System.setProperty("browser", "chrome")
      System.setProperty("browser.version", "136")
      System.setProperty("browser.option.downloadFromArtifactory", "false")
      System.setProperty("ARTIFACTORY_URI", "https://unreachable-artifactory.invalid")
      ConfigFactory.invalidateCaches()

      startBrowser()

      Driver.instance.asInstanceOf[ChromeDriver].getSessionId shouldNot be(null)

      quitBrowser()
    }

    "respect user-configured SE_CHROME_MIRROR_URL and skip Artifactory configuration" in {
      System.setProperty("browser", "chrome")
      System.setProperty("browser.version", "136")
      System.setProperty("browser.option.downloadFromArtifactory", "true")
      System.setProperty("ARTIFACTORY_URI", "https://unreachable-artifactory.invalid")
      System.setProperty("SE_CHROME_MIRROR_URL", "https://artefacts.tax.service.gov.uk/artifactory/chrome-browser/")
      System.setProperty(
        "SE_CHROMEDRIVER_MIRROR_URL",
        "https://artefacts.tax.service.gov.uk/artifactory/chrome-browser/"
      )
      ConfigFactory.invalidateCaches()

      startBrowser()

      Driver.instance.asInstanceOf[ChromeDriver].getSessionId shouldNot be(null)

      quitBrowser()
    }

    "start and quit Firefox browser with Artifactory enabled" in {
      System.setProperty("browser", "firefox")
      System.setProperty("browser.option.downloadFromArtifactory", "true")
      ConfigFactory.invalidateCaches()

      startBrowser()

      Driver.instance.asInstanceOf[FirefoxDriver].getSessionId                  shouldNot be(null)
      Driver.instance.asInstanceOf[FirefoxDriver].getCapabilities.getBrowserName shouldBe "firefox"

      quitBrowser()

      Driver.instance.asInstanceOf[FirefoxDriver].getSessionId shouldBe null
    }

    "throw an exception for unknown browser" in {
      System.setProperty("browser", "test")
      ConfigFactory.invalidateCaches()

      val exception: Exception = intercept[Exception] {
        startBrowser()
      }

      exception.getMessage shouldBe "Browser 'test' is not supported."
    }

    "throw an exception for undefined browser" in {
      System.clearProperty("browser")
      ConfigFactory.invalidateCaches()

      val exception: Exception = intercept[Exception] {
        startBrowser()
      }

      exception.getMessage shouldBe "System property 'browser' is required but was not defined."
    }

    "fail fast with VPN error when Artifactory is unreachable" in {
      assume(!sys.env.contains("CI"), "Skipping VPN test in CI environment")

      System.setProperty("browser", "chrome")
      System.setProperty("browser.version", "136")
      System.setProperty("browser.option.downloadFromArtifactory", "true")
      System.setProperty("ARTIFACTORY_URI", "https://unreachable-artifactory.invalid")
      ConfigFactory.invalidateCaches()

      val startTime = System.currentTimeMillis()

      val exception = intercept[Exception] {
        startBrowser()
      }

      val duration = System.currentTimeMillis() - startTime

      exception.getMessage should include("VPN")
      duration             should be < 5000L
    }
  }
}
