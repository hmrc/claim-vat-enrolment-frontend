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


class CaptureVatApplicationNumberFormSpec extends AnyWordSpec with Matchers {
  val form: Form[String] = CaptureVatApplicationNumberForm.form

  val invalidLength = "capture-vat-application-number.error.message.invalid_length"
  val nothing = "capture-vat-application-number.error.message.nothing"
  val invalidFormat = "capture-vat-application-number.error.message.invalid_format"

  "form with valid van number" must {
    "bind successfully" in {
      val res = form.bind(Map("vatApplicationNumber" -> "123456789102"))
      res.errors.isEmpty mustBe true
    }
  }

  "form with valid van number having trailing spaces" must {
    "bind successfully" in {
      val res = form.bind(Map("vatApplicationNumber" -> " 009 456789102 "))
      res.errors.isEmpty mustBe true
    }
  }

  "form with invalid data having some characters within a field" must {
    "not bind successfully" in {

      val res = form.bind(Map("vatApplicationNumber" -> "123x 4567 8910"))
      res.errors.isEmpty mustBe false
      res.errors.map(error => error.message.equals(invalidFormat)) mustBe List(true)
    }
  }

  "form with invalid data having less length" must {
    "not bind successfully" in {
      val res = form.bind(Map("vatApplicationNumber" -> "12378910"))
      res.errors.isEmpty mustBe false
      res.errors.map(error => error.message.equals(invalidLength)) mustBe List(true)
    }
  }

  "form with invalid data having more length" must {
    "not bind successfully" in {
      val res = form.bind(Map("vatApplicationNumber" -> "1237895555510"))
      res.errors.isEmpty mustBe false
      res.errors.map(error => error.message.equals(invalidLength)) mustBe List(true)
    }
  }

  "form with empty spaces for the field" must {
    "not bind successfully" in {
      val res = form.bind(Map("vatApplicationNumber" -> "   "))
      res.errors.isEmpty mustBe false
      res.errors.map(error => error.message.equals(nothing)) mustBe List(true)
    }
  }

  "form with empty data" must {
    "not bind successfully" in {
      val res = form.bind(Map.empty[String, String])
      res.errors.isEmpty mustBe false
    }
  }
}
