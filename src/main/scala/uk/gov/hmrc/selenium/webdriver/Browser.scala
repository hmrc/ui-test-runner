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

import scala.util.Try

trait Browser extends FileDownload with LazyLogging {

  protected def startBrowser(): Unit = {
    Driver.instance = new DriverFactory().initialise()
    Driver.instance.manage().window().maximize()
  }

  protected def quitBrowser(): Unit =
    if (Driver.instance != null) {
      val filename          = "accessibility-assessment"
      val downloadDirectory = s"./target/test-reports/$filename/axe-results"
      val sessionId         = Driver.instance.getSessionId

      Try(download(filename, downloadDirectory, sessionId)).failed
        .foreach(logger.error("Accessibility assessment: Failed to download results", _))

      Driver.instance.quit()
    }
}
