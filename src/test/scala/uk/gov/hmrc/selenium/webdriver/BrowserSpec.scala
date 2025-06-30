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
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.edge.EdgeDriver
import org.openqa.selenium.firefox.FirefoxDriver
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class BrowserSpec extends AnyWordSpec with Matchers with BeforeAndAfterEach with Browser {

  override def afterEach(): Unit = {
    System.clearProperty("browser")
    System.clearProperty("browser.version")
    System.clearProperty("driver-mirror-url")
    System.clearProperty("browser-mirror-url")
    ConfigFactory.invalidateCaches()
  }

  "Browser" should {
    "start and quit Chrome browser with default options" in {
      System.setProperty("browser", "chrome")
      System.setProperty("driver-mirror-url", "https://artefacts.tax.service.gov.uk/artifactory/chrome-browser/")
      System.setProperty("browser-mirror-url", "https://artefacts.tax.service.gov.uk/artifactory/chrome-browser/")
      System.setProperty("browser.version", "136")
      ConfigFactory.invalidateCaches()

      startBrowser()

      Driver.instance.asInstanceOf[ChromeDriver].getSessionId                  shouldNot be(null)
      Driver.instance.asInstanceOf[ChromeDriver].getCapabilities.getBrowserName shouldBe "chrome"

      quitBrowser()

      Driver.instance.asInstanceOf[ChromeDriver].getSessionId shouldBe null
    }

    // commenting out for now - we want this test but it currently fails due to the following known issue:
    // https://github.com/MicrosoftEdge/EdgeWebDriver/issues/159
    //    "start and quit Edge browser with default options" in {
    //      System.setProperty("browser", "edge")
    //
    //      startBrowser()
    //
    //      Driver.instance.asInstanceOf[EdgeDriver].getSessionId                  shouldNot be(null)
    //      Driver.instance.asInstanceOf[EdgeDriver].getCapabilities.getBrowserName shouldBe "MicrosoftEdge"
    //
    //      quitBrowser()
    //
    //      Driver.instance.asInstanceOf[EdgeDriver].getSessionId shouldBe null
    //    }

    "start and quit Firefox browser with default options" in {
      System.setProperty("browser", "firefox")
      System.setProperty("driver-mirror-url", "https://artefacts.tax.service.gov.uk/artifactory/firefox-browser/")
      System.setProperty("browser-mirror-url", "https://artefacts.tax.service.gov.uk/artifactory/firefox-browser/")
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
  }
}
