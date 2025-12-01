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
import org.openqa.selenium.WebDriver
import org.openqa.selenium.logging.{LogEntries, LogEntry, LogType}
import org.openqa.selenium.remote.RemoteWebDriver
import uk.gov.hmrc.uitestrunner.config.TestRunnerConfig

import java.io.{File, FileWriter, PrintWriter}
import java.time.Instant
import java.time.format.DateTimeFormatter
import scala.jdk.CollectionConverters._
import scala.util.{Try, Using}

object BrowserLogger extends LazyLogging {

  private val LOG_DIR = "target/test-reports/browser-logs"

  private def logFileName: String = s"browser-${System.currentTimeMillis()}.log"

  def isSupported(driver: WebDriver): Boolean =
    Try {
      driver match {
        case remoteDriver: RemoteWebDriver =>
          val availableLogTypes = remoteDriver.manage().logs().getAvailableLogTypes.asScala
          availableLogTypes.nonEmpty
        case _                             =>
          false
      }
    }.getOrElse(false)

  def collectAndOutputLogs(driver: WebDriver): Unit =
    Try {
      driver match {
        case remoteDriver: RemoteWebDriver =>
          val logs              = remoteDriver.manage().logs()
          val availableLogTypes = logs.getAvailableLogTypes.asScala.toSet

          if (availableLogTypes.isEmpty) {
            logger.warn("Browser logging: No log types available")
            return
          }

          val logDir = new File(LOG_DIR)
          if (!logDir.exists()) {
            logDir.mkdirs()
          }

          val logFile = new File(logDir, logFileName)

          val allLogs = scala.collection.mutable.Map[String, List[LogEntry]]()

          if (TestRunnerConfig.browserLoggingEnabled && availableLogTypes.contains(LogType.BROWSER)) {
            collectLogType(logs, LogType.BROWSER, allLogs)
          }

          if (TestRunnerConfig.driverLoggingEnabled && availableLogTypes.contains(LogType.DRIVER)) {
            collectLogType(logs, LogType.DRIVER, allLogs)
          }

          if (TestRunnerConfig.performanceLoggingEnabled && availableLogTypes.contains(LogType.PERFORMANCE)) {
            collectLogType(logs, LogType.PERFORMANCE, allLogs)
          }

          val totalEntries = allLogs.values.map(_.size).sum
          writeLogsToFile(logFile, allLogs.toMap, remoteDriver.getCapabilities.getBrowserName)

          if (totalEntries > 0) {
            logger.info(s"Browser logs ($totalEntries entries) saved to: ${logFile.getAbsolutePath}")
          } else {
            logger.info(s"Browser logs (empty) saved to: ${logFile.getAbsolutePath}")
          }

        case _ =>
      }
    }.recover { case e: Exception =>
      logger.error(s"Error collecting browser logs: ${e.getMessage}")
    }

  private def collectLogType(
    logs: org.openqa.selenium.logging.Logs,
    logType: String,
    allLogs: scala.collection.mutable.Map[String, List[LogEntry]]
  ): Unit =
    Try {
      val entries: LogEntries = logs.get(logType)
      val logList             = entries.getAll.asScala.toList
      allLogs(logType) = logList
    }.recover { case e: Exception =>
    }

  private def writeLogsToFile(file: File, logs: Map[String, List[LogEntry]], browserName: String): Unit =
    Using(new PrintWriter(new FileWriter(file))) { writer =>
      writer.write("=" * 80 + "\n")
      writer.write(s"Browser Logs - $browserName\n")
      writer.write("=" * 80 + "\n")
      writer.write(s"Generated: ${DateTimeFormatter.ISO_INSTANT.format(Instant.now())}\n")
      writer.write(s"Total log types: ${logs.size}\n")
      writer.write(s"Total entries: ${logs.values.map(_.size).sum}\n")
      writer.write("=" * 80 + "\n")
      writer.write("\n")

      if (logs.values.forall(_.isEmpty)) {
        writer.write("No log entries found.\n")
        writer.write("\n")
        writer.write("This may indicate that:\n")
        writer.write("1. LoggingPreferences were not set correctly in browser options\n")
        writer.write("2. Logs were consumed by another process\n")
        writer.write("3. No browser activity occurred during the test\n")
        writer.write("\n")
        writer.write("To disable specific log types, set:\n")
        writer.write("  - browser.logging=false (disable browser console)\n")
        writer.write("  - driver.logging=false (disable WebDriver commands)\n")
        writer.write("  - performance.logging=false (disable network/performance)\n")
        writer.write("\n")
        writer.write("To adjust log levels, set:\n")
        writer.write("  - browser.logging.level=INFO\n")
        writer.write("  - driver.logging.level=INFO\n")
        writer.write("  - performance.logging.level=INFO\n")
      } else {
        logs.foreach { case (logType, entries) =>
          if (entries.nonEmpty) {
            writer.write(s"--- $logType Logs (${entries.size} entries) ---\n")
            writer.write("\n")

            entries.foreach { entry =>
              val timestamp = DateTimeFormatter.ISO_INSTANT.format(Instant.ofEpochMilli(entry.getTimestamp))
              writer.write(s"[$timestamp] [${entry.getLevel}] ${entry.getMessage}\n")
            }

            writer.write("\n")
            writer.write("-" * 80 + "\n")
            writer.write("\n")
          }
        }
      }

      writer.write("=" * 80 + "\n")
      writer.write("End of Browser Logs\n")
      writer.write("=" * 80 + "\n")
    }.recover { case e: Exception =>
      logger.error(s"Failed to write logs to file: ${e.getMessage}")
    }
}
