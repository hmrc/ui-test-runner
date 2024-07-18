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

import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import uk.gov.hmrc.selenium.webdriver.ResourceExtractor.{ResourceFileNotFoundException, extractExtensionResourcesAndReturnPath}

import java.io.File
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file.{FileVisitResult, Files, Path, SimpleFileVisitor}

class ResourceExtractorSpec extends AnyWordSpec with Matchers with BeforeAndAfterEach {
  private val targetPath      = "target/accessibility-assessment-extension"
  private val targetDirectory = new File(targetPath)

  override protected def beforeEach(): Unit = {
    val path = Path.of(targetPath)

    if (Files.exists(path)) {
      Files.walkFileTree(
        path,
        new SimpleFileVisitor[Path] {
          override def visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult = {
            Files.delete(file)
            FileVisitResult.CONTINUE
          }

          override def postVisitDirectory(dir: Path, exc: java.io.IOException): FileVisitResult = {
            Files.delete(dir)
            FileVisitResult.CONTINUE
          }
        }
      )
      Files.deleteIfExists(path)
    }
  }

  "extractExtensionResourcesAndReturnPath" should {
    "extract jar resources to target folder" in {
      targetDirectory.exists() shouldBe false

      val targetExtensionPath = extractExtensionResourcesAndReturnPath("/META-INF")

      targetExtensionPath                      should include regex ".*/target/accessibility-assessment-extension"
      targetDirectory.exists()               shouldBe true
      targetDirectory.isDirectory            shouldBe true
      targetDirectory.list().toList.nonEmpty shouldBe true
    }

    "fail to extract resources if resource path is null" in {
      targetDirectory.exists() shouldBe false

      assertThrows[ResourceFileNotFoundException](extractExtensionResourcesAndReturnPath(null))
    }

    "fail to extract resources if resource path does not exist" in {
      targetDirectory.exists() shouldBe false

      assertThrows[ResourceFileNotFoundException](extractExtensionResourcesAndReturnPath("/i-dont-exist"))
    }
  }
}
