/*
 * Copyright 2023 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.uitestrunner.config

import com.typesafe.config.{Config, ConfigFactory}

import java.util.logging.Level
import scala.concurrent.duration.{Duration, DurationInt}
import scala.jdk.DurationConverters._

object TestRunnerConfig {

  private val chromeBrowserMirrorUrlValue  = "https://artefacts.tax.service.gov.uk/artifactory/chrome-browser/"
  private val chromeDriverMirrorUrlValue   = "https://artefacts.tax.service.gov.uk/artifactory/chrome-browser/"
  private val firefoxBrowserMirrorUrlValue = "https://artefacts.tax.service.gov.uk/artifactory/firefox-browser/"
  private val firefoxDriverMirrorUrlValue  = "https://artefacts.tax.service.gov.uk/artifactory/firefox-browser/"
  private val edgeBrowserMirrorUrlValue    = "https://artefacts.tax.service.gov.uk/artifactory/edge-browser/"
  private val edgeDriverMirrorUrlValue     = "https://artefacts.tax.service.gov.uk/artifactory/edge-driver/"

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

  def biDiEnabled: Boolean =
    sys.props.getOrElse("browser.bidi", "false").toBoolean

  def browserOptionHeadLessEnabled: Boolean =
    sys.props.getOrElse("browser.option.headless", "true").toBoolean

  def browserChromeVersion: String =
    if (sys.props.getOrElse("browser.usePreviousVersion", "false").toBoolean) {
      sys.props.getOrElse("browser.version", "128")
    } else {
      sys.props.getOrElse("browser.version", "136")
    }

  def browserEdgeVersion: String =
    sys.props.getOrElse("browser.version", "137")

  def browserFirefoxVersion: String =
    sys.props.getOrElse("browser.version", "136")

  def browserLoggingEnabled: Boolean =
    sys.props.getOrElse("browser.logging", "false").toBoolean

  def driverLoggingEnabled: Boolean =
    sys.props.getOrElse("driver.logging", "false").toBoolean

  def performanceLoggingEnabled: Boolean =
    sys.props.getOrElse("performance.logging", "false").toBoolean

  def browserLoggingLevel: Level =
    sys.props
      .get("browser.logging.level")
      .map(Level.parse)
      .getOrElse(Level.ALL)

  def driverLoggingLevel: Level =
    sys.props
      .get("driver.logging.level")
      .map(Level.parse)
      .getOrElse(Level.ALL)

  def performanceLoggingLevel: Level =
    sys.props
      .get("performance.logging.level")
      .map(Level.parse)
      .getOrElse(Level.ALL)

  def anyLoggingEnabled: Boolean =
    browserLoggingEnabled || driverLoggingEnabled || performanceLoggingEnabled

  def chromeBrowserMirrorUrl: Option[String] =
    if (useMirrorUrls) Some(chromeBrowserMirrorUrlValue) else None

  def chromeDriverMirrorUrl: Option[String] =
    if (useMirrorUrls) Some(chromeDriverMirrorUrlValue) else None

  def firefoxBrowserMirrorUrl: Option[String] =
    if (useMirrorUrls) Some(firefoxBrowserMirrorUrlValue) else None

  def firefoxDriverMirrorUrl: Option[String] =
    if (useMirrorUrls) Some(firefoxDriverMirrorUrlValue) else None

  def edgeBrowserMirrorUrl: Option[String] =
    if (useMirrorUrls) Some(edgeBrowserMirrorUrlValue) else None

  def edgeDriverMirrorUrl: Option[String] =
    if (useMirrorUrls) Some(edgeDriverMirrorUrlValue) else None

  def useMirrorUrls: Boolean =
    sys.props.get("use-mirror-urls").forall(_.toBoolean)
}
