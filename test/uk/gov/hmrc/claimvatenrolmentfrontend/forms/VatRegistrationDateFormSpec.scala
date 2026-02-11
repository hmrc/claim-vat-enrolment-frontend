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

package uk.gov.hmrc.claimvatenrolmentfrontend.forms

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.data.Form

import java.time.LocalDate

class VatRegistrationDateFormSpec extends AnyWordSpec with Matchers {
  val form: Form[LocalDate] = VatRegistrationDateForm.vatRegistrationDateForm

  "form with valid date" must {
    "bind successfully" in {
      val res = form.bind(Map("date.day" -> "5", "date.month" -> "5", "date.year" -> "2020"))
      res.errors.isEmpty mustBe true
    }
  }

  "form with date fields having spaces" must {
    "bind successfully" in {
      val res = form.bind(Map("date.day" -> " 5", "date.month" -> "5 ", "date.year" -> " 2020"))
      res.errors.isEmpty mustBe true
    }
  }

  "form with invalid data having spaces within a field" must {
    "not bind successfully" in {
      val res = form.bind(Map("date.day" -> "5", "date.month" -> "5", "date.year" -> "20 20"))
      res.errors.isEmpty mustBe false
    }
  }

  "form with empty data" must {
    "not bind successfully" in {
      val res = form.bind(Map.empty[String, String])
      res.errors.isEmpty mustBe false
    }
  }
}
