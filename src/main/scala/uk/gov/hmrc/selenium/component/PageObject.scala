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

package uk.gov.hmrc.selenium.component

import org.openqa.selenium.{By, Keys, WebDriver, WebElement}
import org.openqa.selenium.support.ui.{ExpectedConditions, FluentWait, Select, Wait}
import uk.gov.hmrc.selenium.webdriver.Driver

import java.time.Duration

trait PageObject {

  private def fluentWait: Wait[WebDriver] = new FluentWait[WebDriver](Driver.instance)
    .withTimeout(Duration.ofSeconds(3))
    .pollingEvery(Duration.ofSeconds(1))

  protected def click(locator: By): Unit = {
    waitForElementToBePresent(locator)
    findElement(locator).click()
  }

  protected def get(url: String): Unit =
    Driver.instance.get(url)

  protected def getCurrentUrl: String =
    Driver.instance.getCurrentUrl

  protected def getPageSource: String =
    Driver.instance.getPageSource

  protected def getText(locator: By): String = {
    waitForElementToBePresent(locator)
    findElement(locator).getText
  }

  protected def getTitle: String =
    Driver.instance.getTitle

  protected def sendKeys(locator: By, value: String): Unit = {
    clear(locator)
    findElement(locator).sendKeys(value)
  }

  protected def sendKeys(locator: By, keys: Keys*): Unit = {
    clear(locator)
    val element = findElement(locator)
    keys.foreach(key => element.sendKeys(key))
  }

  protected def selectCheckbox(locator: By): Unit =
    if (!isSelected(locator))
      click(locator)

  protected def deselectCheckbox(locator: By): Unit =
    if (isSelected(locator))
      click(locator)

  protected def selectByValue(locator: By, value: String): Unit = {
    waitForElementToBePresent(locator)
    val select: Select = new Select(findElement(locator))
    select.selectByValue(value)
  }

  protected def deselectByValue(locator: By, value: String): Unit = {
    waitForElementToBePresent(locator)
    val select: Select = new Select(findElement(locator))
    select.deselectByValue(value)
  }

  protected def selectByVisibleText(locator: By, value: String): Unit = {
    waitForElementToBePresent(locator)
    val select: Select = new Select(findElement(locator))
    select.selectByVisibleText(value)
  }

  protected def deselectByVisibleText(locator: By, value: String): Unit = {
    waitForElementToBePresent(locator)
    val select: Select = new Select(findElement(locator))
    select.deselectByVisibleText(value)
  }

  private def clear(locator: By): Unit = {
    waitForElementToBePresent(locator)
    findElement(locator).clear()
  }

  private def findElement(locator: By): WebElement =
    Driver.instance.findElement(locator)

  private def isSelected(locator: By): Boolean =
    findElement(locator).isSelected

  private def waitForElementToBePresent(locator: By): WebElement =
    fluentWait.until(ExpectedConditions.presenceOfElementLocated(locator))

}
