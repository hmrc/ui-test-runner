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

package uk.gov.hmrc.uitestrunner.config

import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import java.util.logging.Level

class TestRunnerConfigSpec extends AnyWordSpec with Matchers with BeforeAndAfterEach {

  private val props = Seq(
    "browser",
    "browser.bidi",
    "browser.bidi.console",
    "browser.bidi.jserrors",
    "browser.logging",
    "driver.logging",
    "performance.logging",
    "browser.logging.level",
    "browser.version",
    "browser.usePreviousVersion"
  )

  override def afterEach(): Unit = props.foreach(System.clearProperty)

  "TestRunnerConfig" should {
    "default all logging flags to false" in {
      TestRunnerConfig.biDiEnabled               shouldBe false
      TestRunnerConfig.bidiConsoleLoggingEnabled shouldBe false
      TestRunnerConfig.bidiJsErrorLoggingEnabled shouldBe false
      TestRunnerConfig.anyLoggingEnabled         shouldBe false
    }

    "read the BiDi flags from system properties" in {
      System.setProperty("browser.bidi", "true")
      System.setProperty("browser.bidi.console", "true")
      System.setProperty("browser.bidi.jserrors", "true")

      TestRunnerConfig.biDiEnabled               shouldBe true
      TestRunnerConfig.bidiConsoleLoggingEnabled shouldBe true
      TestRunnerConfig.bidiJsErrorLoggingEnabled shouldBe true
    }

    "not count BiDi flags as anyLoggingEnabled" in {
      System.setProperty("browser.bidi.console", "true")
      TestRunnerConfig.anyLoggingEnabled shouldBe false
    }

    Seq("browser.logging", "driver.logging", "performance.logging").foreach { prop =>
      s"report anyLoggingEnabled when only $prop is true" in {
        System.setProperty(prop, "true")
        TestRunnerConfig.anyLoggingEnabled shouldBe true
      }
    }

    "lowercase the browser type" in {
      System.setProperty("browser", "Chrome")
      TestRunnerConfig.browserType shouldBe Some("chrome")
    }

    "return None when browser is not set" in {
      TestRunnerConfig.browserType shouldBe None
    }

    "default logging level to ALL and parse overrides" in {
      TestRunnerConfig.browserLoggingLevel shouldBe Level.ALL
      System.setProperty("browser.logging.level", "SEVERE")
      TestRunnerConfig.browserLoggingLevel shouldBe Level.SEVERE
    }

    "use the previous Chrome version when requested" in {
      System.setProperty("browser.usePreviousVersion", "true")
      TestRunnerConfig.browserChromeVersion shouldBe "128"
    }

    "prefer an explicit browser.version" in {
      System.setProperty("browser.version", "140")
      TestRunnerConfig.browserChromeVersion shouldBe "140"
    }

    "throw for an invalid boolean" in {
      System.setProperty("browser.bidi", "maybe")
      an[IllegalArgumentException] should be thrownBy TestRunnerConfig.biDiEnabled
    }
  }
}
