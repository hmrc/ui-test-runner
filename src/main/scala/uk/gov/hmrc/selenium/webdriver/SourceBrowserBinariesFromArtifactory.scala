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

import com.typesafe.scalalogging.LazyLogging
import org.openqa.selenium.WebDriver
import uk.gov.hmrc.uitestrunner.config.TestRunnerConfig

object SourceBrowserBinariesFromArtifactory extends LazyLogging {
  def apply(initWebDriver: => WebDriver): WebDriver = {
    val artifactoryBaseUrl = TestRunnerConfig.artifactoryBaseUrl

    val propertiesToOverride = Seq(
      "SE_CHROME_MIRROR_URL"       -> s"$artifactoryBaseUrl/chrome-browser/",
      "SE_CHROMEDRIVER_MIRROR_URL" -> s"$artifactoryBaseUrl/chrome-browser/",
      "SE_FIREFOX_MIRROR_URL"      -> s"$artifactoryBaseUrl/firefox-browser/",
      "SE_GECKODRIVER_MIRROR_URL"  -> s"$artifactoryBaseUrl/firefox-browser/",
      "SE_MSEDGE_MIRROR_URL"       -> s"$artifactoryBaseUrl/edge-browser/",
      "SE_MSEDGEDRIVER_MIRROR_URL" -> s"$artifactoryBaseUrl/edge-driver/"
    ).filterNot { case (key, _) =>
      sys.env.contains(key) || sys.props.contains(key)
    }

    if (!TestRunnerConfig.downloadBrowsersFromArtifactory || propertiesToOverride.isEmpty) {
      logger.info("Using default external or environment defined sources for browser binaries")
      return initWebDriver
    }

    logger.info(s"Configuring selenium to download any needed browser binaries from $artifactoryBaseUrl")

    // because selenium does not fail fast enough when it's unavailable
    if (isUnavailable(s"$artifactoryBaseUrl/api/system/ping")) {
      throw DriverFactoryException(s"Artifactory unreachable. Are you connected to VPN? ($artifactoryBaseUrl)")
    }

    logger.info(s"Temporarily overriding ${propertiesToOverride.map(_._1).mkString(", ")}")
    try {
      sys.props.addAll(propertiesToOverride)
      initWebDriver
    } finally
      propertiesToOverride.foreach { case (key, _) =>
        sys.props.remove(key)
      }
  }

  private def isUnavailable(healthcheck: String): Boolean = {
    val conn = new java.net.URL(healthcheck).openConnection().asInstanceOf[java.net.HttpURLConnection]
    conn.setConnectTimeout(1000)
    try {
      conn.getResponseCode
      false
    } catch {
      case _: java.net.SocketTimeoutException => true
      case _: java.net.UnknownHostException   => true
      case _: java.io.IOException             => true
    }
  }
}
