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

package uk.gov.hmrc.claimvatenrolmentfrontend.models

import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.libs.json.{JsString, Json}
import uk.gov.hmrc.http.InternalServerException

class PostcodeSpec extends PlaySpec with GuiceOneAppPerSuite with MockitoSugar {
  private val standardPostcode: Postcode               = Postcode("AA1 1AA")
  private val postcodeWithNoWhitespace: Postcode       = Postcode("AA11AA")
  private val postcodeWithLeadingWhitespace: Postcode  = Postcode("    AA1 1AA")
  private val postcodeWithTrailingWhitespace: Postcode = Postcode("AA1 1AA    ")
  private val postcodeWithInternalWhitespace: Postcode = Postcode(" A A 1 1 A A ")

  private val testCases: Set[Postcode] =
    Set(standardPostcode, postcodeWithNoWhitespace, postcodeWithLeadingWhitespace, postcodeWithTrailingWhitespace, postcodeWithInternalWhitespace)

  "stringValue" should {
    "return the String value, unchanged" in {
      standardPostcode.stringValue mustBe "AA1 1AA"
      postcodeWithNoWhitespace.stringValue mustBe "AA11AA"
      postcodeWithLeadingWhitespace.stringValue mustBe "    AA1 1AA"
      postcodeWithTrailingWhitespace.stringValue mustBe "AA1 1AA    "
      postcodeWithInternalWhitespace.stringValue mustBe " A A 1 1 A A "
    }
  }

  "sanitisedPostcode" should {
    "return the String value formatted the standardized format" in {
      testCases.forall(_.sanitisedPostcode == "AA1 1AA") mustBe true
    }

    "throw an InternalServerException when a postcode has an invalid value" in {
      val exception = intercept[InternalServerException] {
        Postcode("not-a-postcode").sanitisedPostcode
      }

      exception.getMessage mustBe "Invalid postcode format: NOT-A-POSTCODE"
    }
  }

  "writes" should {
    "write only the stringValue as a Json String" in {
      Json.toJson(standardPostcode) mustBe JsString("AA1 1AA")
      Json.toJson(postcodeWithNoWhitespace) mustBe JsString("AA1 1AA")
      Json.toJson(postcodeWithLeadingWhitespace) mustBe JsString("AA1 1AA")
      Json.toJson(postcodeWithTrailingWhitespace) mustBe JsString("AA1 1AA")
      Json.toJson(postcodeWithInternalWhitespace) mustBe JsString("AA1 1AA")
    }
  }

}
