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

package uk.gov.hmrc.uitestrunner.config

import com.typesafe.config.{Config, ConfigFactory}

import scala.concurrent.duration.{Duration, DurationInt}
import scala.jdk.DurationConverters._

object TestRunnerConfig {

  // Everything is a `def`` so that tests can invalidate the config
  private def configuration: Config =
    ConfigFactory.load()

  def securityAssessmentEnabled: Boolean =
    configuration.getBoolean("security.assessment")

  def zapHost: String =
    configuration.getString("zap.host")

  def accessibilityAssessmentEnabled: Boolean =
    configuration.getBoolean("accessibility.assessment")

  def accessibilityTimeout: Duration =
    // For backward compatibility, fallback to Int definition
    scala.util
      .Try(configuration.getDuration("accessibility.timeout").toScala)
      .getOrElse(configuration.getInt("accessibility.timeout").millis)

  // Since there is a system property "browser" which is a String
  // this is incompatible with HOCON where browser.logger etc mean browser is an Object
  // For now stick with system properties only

  def browserType: Option[String] =
    sys.props
      .get("browser")
      .map(_.toLowerCase)

  def browserLoggingEnabled: Boolean =
    sys.props.getOrElse("browser.logging", "false").toBoolean

  def browserOptionHeadLessEnabled: Boolean =
    sys.props.getOrElse("browser.option.headless", "true").toBoolean

  def browserChromeVersion: String =
    sys.props.getOrElse("browser.version", "136")

  def browserEdgeVersion: String =
    sys.props.getOrElse("browser.version", "137")

  def browserFirefoxVersion: String =
    sys.props.getOrElse("browser.version", "136")
}
