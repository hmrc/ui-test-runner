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

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class ScreenshotRegexSpec extends AnyWordSpec with Matchers {

  def sanitiseTestName(name: String): String =
    name.replaceAll("[^A-Za-z0-9\\s-]", "").trim.replaceAll("\\s+", "-")

  "withFixture" should {

    "replace a single space with a hyphen" in {
      sanitiseTestName("my test") shouldBe "my-test"
    }

    "replace multiple consecutive spaces with a single hyphen" in {
      sanitiseTestName("my   test") shouldBe "my-test"
    }

    "replace tab characters with a hyphen" in {
      sanitiseTestName("my\ttest") shouldBe "my-test"
    }

    "replace newline characters with a hyphen" in {
      sanitiseTestName("my\ntest") shouldBe "my-test"
    }

    "replace mixed whitespace with a single hyphen" in {
      sanitiseTestName("my \t\n test") shouldBe "my-test"
    }

    "remove special characters" in {
      sanitiseTestName("my-test!@#$%^&*()") shouldBe "my-test"
    }

    "remove punctuation from the test name" in {
      sanitiseTestName("user's input: valid?") shouldBe "users-input-valid"
    }

    "preserve alphanumeric characters" in {
      sanitiseTestName("myTest123") shouldBe "myTest123"
    }

    "preserve existing hyphens" in {
      sanitiseTestName("my-test-name") shouldBe "my-test-name"
    }

    "handle a test name with spaces and special characters" in {
      sanitiseTestName("Sign in & verify user (happy path)") shouldBe "Sign-in-verify-user-happy-path"
    }

    "return an empty string when given an empty string" in {
      sanitiseTestName("") shouldBe ""
    }

    "return an empty string when the name contains only special characters" in {
      sanitiseTestName("!@#$%") shouldBe ""
    }

    "handle a name that is already sanitised" in {
      sanitiseTestName("already-clean-123") shouldBe "already-clean-123"
    }

    "handle leading and trailing whitespace" in {
      sanitiseTestName("  test name  ") shouldBe "test-name"
    }
  }
}
