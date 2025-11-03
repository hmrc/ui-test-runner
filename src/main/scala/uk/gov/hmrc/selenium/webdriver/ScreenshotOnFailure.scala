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

import org.openqa.selenium.io.FileHandler.{copy, createDir}
import org.openqa.selenium.logging.LogType
import org.openqa.selenium.{OutputType, TakesScreenshot}
import org.scalatest.{Documenting, Outcome, TestSuite, TestSuiteMixin}

import java.io.File
import scala.jdk.CollectionConverters.CollectionHasAsScala

trait ScreenshotOnFailure extends TestSuiteMixin with Documenting { this: TestSuite =>

  abstract override def withFixture(test: NoArgTest): Outcome = {
    val testOutcome         = super.withFixture(test)
    val testName            = test.name.replaceAll(" ", "-").replaceAll(":", "")
    val screenshotName      = testName + ".png"
    val screenshotDirectory = "./target/test-reports/html-report/images/screenshots/"

    if (testOutcome.isExceptional) {
      outputAllBrowserLogs()
      captureScreenshot(screenshotName, screenshotDirectory)
      markup(s"<img src='images/screenshots/$screenshotName' />")
    }

    testOutcome
  }

  private def captureScreenshot(screenshotName: String, screenshotDirectory: String): Unit = {
    val tmpFile        = Driver.instance.asInstanceOf[TakesScreenshot].getScreenshotAs(OutputType.FILE)
    val screenshotFile = new File(screenshotDirectory, screenshotName)

    createDir(new File(screenshotDirectory))
    copy(tmpFile, screenshotFile)
  }

  private def outputAllBrowserLogs(): Unit = {
    val logTypes = Seq(LogType.BROWSER, LogType.DRIVER, LogType.CLIENT, LogType.PERFORMANCE)

    logTypes.foreach { logType =>
      try {
        val logs = Driver.instance.manage().logs().get(logType)
        if (!logs.getAll.isEmpty) {
          println(s"\n >>>>>>>>>> $logType Logs <<<<<<<<<<")
          logs.getAll.asScala.foreach { entry =>
            println(s"[${entry.getLevel}] ${entry.getTimestamp}: ${entry.getMessage}")
          }
        }
      } catch {
        case _: Exception => // Log type not supported by browser, skip
      }
    }
  }

}
