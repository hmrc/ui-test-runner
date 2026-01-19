/*
 * Copyright 2026 HM Revenue & Customs
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

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.options

import scala.jdk.CollectionConverters.CollectionHasAsScala
import org.openqa.selenium.chrome.{ChromeDriver, ChromeOptions}
import org.openqa.selenium.edge.{EdgeDriver, EdgeOptions}
import org.openqa.selenium.firefox.{FirefoxDriver, FirefoxOptions}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import uk.gov.hmrc.helpers.{unusedPort, withSystemProperties}
import uk.gov.hmrc.uitestrunner.config.TestRunnerConfig

class SourceBrowserBinariesFromArtifactorySpec extends AnyWordSpec with Matchers {

  "checkArtifactoryIsAvailable()" should {
    "not make repeat requests to artifactory" in {
      val fakeArtifactory = new WireMockServer(options().dynamicPort())
      try {
        fakeArtifactory.start()
        fakeArtifactory.stubFor(get("/api/system/ping").willReturn(aResponse().withStatus(200)))
        val sourcesOfBrowserBinaries = new SourceBrowserBinariesFromArtifactory(
          sysEnv = Map.empty,
          artifactoryBaseUrl = fakeArtifactory.baseUrl()
        )
        sourcesOfBrowserBinaries.checkArtifactoryIsAvailable()
        sourcesOfBrowserBinaries.checkArtifactoryIsAvailable()
        fakeArtifactory.verify(1, getRequestedFor(urlEqualTo("/api/system/ping")))
      } finally fakeArtifactory.stop()
    }

    "throw if artifactory is unavailable and indicate likely cause" in {
      val sourcesOfBrowserBinaries = new SourceBrowserBinariesFromArtifactory(
        sysEnv = Map.empty,
        artifactoryBaseUrl = s"http://localhost:$unusedPort"
      )
      intercept[RuntimeException] {
        sourcesOfBrowserBinaries.checkArtifactoryIsAvailable()
      }.getMessage should include("Artifactory unreachable. Are you connected to VPN?")
    }
  }

  "configureSeleniumManager()" when {
    "config isn't already set in system env" should {
      "set system properties for all mirror urls" in {
        val artifactoryBaseUrl = s"http://localhost:$unusedPort"

        try {
          new SourceBrowserBinariesFromArtifactory(
            sysEnv = Map.empty,
            artifactoryBaseUrl = artifactoryBaseUrl
          ).configureSeleniumManager()

          sys.props.get("SE_CHROME_MIRROR_URL")       shouldBe Some(s"$artifactoryBaseUrl/chrome-browser/")
          sys.props.get("SE_CHROMEDRIVER_MIRROR_URL") shouldBe Some(s"$artifactoryBaseUrl/chrome-browser/")
          sys.props.get("SE_FIREFOX_MIRROR_URL")      shouldBe Some(s"$artifactoryBaseUrl/firefox-browser/")
          sys.props.get("SE_GECKODRIVER_MIRROR_URL")  shouldBe Some(s"$artifactoryBaseUrl/firefox-browser/")
          sys.props.get("SE_MSEDGE_MIRROR_URL")       shouldBe Some(s"$artifactoryBaseUrl/edge-browser/")
          sys.props.get("SE_MSEDGEDRIVER_MIRROR_URL") shouldBe Some(s"$artifactoryBaseUrl/edge-driver/")
        } finally
          Seq(
            "SE_CHROME_MIRROR_URL",
            "SE_CHROMEDRIVER_MIRROR_URL",
            "SE_FIREFOX_MIRROR_URL",
            "SE_GECKODRIVER_MIRROR_URL",
            "SE_MSEDGE_MIRROR_URL",
            "SE_MSEDGEDRIVER_MIRROR_URL"
          ).foreach(sys.props.remove)
      }
    }

    "config is already set in system env" should {
      "not override them" in {
        try {
          new SourceBrowserBinariesFromArtifactory(
            sysEnv = Map(
              "SE_CHROME_MIRROR_URL"       -> "chrome-browser",
              "SE_CHROMEDRIVER_MIRROR_URL" -> "chrome-driver",
              "SE_FIREFOX_MIRROR_URL"      -> "firefox-browser",
              "SE_GECKODRIVER_MIRROR_URL"  -> "firefox-driver",
              "SE_MSEDGE_MIRROR_URL"       -> "edge-browser",
              "SE_MSEDGEDRIVER_MIRROR_URL" -> "edge-driver"
            ),
            artifactoryBaseUrl = s"http://localhost:$unusedPort"
          ).configureSeleniumManager()

          sys.props.get("SE_CHROME_MIRROR_URL")       shouldBe Some(s"chrome-browser")
          sys.props.get("SE_CHROMEDRIVER_MIRROR_URL") shouldBe Some(s"chrome-driver")
          sys.props.get("SE_FIREFOX_MIRROR_URL")      shouldBe Some(s"firefox-browser")
          sys.props.get("SE_GECKODRIVER_MIRROR_URL")  shouldBe Some(s"firefox-driver")
          sys.props.get("SE_MSEDGE_MIRROR_URL")       shouldBe Some(s"edge-browser")
          sys.props.get("SE_MSEDGEDRIVER_MIRROR_URL") shouldBe Some(s"edge-driver")
        } finally
          Seq(
            "SE_CHROME_MIRROR_URL",
            "SE_CHROMEDRIVER_MIRROR_URL",
            "SE_FIREFOX_MIRROR_URL",
            "SE_GECKODRIVER_MIRROR_URL",
            "SE_MSEDGE_MIRROR_URL",
            "SE_MSEDGEDRIVER_MIRROR_URL"
          ).foreach(sys.props.remove)
      }
    }
  }

  "configureSeleniumManagerTemporarily()" should {
    "only override system properties temporarily within the passed scope" in {
      val artifactoryBaseUrl = s"http://localhost:$unusedPort"

      withSystemProperties(
        "SE_CHROME_MIRROR_URL"       -> "chrome-browser",
        "SE_CHROMEDRIVER_MIRROR_URL" -> "chrome-driver",
        "SE_FIREFOX_MIRROR_URL"      -> "firefox-browser",
        "SE_GECKODRIVER_MIRROR_URL"  -> "firefox-driver",
        "SE_MSEDGE_MIRROR_URL"       -> "edge-browser",
        "SE_MSEDGEDRIVER_MIRROR_URL" -> "edge-driver"
      ) {
        new SourceBrowserBinariesFromArtifactory(
          sysEnv = Map(
            "SE_CHROME_MIRROR_URL"       -> "chrome-browser-set-by-sys-env",
            "SE_CHROMEDRIVER_MIRROR_URL" -> "chrome-driver-set-by-sys-env"
          ),
          artifactoryBaseUrl = artifactoryBaseUrl
        ).configureSeleniumManagerTemporarily {
          sys.props.get("SE_CHROME_MIRROR_URL")       shouldBe Some("chrome-browser-set-by-sys-env")
          sys.props.get("SE_CHROMEDRIVER_MIRROR_URL") shouldBe Some("chrome-driver-set-by-sys-env")
          sys.props.get("SE_FIREFOX_MIRROR_URL")      shouldBe Some(s"$artifactoryBaseUrl/firefox-browser/")
          sys.props.get("SE_GECKODRIVER_MIRROR_URL")  shouldBe Some(s"$artifactoryBaseUrl/firefox-browser/")
          sys.props.get("SE_MSEDGE_MIRROR_URL")       shouldBe Some(s"$artifactoryBaseUrl/edge-browser/")
          sys.props.get("SE_MSEDGEDRIVER_MIRROR_URL") shouldBe Some(s"$artifactoryBaseUrl/edge-driver/")
        }

        sys.props.get("SE_CHROME_MIRROR_URL")       shouldBe Some(s"chrome-browser")
        sys.props.get("SE_CHROMEDRIVER_MIRROR_URL") shouldBe Some(s"chrome-driver")
        sys.props.get("SE_FIREFOX_MIRROR_URL")      shouldBe Some(s"firefox-browser")
        sys.props.get("SE_GECKODRIVER_MIRROR_URL")  shouldBe Some(s"firefox-driver")
        sys.props.get("SE_MSEDGE_MIRROR_URL")       shouldBe Some(s"edge-browser")
        sys.props.get("SE_MSEDGEDRIVER_MIRROR_URL") shouldBe Some(s"edge-driver")
      }
    }
  }

  "the mirror url system properties which we are using to configure selenium-manager" when {
    "starting chrome browser" should {
      "override where browser binaries are sourced from" in {
        withSystemProperties(
          // without the following it doesn't always seem to make a request to artifactory so
          // the tests could be flakey - there wouldn't always be an exception intercepted
          "SE_CACHE_PATH" -> os.temp.dir(deleteOnExit = true).toString
        ) {
          new SourceBrowserBinariesFromArtifactory(
            sysEnv = Map.empty,
            artifactoryBaseUrl = s"http://localhost:$unusedPort"
          ).configureSeleniumManagerTemporarily {
            intercept[Exception] {
              val options = new ChromeOptions()
              options.setBrowserVersion("136")
              new ChromeDriver(options)
            }.getMessage should include("Unable to obtain: chromedriver")
          }
        }
      }

      "don't make any requests to other sources of browser binaries" in {
        val wiremockProxy = new WireMockServer(
          options()
            .dynamicPort()
            .enableBrowserProxying(true)
            .trustAllProxyTargets(true)
        )
        try {
          wiremockProxy.start()
          withSystemProperties(
            "SE_PROXY"      -> wiremockProxy.baseUrl(),
            // without the following it doesn't always seem to make a request to artifactory so
            // the tests could be flakey - there wouldn't always be an exception intercepted
            "SE_CACHE_PATH" -> os.temp.dir(deleteOnExit = true).toString
          ) {
            new SourceBrowserBinariesFromArtifactory(
              sysEnv = Map.empty,
              artifactoryBaseUrl = TestRunnerConfig.artifactoryBaseUrl
            ).configureSeleniumManagerTemporarily {
              val options = new ChromeOptions()
              options.setBrowserVersion("136")
              val driver  = new ChromeDriver(options)
              driver.quit()
            }
            // the following is an example of how to check all the requests:
            wiremockProxy.getAllServeEvents.asScala.toList.foreach(request =>
              println(request.getRequest.getHost + request.getRequest.getUrl)
            )
          }
        } finally
          wiremockProxy.stop()
      }
    }

    "starting firefox browser" should {
      "override where browser binaries are sourced from" in {
        withSystemProperties(
          // without the following it doesn't always seem to make a request to artifactory so
          // the tests could be flakey - there wouldn't always be an exception intercepted
          "SE_CACHE_PATH" -> os.temp.dir(deleteOnExit = true).toString
        ) {
          new SourceBrowserBinariesFromArtifactory(
            sysEnv = Map.empty,
            artifactoryBaseUrl = s"http://localhost:$unusedPort"
          ).configureSeleniumManagerTemporarily {
            intercept[Exception] {
              val options = new FirefoxOptions()
              new FirefoxDriver(options)
            }.getMessage should include("Unable to obtain: geckodriver")
          }
        }
      }
    }

    "starting edge browser" should { // this test is slow to fail
      "override where browser binaries are sourced from" in {
        withSystemProperties(
          // without the following it doesn't always seem to make a request to artifactory so
          // the tests could be flakey - there wouldn't always be an exception intercepted
          "SE_CACHE_PATH" -> os.temp.dir(deleteOnExit = true).toString
        ) {
          new SourceBrowserBinariesFromArtifactory(
            sysEnv = Map.empty,
            artifactoryBaseUrl = s"http://localhost:$unusedPort"
          ).configureSeleniumManagerTemporarily {
            intercept[Exception] {
              val options = new EdgeOptions()
              new EdgeDriver(options)
            }.getMessage should include("Unable to obtain: msedgedriver")
          }
        }
      }
    }
  }
}
