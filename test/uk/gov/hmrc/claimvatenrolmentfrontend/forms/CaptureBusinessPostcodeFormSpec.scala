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

package uk.gov.hmrc.claimvatenrolmentfrontend.forms

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class CaptureBusinessPostcodeFormSpec extends AnyWordSpecLike with Matchers{

  "The CaptureBusinessPostcodeForm" should {
    "validate the form correctly with valid post code with no spaces" in {
      val validData = Map("business_postcode" -> "AA11AA")
      val result = CaptureBusinessPostcodeForm.form.bind(validData)
      result.hasErrors mustBe false
    }

    "validate the form correctly with valid post code with spaces" in {
      val validData = Map("business_postcode" -> " A A 1 1 A A ")
      val result = CaptureBusinessPostcodeForm.form.bind(validData)
      result.hasErrors mustBe false
    }

    "not validate the form correctly with empty postcode" in {
      val validData = Map("business_postcode" -> " ")
      val result = CaptureBusinessPostcodeForm.form.bind(validData)
      result.hasErrors mustBe true

      result("business_postcode").error.fold(fail("was expecting an error")){ error =>
        error.message mustBe "capture-business-postcode.error.emptyPostcode"
      }
    }

    "not validate the form correctly with invalid postcode" in {
      val validData = Map("business_postcode" -> " A A 1 1 A A B ")
      val result = CaptureBusinessPostcodeForm.form.bind(validData)
      result.hasErrors mustBe true

      result("business_postcode").error.fold(fail("was expecting an error")){ error =>
        error.message mustBe "capture-business-postcode.error.emptyPostcode"
      }
    }
  }

}
