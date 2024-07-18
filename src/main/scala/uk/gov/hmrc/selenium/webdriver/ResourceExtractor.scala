/*
 * Copyright 2024 HM Revenue & Customs
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

import java.nio.file.attribute.PosixFilePermissions
import java.nio.file.{FileSystems, Files, Path, StandardCopyOption}
import scala.jdk.CollectionConverters._
import scala.util.{Failure, Success, Try}

object ResourceExtractor {
  case class ResourceFileNotFoundException() extends Exception {
    override def getMessage: String = "Resource file does not exist."
  }

  def extractExtensionResourcesAndReturnPath(resourcePath: String): String = {
    val env = Map("create" -> "true").asJava

    val targetDir  = Path.of("target")
    val targetPath = targetDir.resolve("accessibility-assessment-extension")

    Try(getClass.getResource(resourcePath)) match {
      case Failure(_)           => throw new ResourceFileNotFoundException
      case Success(resourceUrl) =>
        if (!Files.exists(targetPath)) {
          val tempDir = Files.createDirectory(
            targetPath,
            PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rwxr-xr-x"))
          )

          Try(FileSystems.newFileSystem(resourceUrl.toURI, env)) match {
            case Failure(_)  => throw new ResourceFileNotFoundException
            case Success(fs) =>
              val pathInJar = fs.getPath(resourcePath)
              Files.walk(pathInJar).iterator().asScala.foreach { path =>
                val targetPath = tempDir.resolve(pathInJar.relativize(path).toString)
                if (Files.isDirectory(path)) {
                  Files.createDirectories(targetPath)
                } else {
                  Files.copy(path, targetPath, StandardCopyOption.REPLACE_EXISTING)
                }
              }
          }
        }
    }

    targetPath.toAbsolutePath.toString
  }
}
