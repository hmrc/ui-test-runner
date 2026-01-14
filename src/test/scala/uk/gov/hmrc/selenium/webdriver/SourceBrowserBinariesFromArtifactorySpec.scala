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

import org.openqa.selenium.WebDriver
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import uk.gov.hmrc.uitestrunner.config.TestRunnerConfig
import org.scalamock.scalatest.MockFactory
import uk.gov.hmrc.helpers.withSystemProperties

class SourceBrowserBinariesFromArtifactorySpec extends AnyWordSpec with Matchers with MockFactory {

  "SourceBrowserBinariesFromArtifactory" should {
    val artifactoryBaseUrl = TestRunnerConfig.artifactoryBaseUrl

    "override system properties that control where selenium gets browser binaries from temporarily" in {
      SourceBrowserBinariesFromArtifactory {
        sys.props.get("SE_CHROME_MIRROR_URL")       should be(Some(s"$artifactoryBaseUrl/chrome-browser/"))
        sys.props.get("SE_CHROMEDRIVER_MIRROR_URL") should be(Some(s"$artifactoryBaseUrl/chrome-browser/"))
        sys.props.get("SE_FIREFOX_MIRROR_URL")      should be(Some(s"$artifactoryBaseUrl/firefox-browser/"))
        sys.props.get("SE_GECKODRIVER_MIRROR_URL")  should be(Some(s"$artifactoryBaseUrl/firefox-browser/"))
        sys.props.get("SE_MSEDGE_MIRROR_URL")       should be(Some(s"$artifactoryBaseUrl/edge-browser/"))
        sys.props.get("SE_MSEDGEDRIVER_MIRROR_URL") should be(Some(s"$artifactoryBaseUrl/edge-driver/"))
        stub[WebDriver]
      }
      sys.props.get("SE_CHROME_MIRROR_URL")       should be(None)
      sys.props.get("SE_CHROMEDRIVER_MIRROR_URL") should be(None)
      sys.props.get("SE_FIREFOX_MIRROR_URL")      should be(None)
      sys.props.get("SE_GECKODRIVER_MIRROR_URL")  should be(None)
      sys.props.get("SE_MSEDGE_MIRROR_URL")       should be(None)
      sys.props.get("SE_MSEDGEDRIVER_MIRROR_URL") should be(None)
    }

    "not override system properties already set by the user" in {
      withSystemProperties(
        "SE_CHROME_MIRROR_URL"       -> "???",
        "SE_CHROMEDRIVER_MIRROR_URL" -> "???"
      ) {
        SourceBrowserBinariesFromArtifactory {
          sys.props.get("SE_CHROME_MIRROR_URL")       should be(Some(s"???"))
          sys.props.get("SE_CHROMEDRIVER_MIRROR_URL") should be(Some(s"???"))
          sys.props.get("SE_FIREFOX_MIRROR_URL")      should be(Some(s"$artifactoryBaseUrl/firefox-browser/"))
          sys.props.get("SE_GECKODRIVER_MIRROR_URL")  should be(Some(s"$artifactoryBaseUrl/firefox-browser/"))
          sys.props.get("SE_MSEDGE_MIRROR_URL")       should be(Some(s"$artifactoryBaseUrl/edge-browser/"))
          sys.props.get("SE_MSEDGEDRIVER_MIRROR_URL") should be(Some(s"$artifactoryBaseUrl/edge-driver/"))
          stub[WebDriver]
        }
        sys.props.get("SE_CHROME_MIRROR_URL")       should be(Some("???"))
        sys.props.get("SE_CHROMEDRIVER_MIRROR_URL") should be(Some("???"))
        sys.props.get("SE_FIREFOX_MIRROR_URL")      should be(None)
        sys.props.get("SE_GECKODRIVER_MIRROR_URL")  should be(None)
        sys.props.get("SE_MSEDGE_MIRROR_URL")       should be(None)
        sys.props.get("SE_MSEDGEDRIVER_MIRROR_URL") should be(None)
      }
    }

    "throw an exception if artifactory is unavailable and get base url from ARTIFACTORY_URI" in {
      withSystemProperties("ARTIFACTORY_URI" -> "http://localhost") {
        val exception = intercept[Exception] {
          SourceBrowserBinariesFromArtifactory {
            stub[WebDriver]
          }
        }
        exception.getMessage should be("Artifactory unreachable. Are you connected to VPN? (http://localhost)")
      }
    }

    "be able to be disabled" in {
      withSystemProperties("browser.option.downloadFromArtifactory" -> "false") {
        SourceBrowserBinariesFromArtifactory {
          sys.props.get("SE_CHROME_MIRROR_URL")       should be(None)
          sys.props.get("SE_CHROMEDRIVER_MIRROR_URL") should be(None)
          sys.props.get("SE_FIREFOX_MIRROR_URL")      should be(None)
          sys.props.get("SE_GECKODRIVER_MIRROR_URL")  should be(None)
          sys.props.get("SE_MSEDGE_MIRROR_URL")       should be(None)
          sys.props.get("SE_MSEDGEDRIVER_MIRROR_URL") should be(None)
          stub[WebDriver]
        }
      }
    }

    "skip artifactory availability check when disabled" in {
      withSystemProperties(
        "ARTIFACTORY_URI"                        -> "http://localhost",
        "browser.option.downloadFromArtifactory" -> "false"
      ) {
        noException should be thrownBy {
          SourceBrowserBinariesFromArtifactory {
            stub[WebDriver]
          }
        }
      }
    }

    "skip artifactory availability check when everything is overridden" in {
      withSystemProperties(
        "ARTIFACTORY_URI"            -> "http://localhost", // which would error if checked
        "SE_CHROME_MIRROR_URL"       -> "???",
        "SE_CHROMEDRIVER_MIRROR_URL" -> "???",
        "SE_FIREFOX_MIRROR_URL"      -> "???",
        "SE_GECKODRIVER_MIRROR_URL"  -> "???",
        "SE_MSEDGE_MIRROR_URL"       -> "???",
        "SE_MSEDGEDRIVER_MIRROR_URL" -> "???"
      ) {
        noException should be thrownBy {
          SourceBrowserBinariesFromArtifactory {
            stub[WebDriver]
          }
        }
      }
    }
  }
}
