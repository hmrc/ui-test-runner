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

import java.io.File
import java.util.concurrent.TimeUnit

import org.openqa.selenium.Dimension

trait Browser extends LazyLogging {

  private val collectedDrivers = scala.collection.mutable.Set[String]()

  protected def startBrowser(): Unit = {
    Driver.instance = new DriverFactory().initialise()
    configureWindowSize()

    if (bidiLoggingRequired) {
      Driver.instance match {
        case remote: org.openqa.selenium.remote.RemoteWebDriver =>
          BidiLogger.attach(remote)
        case _                                                  =>
      }
    }
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

      if (bidiLoggingRequired) {
        Driver.instance match {
          case remote: org.openqa.selenium.remote.RemoteWebDriver =>
            val file = new File("target/test-reports/browser-logs", s"bidi-${System.currentTimeMillis()}.log")
            file.getParentFile.mkdirs()
            BidiLogger.detachAndFlush(remote, file)
          case _                                                  =>
        }
      }

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

    if (TestRunnerConfig.anyLoggingEnabled && BrowserLogger.isSupported(Driver.instance)) {
      BrowserLogger.collectAndOutputLogs(Driver.instance)
      collectedDrivers += driverSessionId
    }
  }

  private def configureWindowSize(): Unit =
    if (TestRunnerConfig.browserOptionHeadLessEnabled || TestRunnerConfig.browserENVHeadLessEnabled)
      Driver.instance.manage().window().setSize(new Dimension(1920, 1080))
    else
      Driver.instance.manage().window().maximize()

  protected def bidiLoggingRequired: Boolean =
    TestRunnerConfig.biDiEnabled &&
      (TestRunnerConfig.bidiConsoleLoggingEnabled || TestRunnerConfig.bidiJsErrorLoggingEnabled)

}
