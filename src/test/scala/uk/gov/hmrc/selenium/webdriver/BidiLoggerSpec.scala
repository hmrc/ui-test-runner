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

import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.{any, anyString}
import org.mockito.Mockito._
import org.openqa.selenium.bidi.log.{ConsoleLogEntry, JavascriptLogEntry}
import org.openqa.selenium.remote.RemoteWebDriver
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import java.io.File
import java.nio.file.Files
import java.util.function.Consumer

class BidiLoggerSpec extends AnyWordSpec with Matchers with BeforeAndAfterEach {

  private var driver: RemoteWebDriver = _
  private var outFile: File           = _

  override def beforeEach(): Unit = {
    driver = mock(classOf[RemoteWebDriver], RETURNS_DEEP_STUBS)
    when(driver.script().addConsoleMessageHandler(any[Consumer[ConsoleLogEntry]]())).thenReturn("console-id")
    when(driver.script().addJavaScriptErrorHandler(any[Consumer[JavascriptLogEntry]]())).thenReturn("js-id")
    outFile = Files.createTempFile("bidi", ".log").toFile
  }

  override def afterEach(): Unit = {
    Seq("browser.bidi.console", "browser.bidi.jserrors").foreach(System.clearProperty)
    // reset the singleton's state
    BidiLogger.detachAndFlush(mock(classOf[RemoteWebDriver], RETURNS_DEEP_STUBS), outFile)
    outFile.delete()
  }

  private def content: String = new String(Files.readAllBytes(outFile.toPath), "UTF-8")

  private def consoleEntry(text: String, ts: Long = 0L): ConsoleLogEntry = {
    val e = mock(classOf[ConsoleLogEntry])
    when(e.getText).thenReturn(text)
    when(e.getTimestamp).thenReturn(ts)
    e
  }

  private def jsEntry(text: String, ts: Long = 0L): JavascriptLogEntry = {
    val e = mock(classOf[JavascriptLogEntry])
    when(e.getText).thenReturn(text)
    when(e.getTimestamp).thenReturn(ts)
    e
  }

  "BidiLogger.attach" should {
    "attach nothing when both flags are off" in {
      BidiLogger.attach(driver)
      verify(driver.script(), never()).addConsoleMessageHandler(any[Consumer[ConsoleLogEntry]]())
      verify(driver.script(), never()).addJavaScriptErrorHandler(any[Consumer[JavascriptLogEntry]]())
    }

    "attach only the console handler when only console is on" in {
      System.setProperty("browser.bidi.console", "true")
      BidiLogger.attach(driver)
      verify(driver.script()).addConsoleMessageHandler(any[Consumer[ConsoleLogEntry]]())
      verify(driver.script(), never()).addJavaScriptErrorHandler(any[Consumer[JavascriptLogEntry]]())
    }

    "attach only the JS error handler when only jserrors is on" in {
      System.setProperty("browser.bidi.jserrors", "true")
      BidiLogger.attach(driver)
      verify(driver.script(), never()).addConsoleMessageHandler(any[Consumer[ConsoleLogEntry]]())
      verify(driver.script()).addJavaScriptErrorHandler(any[Consumer[JavascriptLogEntry]]())
    }

    "still attach the JS handler if the console handler fails and does not fail tests" in {
      System.setProperty("browser.bidi.console", "true")
      System.setProperty("browser.bidi.jserrors", "true")
      when(driver.script().addConsoleMessageHandler(any[Consumer[ConsoleLogEntry]]()))
        .thenThrow(new RuntimeException("error"))

      noException should be thrownBy BidiLogger.attach(driver)
      verify(driver.script()).addJavaScriptErrorHandler(any[Consumer[JavascriptLogEntry]]())
    }
  }

  "BidiLogger.detachAndFlush" should {
    "write a placeholder when nothing was captured" in {
      BidiLogger.detachAndFlush(driver, outFile)
      content should include("No BiDi log entries captured.")
    }

    "write captured console and JS error entries" in {
      System.setProperty("browser.bidi.console", "true")
      System.setProperty("browser.bidi.jserrors", "true")
      BidiLogger.attach(driver)

      val consoleCaptor = ArgumentCaptor.forClass(classOf[Consumer[ConsoleLogEntry]])
      val jsCaptor      = ArgumentCaptor.forClass(classOf[Consumer[JavascriptLogEntry]])
      verify(driver.script()).addConsoleMessageHandler(consoleCaptor.capture())
      verify(driver.script()).addJavaScriptErrorHandler(jsCaptor.capture())

      consoleCaptor.getValue.accept(consoleEntry("hello console"))
      jsCaptor.getValue.accept(jsEntry("TypeError: x"))

      BidiLogger.detachAndFlush(driver, outFile)

      content should include("--- Console Logs ---")
      content should include("[1970-01-01T00:00:00Z] hello console")
      content should include("--- JavaScript Error Logs ---")
      content should include("[1970-01-01T00:00:00Z] TypeError: x")
      content should not include "No BiDi log entries captured."
    }

    "remove the registered handlers" in {
      System.setProperty("browser.bidi.console", "true")
      System.setProperty("browser.bidi.jserrors", "true")
      BidiLogger.attach(driver)
      BidiLogger.detachAndFlush(driver, outFile)

      verify(driver.script()).removeConsoleMessageHandler("console-id")
      verify(driver.script()).removeJavaScriptErrorHandler("js-id")
    }

    "not remove handlers twice and clear entries between runs" in {
      System.setProperty("browser.bidi.console", "true")
      BidiLogger.attach(driver)
      val captor = ArgumentCaptor.forClass(classOf[Consumer[ConsoleLogEntry]])
      verify(driver.script()).addConsoleMessageHandler(captor.capture())
      captor.getValue.accept(consoleEntry("first"))

      BidiLogger.detachAndFlush(driver, outFile)
      BidiLogger.detachAndFlush(driver, outFile)

      verify(driver.script(), times(1)).removeConsoleMessageHandler(anyString())
      content should include("No BiDi log entries captured.")
      content should not include "first"
    }

    "still write the file if removing a handler fails" in {
      System.setProperty("browser.bidi.console", "true")
      BidiLogger.attach(driver)

      val script = driver.script()
      doThrow(new RuntimeException("error")).when(script).removeConsoleMessageHandler(anyString())

      noException should be thrownBy BidiLogger.detachAndFlush(driver, outFile)
      content should include("No BiDi log entries captured.")
    }

    "when the output file cannot be written does not break the test" in {
      val bad = new File("/dir/notthere/bidi.log")
      noException should be thrownBy BidiLogger.detachAndFlush(driver, bad)
    }
  }
}