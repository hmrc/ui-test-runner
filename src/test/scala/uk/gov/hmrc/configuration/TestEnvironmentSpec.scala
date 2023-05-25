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

package uk.gov.hmrc.configuration

import com.typesafe.config.ConfigFactory
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class TestEnvironmentSpec extends AnyWordSpec with Matchers {

  "TestEnvironment" should {

    "return local url" in {
      val url = TestEnvironment.url("service-frontend") + "/local"

      url shouldBe "http://localhost:1234/test/local"
    }

    "return dev url" in {
      System.setProperty("environment", "dev")
      ConfigFactory.invalidateCaches()

      val url: String = TestEnvironment.url("service-frontend") + "/dev"

      url shouldBe "https://www.development.tax.service.gov.uk/test/dev"
    }

    "return qa url" in {
      System.setProperty("environment", "qa")
      ConfigFactory.invalidateCaches()

      val url: String = TestEnvironment.url("service-frontend") + "/qa"

      url shouldBe "https://www.qa.tax.service.gov.uk/test/qa"
    }

    "return staging url" in {
      System.setProperty("environment", "staging")
      ConfigFactory.invalidateCaches()

      val url: String = TestEnvironment.url("service-frontend") + "/staging"

      url shouldBe "https://www.staging.tax.service.gov.uk/test/staging"
    }

    "throw an exception for unknown environment" in {
      System.setProperty("environment", "test")
      ConfigFactory.invalidateCaches()

      val exception: Exception = intercept[Exception] {
        TestEnvironment.url("test-frontend") + "/test"
      }

      assert(exception.getMessage contains "No configuration setting found for key 'test'")
    }

    "throw an exception for unknown service" in {
      System.setProperty("environment", "local")
      ConfigFactory.invalidateCaches()

      val exception: Exception = intercept[Exception] {
        TestEnvironment.url("test-frontend") + "/test"
      }

      assert(exception.getMessage contains "No configuration setting found for key 'services.test-frontend'")
    }

  }

}
