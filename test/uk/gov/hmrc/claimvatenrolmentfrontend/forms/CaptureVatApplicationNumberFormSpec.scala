/*
 * Copyright 2025 HM Revenue & Customs
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
import org.scalatest.wordspec.AnyWordSpec
import play.api.data.Form
import uk.gov.hmrc.claimvatenrolmentfrontend.models.VatApplicationNumber

class CaptureVatApplicationNumberFormSpec extends AnyWordSpec with Matchers {

  private val form: Form[VatApplicationNumber] = CaptureVatApplicationNumberForm.form

  private val invalidLength = "capture-vat-application-number.error.message.invalid_length"
  private val nothing       = "capture-vat-application-number.error.message.nothing"
  private val invalidFormat = "capture-vat-application-number.error.message.invalid_format"
  private val dataKey       = "vatApplicationNumber"

  "CaptureVatApplicationNumberForm .form" should {
    "bind successfully" when {
      "mapping a valid van number" in {
        val res = form.bind(Map(dataKey -> "123456789102"))

        res.value mustBe Some(VatApplicationNumber("123456789102"))
        res.errors.isEmpty mustBe true
      }

      "mapping a valid van number, removing any white spaces" in {
        val res = form.bind(Map(dataKey -> " 009 456789102 "))

        res.value mustBe Some(VatApplicationNumber("009456789102"))
        res.errors.isEmpty mustBe true
      }
    }

    "not bind successfully" when {
      "mapping data with invalid characters" in {
        val res = form.bind(Map(dataKey -> "123x 4567 8910"))

        res.value mustBe None
        res.errors.exists(error => error.message.equals(invalidFormat)) mustBe true
      }

      "mapping valid data with too few characters" in {
        val res = form.bind(Map(dataKey -> "12378910"))

        res.value mustBe None
        res.errors.exists(error => error.message.equals(invalidLength)) mustBe true
      }

      "mapping valid data with too many characters" in {
        val res = form.bind(Map(dataKey -> "1237895555510"))

        res.value mustBe None
        res.errors.exists(error => error.message.equals(invalidLength)) mustBe true
      }

      "mapping data which is only whitespace" in {
        val res = form.bind(Map(dataKey -> "            "))

        res.value mustBe None
        res.errors.exists(error => error.message.equals(nothing)) mustBe true
      }

      "mapping empty data" in {
        val res = form.bind(Map(dataKey -> ""))

        res.value mustBe None
        res.errors.exists(error => error.message.equals(nothing)) mustBe true
      }
    }
  }

}
