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

import com.typesafe.scalalogging.LazyLogging
import uk.gov.hmrc.uitestrunner.config.TestRunnerConfig

import java.util.concurrent.TimeUnit

trait Browser extends LazyLogging {

  private val collectedDrivers = scala.collection.mutable.Set[String]()

  protected def startBrowser(): Unit = {
    Driver.instance = new DriverFactory().initialise()
    Driver.instance.manage().window().maximize()
  }

  /** TODO: Remove or refactor hard coded sleep (250ms) before browser quit function is called
   *
   * This is currently required to ensure that file downloads that are triggered by the accessibility assessment
   * extension are not corrupted because the browser has quit before the file download is completed.
   *
   * @see
   * https://www.selenium.dev/documentation/grid/configuration/cli_options/#complete-sample-code-in-java
   */
  protected def quitBrowser(): Unit =
    if (Driver.instance != null) {
      outputBrowserLogs()
      TimeUnit.MILLISECONDS.sleep(TestRunnerConfig.accessibilityTimeout.toMillis)
      Driver.instance.quit()
    }

  private def outputBrowserLogs(): Unit = {

    val driverSessionId = Driver.instance match {
      case remote: org.openqa.selenium.remote.RemoteWebDriver =>
        remote.getSessionId.toString
      case _                                                  =>
        System.identityHashCode(Driver.instance).toString
    }

    if (collectedDrivers.contains(driverSessionId)) {
      return
    }

    val browserLogsEnabled     = sys.props.get("browser.logging").contains("true")
    val driverLogsEnabled      = sys.props.get("driver.logging").contains("true")
    val performanceLogsEnabled = sys.props.get("performance.logging").contains("true")

    val anyLoggingEnabled = browserLogsEnabled || driverLogsEnabled || performanceLogsEnabled

    if (anyLoggingEnabled && BrowserLogger.isSupported(Driver.instance)) {
      BrowserLogger.collectAndOutputLogs(Driver.instance)
      collectedDrivers += driverSessionId
    }
  }
}
