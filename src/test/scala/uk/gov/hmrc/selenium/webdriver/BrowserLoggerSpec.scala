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

import org.mockito.Mockito._
import org.openqa.selenium.WebDriver
import org.openqa.selenium.logging.LogType
import org.openqa.selenium.remote.RemoteWebDriver
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import java.util.{Set => JSet}

class BrowserLoggerSpec extends AnyWordSpec with Matchers {

  "BrowserLogger.isSupported" should {
    "be false for a non-remote driver" in {
      BrowserLogger.isSupported(mock(classOf[WebDriver])) shouldBe false
    }

    "be true when the remote driver exposes log types" in {
      val remoteDriver = mock(classOf[RemoteWebDriver], RETURNS_DEEP_STUBS)
      when(remoteDriver.manage().logs().getAvailableLogTypes).thenReturn(JSet.of(LogType.BROWSER))
      BrowserLogger.isSupported(remoteDriver) shouldBe true
    }

    "be false when no log types are available" in {
      val remoteDriver = mock(classOf[RemoteWebDriver], RETURNS_DEEP_STUBS)
      when(remoteDriver.manage().logs().getAvailableLogTypes).thenReturn(JSet.of[String]())
      BrowserLogger.isSupported(remoteDriver) shouldBe false
    }

    "be false when logs() throws" in {
      val remoteDriver = mock(classOf[RemoteWebDriver], RETURNS_DEEP_STUBS)
      when(remoteDriver.manage().logs()).thenThrow(new RuntimeException("error"))
      BrowserLogger.isSupported(remoteDriver) shouldBe false
    }
  }

  "BrowserLogger.collectAndOutputLogs" should {
    "not throw when no log types are available" in {
      val remoteDriver = mock(classOf[RemoteWebDriver], RETURNS_DEEP_STUBS)
      when(remoteDriver.manage().logs().getAvailableLogTypes).thenReturn(JSet.of[String]())
      noException should be thrownBy BrowserLogger.collectAndOutputLogs(remoteDriver)
    }

    "not throw when the driver fails" in {
      val remoteDriver = mock(classOf[RemoteWebDriver], RETURNS_DEEP_STUBS)
      when(remoteDriver.manage().logs()).thenThrow(new RuntimeException("error"))
      noException should be thrownBy BrowserLogger.collectAndOutputLogs(remoteDriver)
    }

    "ignore non-remote drivers" in {
      noException should be thrownBy BrowserLogger.collectAndOutputLogs(mock(classOf[WebDriver]))
    }
  }
}