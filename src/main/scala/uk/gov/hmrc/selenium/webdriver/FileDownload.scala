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

import org.openqa.selenium.io.FileHandler.createDir
import org.openqa.selenium.io.Zip
import org.openqa.selenium.remote.SessionId
import org.openqa.selenium.remote.http.{Contents, HttpClient, HttpMethod, HttpRequest}
import play.api.libs.json.Json

import java.io.File
import java.net.URL
import java.util.concurrent.TimeUnit

trait FileDownload {

  def download(filename: String, downloadDirectory: String, sessionId: SessionId): Unit = {
    val url = "http://localhost:4444"
    val uri = s"/session/$sessionId/se/files"

    TimeUnit.MILLISECONDS.sleep(250)

    val fileList = (Json.parse(get(url, uri)) \ "value" \ "names").as[List[String]]

    if (fileList.contains(filename)) {
      val requestBody  = s"""{"name": "$filename"}"""
      val fileContents = (Json.parse(post(url, uri, requestBody)) \ "value" \ "contents").as[String]

      createDir(new File(downloadDirectory))
      Zip.unzip(fileContents, new File(downloadDirectory))
    }
  }

  private def get(url: String, uri: String): String = {
    val httpClient   = HttpClient.Factory.createDefault().createClient(new URL(url))
    val httpRequest  = new HttpRequest(HttpMethod.GET, uri)
    val httpResponse = httpClient.execute(httpRequest)

    Contents.string(httpResponse)
  }

  private def post(url: String, uri: String, body: String): String = {
    val httpClient   = HttpClient.Factory.createDefault().createClient(new URL(url))
    val httpRequest  = new HttpRequest(HttpMethod.POST, uri)
    httpRequest.setContent(Contents.utf8String(body))
    val httpResponse = httpClient.execute(httpRequest)

    Contents.string(httpResponse)
  }

}
