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

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.{JsSuccess, Json}

class VatApplicationNumberSpec extends AnyWordSpec with Matchers {

  private val model = VatApplicationNumber("123456789012")
  private val json  = Json.obj("value" -> "123456789012")

  "VatApplicationNumber" should {
    "write a VatApplicationNumber model to JSON" in {
      Json.toJson(model) mustBe json
    }
    "read valid Json to a VatApplicationNumber model" in {
      json.validate[VatApplicationNumber] mustBe JsSuccess(model)
    }
  }
}
