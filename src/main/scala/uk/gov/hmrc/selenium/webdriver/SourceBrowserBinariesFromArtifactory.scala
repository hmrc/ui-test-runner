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

class SourceBrowserBinariesFromArtifactory(sysEnv: Map[String, String], artifactoryBaseUrl: String) {
  private val systemProperties = Seq(
    "SE_CHROME_MIRROR_URL"       ->
      sysEnv.getOrElse("SE_CHROME_MIRROR_URL", s"$artifactoryBaseUrl/chrome-browser/"),
    "SE_CHROMEDRIVER_MIRROR_URL" ->
      sysEnv.getOrElse("SE_CHROMEDRIVER_MIRROR_URL", s"$artifactoryBaseUrl/chrome-browser/"),
    "SE_FIREFOX_MIRROR_URL"      ->
      sysEnv.getOrElse("SE_FIREFOX_MIRROR_URL", s"$artifactoryBaseUrl/firefox-browser/"),
    "SE_GECKODRIVER_MIRROR_URL"  ->
      sysEnv.getOrElse("SE_GECKODRIVER_MIRROR_URL", s"$artifactoryBaseUrl/firefox-browser/"),

    // TODO are these edge ones right? doesn't seem to match jenkins
    "SE_MSEDGE_MIRROR_URL"       ->
      sysEnv.getOrElse("SE_MSEDGE_MIRROR_URL", s"$artifactoryBaseUrl/edge-browser/"),
    "SE_MSEDGEDRIVER_MIRROR_URL" ->
      sysEnv.getOrElse("SE_MSEDGEDRIVER_MIRROR_URL", s"$artifactoryBaseUrl/edge-driver/")
  )

  def configureSeleniumManager(): Unit =
    sys.props.addAll(systemProperties)

  def configureSeleniumManagerTemporarily[T](block: => T): T = {
    val originalValues = systemProperties.map { case (key, _) => key -> sys.props.get(key) }
    try {
      configureSeleniumManager()
      block
    } finally
      originalValues.foreach {
        case (key, Some(value)) => sys.props.update(key, value)
        case (key, None)        => sys.props.remove(key)
      }
  }

  def checkArtifactoryIsAvailable(): this.type = {
    if (!isArtifactoryAvailable) {
      throw new RuntimeException(
        "Artifactory unreachable. Are you connected to VPN? Won't be able to download browser binaries from it, tests would hang."
      )
    }
    this
  }

  private lazy val isArtifactoryAvailable: Boolean = {
    val conn =
      new java.net.URL(s"$artifactoryBaseUrl/api/system/ping").openConnection().asInstanceOf[java.net.HttpURLConnection]
    conn.setConnectTimeout(1000)
    try {
      conn.getResponseCode
      true
    } catch {
      case _: java.net.SocketTimeoutException => false
      case _: java.net.UnknownHostException   => false
      case _: java.io.IOException             => false
    }
  }
}
