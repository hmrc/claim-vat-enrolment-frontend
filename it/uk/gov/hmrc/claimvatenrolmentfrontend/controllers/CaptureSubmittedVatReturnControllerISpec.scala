/*
 * Copyright 2021 HM Revenue & Customs
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

package uk.gov.hmrc.claimvatenrolmentfrontend.controllers

import java.time.Instant

import play.api.libs.json.{JsObject, Json}
import play.api.test.Helpers._
import reactivemongo.play.json._
import uk.gov.hmrc.claimvatenrolmentfrontend.assets.TestConstants._
import uk.gov.hmrc.claimvatenrolmentfrontend.models.VatKnownFacts
import uk.gov.hmrc.claimvatenrolmentfrontend.repositories.JourneyDataRepository.{vatKnownFactsReads, vatKnownFactsWrites}
import uk.gov.hmrc.claimvatenrolmentfrontend.stubs.AuthStub
import uk.gov.hmrc.claimvatenrolmentfrontend.utils.ComponentSpecHelper
import uk.gov.hmrc.claimvatenrolmentfrontend.views.CaptureSubmittedVatReturnViewTests

import scala.concurrent.ExecutionContext.Implicits.global

class CaptureSubmittedVatReturnControllerISpec extends ComponentSpecHelper with CaptureSubmittedVatReturnViewTests with AuthStub {

  s"GET /$testJourneyId/submitted-vat-return" should {
    lazy val result = {
      stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
      get(s"/$testJourneyId/submitted-vat-return")
    }

    "return OK" in {
      result.status mustBe OK
    }

    testCaptureSubmittedVatReturnViewTests(result)
  }


  s"POST /$testJourneyId/submitted-vat-return" should {
    "redirect to CaptureBox5Figure" when {
      "the user selects yes" in {
        stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
        await(journeyDataRepository.insertJourneyVatNumber(testJourneyId, testInternalId, testVatNumber))
        lazy val result = post(s"/$testJourneyId/submitted-vat-return")("vat_return" -> "yes")

        result must have(
          httpStatus(SEE_OTHER),
          redirectUri(routes.CaptureBox5FigureController.show(testJourneyId).url)
        )
      }
    }

    "redirect to Check Your Answers Page" when {
      "the user changes their answer to no" in {
        stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
        await(journeyDataRepository.collection.insert(true).one(
          Json.obj(
            "_id" -> testJourneyId,
            "authInternalId" -> testInternalId,
            "creationTimestamp" -> Json.obj("$date" -> Instant.now.toEpochMilli)
          ) ++ Json.toJsObject(testFullVatKnownFacts)
        ))
        lazy val result = post(s"/$testJourneyId/submitted-vat-return")("vat_return" -> "no")

        result must have(
          httpStatus(SEE_OTHER),
          redirectUri(routes.CheckYourAnswersController.show(testJourneyId).url)
        )
        await(
          journeyDataRepository.collection.find[JsObject, VatKnownFacts](
            Json.obj("_id" -> testJourneyId),
            None
          ).one[VatKnownFacts]
        ) mustBe Some(testVatKnownFactsNoReturns)
      }

      "the user selects no" in {
        stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
        await(journeyDataRepository.collection.insert(true).one(
          Json.obj(
            "_id" -> testJourneyId,
            "authInternalId" -> testInternalId,
            "creationTimestamp" -> Json.obj("$date" -> Instant.now.toEpochMilli)
          ) ++ Json.obj("vatRegPostcode" -> testPostcode.stringValue, "vatRegistrationDate" -> testVatRegDate, "vatNumber" -> testVatNumber)
        ))
        lazy val result = post(s"/$testJourneyId/submitted-vat-return")("vat_return" -> "no")

        result must have(
          httpStatus(SEE_OTHER),
          redirectUri(routes.CheckYourAnswersController.show(testJourneyId).url)
        )
        await(
          journeyDataRepository.collection.find[JsObject, VatKnownFacts](
            Json.obj("_id" -> testJourneyId),
            None
          ).one[VatKnownFacts]
        ) mustBe Some(testVatKnownFactsNoReturns)
      }
    }
  }

  "return a view with errors" when {
    "the user submits an empty form" should {
      lazy val result = {
        stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
        post(s"/$testJourneyId/submitted-vat-return")()
      }

      "return a BAD_REQUEST" in {
        result.status mustBe BAD_REQUEST
      }

      testCaptureSubmittedVatReturnErrorViewTests(result)
    }
  }

}


