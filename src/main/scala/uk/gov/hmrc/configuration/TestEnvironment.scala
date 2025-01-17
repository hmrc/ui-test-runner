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

import com.typesafe.config.{Config, ConfigFactory}

import scala.concurrent.duration.{Duration, DurationLong}

object TestEnvironment {

  println(sys.props.mkString("\n"))

  System.setProperty("browser", "chrome")

  private val configuration: Config            = ConfigFactory.load()
  println(s">>>> Loaded " + configuration)

  // TODO move this to a specific Config object - also in library specific package, e.g. uk.gov.hmrc.ui.testrunner

  val securityAssessmentEnabled: Boolean =
    configuration.getBoolean("security.assessment")

  val zapHost: String =
    configuration.getString("zap.host")

  val browserLoggingEnabled: Boolean =
    //sys.props.get("browser.logging")
    configuration.getBoolean("browser2.logging")

  val browserOptionHeadLessEnabled: Boolean =
    //sys.props.get("browser.option.headless")
    configuration.getBoolean("browser2.option.headless")

  val browserType: Option[String] = {
    // TODO does this mean all `browser.` config can't move to HOCON?
    //sys.props.get("browser")
    println(">>>>>" + configuration.getString("browser2.type"))
    Option(configuration.getString("browser2.type"))
      .map(_.toLowerCase)
  }

  val browserVersion: String =
    //sys.props.get("browser.version")
    configuration.getString("browser2.version")

  val accessibilityAssessmentEnabled: Boolean =
    configuration.getBoolean("accessibility.assessment")

  val accessibilityTimeout: Duration =
    configuration.getInt("accessibility.timeout").millis

  private def environment: String              = configuration.getString("environment")
  private def defaultConfiguration: Config     = configuration.getConfig("local")
  private def environmentConfiguration: Config = configuration.getConfig(environment).withFallback(defaultConfiguration)

  def url(service: String): String = {
    val host = environment match {
      case "local" => s"$serviceHost:${servicePort(service)}"
      case _       => s"${environmentConfiguration.getString("services.host")}"
    }

    s"$host${serviceRoute(service)}"
  }

  private def serviceHost: String = environmentConfiguration.getString("services.host")

  private def servicePort(service: String): String = environmentConfiguration.getString(s"services.$service.port")

  private def serviceRoute(service: String): String =
    environmentConfiguration.getString(s"services.$service.productionRoute")

}
