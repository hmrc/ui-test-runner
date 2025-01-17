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
import org.openqa.selenium.logging.{LogEntries, LogType}
import org.openqa.selenium.remote.RemoteWebDriver
import uk.gov.hmrc.configuration.TestEnvironment

import java.util.concurrent.TimeUnit
import scala.jdk.CollectionConverters.CollectionHasAsScala

trait Browser extends LazyLogging {

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
  protected def quitBrowser(): Unit = {
    if (Driver.instance != null) {
      outputBrowserLogs()
      TimeUnit.MILLISECONDS.sleep(TestEnvironment.accessibilityTimeout.toMillis)
      Driver.instance.quit()
    }
  }

  private def outputBrowserLogs(): Unit =
    if (TestEnvironment.browserLoggingEnabled) {
      val capabilities = Driver.instance.asInstanceOf[RemoteWebDriver].getCapabilities
      val browserName  = capabilities.getBrowserName

      if (browserName == "chrome") {
        val logs: LogEntries = Driver.instance.manage().logs().get(LogType.BROWSER)
        for (entry <- logs.getAll.asScala)
          logger.info(s"${entry.getLevel} ${entry.getMessage}")
      }
    }
}
