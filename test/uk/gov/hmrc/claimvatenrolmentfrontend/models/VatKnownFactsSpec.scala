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
import play.api.libs.json._
import uk.gov.hmrc.claimvatenrolmentfrontend.repositories.JourneyDataRepository
import uk.gov.hmrc.claimvatenrolmentfrontend.repositories.JourneyDataRepository._

import java.time.{LocalDate, Month}

class VatKnownFactsSpec extends PlaySpec with GuiceOneAppPerSuite with MockitoSugar {

  "ReturnsInformation.hasCompleteData" should {

    "return true when both boxFive and lastReturnMonth are defined" in {
      ReturnsInformation(Some("1000"), Some(Month.JANUARY)).hasCompleteData mustBe true
    }

    "return false when boxFive is missing" in {
      ReturnsInformation(None, Some(Month.JANUARY)).hasCompleteData mustBe false
    }

    "return false when lastReturnMonth is missing" in {
      ReturnsInformation(Some("1000"), None).hasCompleteData mustBe false
    }

    "return false when both boxFive and lastReturnMonth are missing" in {
      ReturnsInformation(None, None).hasCompleteData mustBe false
    }
  }

  "VatKnownFacts.hasCompleteJourneyData" should {

    def buildVatKnownFacts(
                           vatRegDate: Option[LocalDate],
                           optRetInfo: Option[ReturnsInformation],
                           formBundleRef: Option[String]
                     ): VatKnownFacts =
      VatKnownFacts(
        vatNumber   = "123123123",
        optPostcode = Some(Postcode("AA1 1AA")),
        vatRegistrationDate = vatRegDate,
        optReturnsInformation = optRetInfo,
        formBundleReference    = formBundleRef
      )

    "return 'true'" when {
      "date is defined and returnInfo has complete data" in {
        val returnInfo = ReturnsInformation(Some("1000"), Some(Month.FEBRUARY))
        buildVatKnownFacts(Some(LocalDate.now), Some(returnInfo), None).hasCompleteJourneyData mustBe true
      }
      "date is defined and returnInfo is incomplete but formBundleRef is defined" in {
        val incompleteReturnInfo = ReturnsInformation(Some("1000"), None) // incomplete
        buildVatKnownFacts(Some(LocalDate.now), Some(incompleteReturnInfo), Some("opt-ref")).hasCompleteJourneyData mustBe true
      }
      "date is defined, returnInfo is absent, and formBundleRef is defined" in {
        buildVatKnownFacts(Some(LocalDate.now), None, Some("opt-ref")).hasCompleteJourneyData mustBe true
      }
    }

    "return 'false'" when {
      "date is not defined (regardless of returnInfo/formBundleRef)" in {
        buildVatKnownFacts(None, Some(ReturnsInformation(Some("1000"), Some(Month.JANUARY))), None).hasCompleteJourneyData mustBe false
        buildVatKnownFacts(None, None, Some("ref")).hasCompleteJourneyData mustBe false
        buildVatKnownFacts(None, Some(ReturnsInformation(None, None)), Some("ref")).hasCompleteJourneyData mustBe false
      }
      "date is defined but both returnInfo is incomplete/absent and formBundleRef is not defined" in {
        val incompleteReturnInfo = ReturnsInformation(None, Some(Month.MARCH)) // incomplete
        buildVatKnownFacts(Some(LocalDate.now), Some(incompleteReturnInfo), None).hasCompleteJourneyData mustBe false

        buildVatKnownFacts(Some(LocalDate.now), None, None).hasCompleteJourneyData mustBe false
      }
    }
  }

  "check the json reads of vatKnownFacts" in {
    val json = Json.parse(
      s"""
         |{
         |    "vatNumber": "123123123",
         |    "vatRegistrationDate": "2021-01-01",
         |    "submittedVatReturn": true,
         |    "box5Figure": "1000.00",
         |    "lastMonthSubmitted": 1
         |}
         """.stripMargin)

    val expectedVatKnownFacts = VatKnownFacts(
      vatNumber = "123123123",
      vatRegistrationDate = Some(LocalDate.of(2021, 1, 1)),
      optPostcode = None,
      optReturnsInformation = Some(ReturnsInformation(
        boxFive = Some("1000.00"),
        lastReturnMonth = Some(Month.JANUARY))),
      formBundleReference = None
    )

    JourneyDataRepository.vatKnownFactsReads.reads(json) mustBe JsSuccess(expectedVatKnownFacts)
  }

  "check the json writes of vatKnownFacts" in {
    val vatKnownFacts = VatKnownFacts(
      vatNumber = "321321321",
      vatRegistrationDate = Some(LocalDate.of(2022, 1, 1)),
      optPostcode = Some(Postcode("AA1 1AA")),
      optReturnsInformation = Some(ReturnsInformation(
        boxFive = Some("1250.00"),
        lastReturnMonth = Some(Month.FEBRUARY))),
      formBundleReference = None
    )

    val expectedJson = Json.parse(
      s"""
         |{
         |    "vatNumber": "321321321",
         |    "vatRegistrationDate": "2022-01-01",
         |    "vatRegPostcode": "AA1 1AA",
         |    "submittedVatReturn": true,
         |    "box5Figure": "1250.00",
         |    "lastMonthSubmitted": 2,
         |    "formBundleReference": null
         |}
         """.stripMargin)

    Json.toJson(vatKnownFacts) mustBe expectedJson
  }

}
