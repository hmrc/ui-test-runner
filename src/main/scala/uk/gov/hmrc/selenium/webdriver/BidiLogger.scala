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
import org.openqa.selenium.bidi.log.{ConsoleLogEntry, JavascriptLogEntry}
import org.openqa.selenium.remote.RemoteWebDriver
import uk.gov.hmrc.uitestrunner.config.TestRunnerConfig

import java.io.{File, PrintWriter}
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.concurrent.ConcurrentLinkedQueue
import scala.jdk.CollectionConverters._
import scala.util.{Try, Using}

object BidiLogger extends LazyLogging {

  private val consoleEntries = new ConcurrentLinkedQueue[ConsoleLogEntry]()
  private val jsErrorEntries = new ConcurrentLinkedQueue[JavascriptLogEntry]()

  private var consoleHandlerId: Option[String] = None
  private var jsErrorHandlerId: Option[String] = None

  def attach(driver: RemoteWebDriver): Unit = {
    if (TestRunnerConfig.bidiConsoleLoggingEnabled) {
      attachConsoleHandler(driver)
    } else {
      logger.info("BiDi Console logging: Disabled")
    }

    if (TestRunnerConfig.bidiJsErrorLoggingEnabled) {
      attachJsErrorHandler(driver)
    } else {
      logger.info("BiDi JS error logging: Disabled")
    }
  }

  private def attachConsoleHandler(driver: RemoteWebDriver): Unit =
    Try {
      val id = driver.script().addConsoleMessageHandler { (entry: ConsoleLogEntry) =>
        consoleEntries.add(entry)
        logger.debug(s"[console] ${entry.getText}")
      }
      consoleHandlerId = Some(id)
      logger.info("BiDi console message handler attached")
    }.recover { case e: Exception =>
      logger.warn(s"Failed to attach BiDi console handler: ${e.getMessage}", e)
    }

  private def attachJsErrorHandler(driver: RemoteWebDriver): Unit =
    Try {
      val id = driver.script().addJavaScriptErrorHandler { (entry: JavascriptLogEntry) =>
        jsErrorEntries.add(entry)
        logger.debug(s"[js-error] ${entry.getText}")
      }
      jsErrorHandlerId = Some(id)
      logger.info("BiDi JavaScript error handler attached")
    }.recover { case e: Exception =>
      logger.warn(s"Failed to attach BiDi JS error handler: ${e.getMessage}", e)
    }

  def detachAndFlush(driver: RemoteWebDriver, outputFile: File): Unit = {
    detachHandlers(driver)
    writeLogFile(outputFile)
    consoleEntries.clear()
    jsErrorEntries.clear()
  }

  private def detachHandlers(driver: RemoteWebDriver): Unit = {
    consoleHandlerId.foreach { id =>
      Try(driver.script().removeConsoleMessageHandler(id)).recover { case e: Exception =>
        logger.warn(s"Failed to remove BiDi console handler: ${e.getMessage}")
      }
    }
    consoleHandlerId = None

    jsErrorHandlerId.foreach { id =>
      Try(driver.script().removeJavaScriptErrorHandler(id)).recover { case e: Exception =>
        logger.warn(s"Failed to remove BiDi JS error handler: ${e.getMessage}")
      }
    }
    jsErrorHandlerId = None
  }

  private def writeLogFile(outputFile: File): Unit = {

    Using(new PrintWriter(outputFile, "UTF-8")) { writer =>
      if (!consoleEntries.isEmpty) {
        writer.println("--- Console Logs ---")
        consoleEntries.asScala.foreach { entry =>
          val ts = DateTimeFormatter.ISO_INSTANT.format(Instant.ofEpochMilli(entry.getTimestamp))
          writer.println(s"[$ts] ${entry.getText}")
        }
        writer.println()
      }

      if (!jsErrorEntries.isEmpty) {
        writer.println("--- JavaScript Error Logs ---")
        jsErrorEntries.asScala.foreach { entry =>
          val ts = DateTimeFormatter.ISO_INSTANT.format(Instant.ofEpochMilli(entry.getTimestamp))
          writer.println(s"[$ts] ${entry.getText}")
        }
      }

      if (consoleEntries.isEmpty && jsErrorEntries.isEmpty) {
        writer.println("No BiDi log entries captured.")
      }
    }

    logger.info(
      s"BiDi logs (console=${consoleEntries.size()}, jsErrors=${jsErrorEntries.size()}) saved to: ${outputFile.getAbsolutePath}"
    )
  }
}
