/*
 * Copyright 2022 HM Revenue & Customs
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

import play.api.libs.json.{JsObject, Json}
import play.api.test.Helpers._
import reactivemongo.play.json._
import uk.gov.hmrc.claimvatenrolmentfrontend.assets.TestConstants._
import uk.gov.hmrc.claimvatenrolmentfrontend.models.VatKnownFacts
import uk.gov.hmrc.claimvatenrolmentfrontend.repositories.JourneyDataRepository.{vatKnownFactsReads, vatKnownFactsWrites}
import uk.gov.hmrc.claimvatenrolmentfrontend.stubs.AuthStub
import uk.gov.hmrc.claimvatenrolmentfrontend.utils.ComponentSpecHelper
import uk.gov.hmrc.claimvatenrolmentfrontend.views.CaptureBusinessPostcodeViewTests

import java.time.Instant
import scala.concurrent.ExecutionContext.Implicits.global

class CaptureBusinessPostcodeControllerISpec extends ComponentSpecHelper with CaptureBusinessPostcodeViewTests with AuthStub {

  s"GET /$testJourneyId/business-postcode" should {
    lazy val result = {
      await(insertJourneyConfig(testJourneyId, testContinueUrl, testInternalId))
      stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
      get(s"/$testJourneyId/business-postcode")
    }

    "return OK" in {
      result.status mustBe OK
    }

    testCaptureBusinessPostcodeViewTests(result)

    "return NOT_FOUND" when {
      "the internal Ids do not match" in {
        stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
        await(insertJourneyConfig(testJourneyId, testContinueUrl, "testInternalId"))

        lazy val result = get(s"/$testJourneyId/business-postcode")

        result.status mustBe NOT_FOUND
      }
      "the journey Id has no internal Id stored" in {
        stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
        await(journeyConfigRepository.collection.insert(true).one(
          Json.obj(
            "_id" -> testJourneyId,
            "creationTimestamp" -> Json.obj("$date" -> Instant.now.toEpochMilli)
          ) ++ Json.toJsObject(testJourneyConfig)
        ))

        lazy val result = get(s"/$testJourneyId/business-postcode")

        result.status mustBe NOT_FOUND
      }
    }
  }

  s"POST /$testJourneyId/business-postcode" should {
    "redirect to CaptureSubmittedVatReturn" when {
      "the postcode contains a space" in {
        stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
        lazy val result = post(s"/$testJourneyId/business-postcode")("business_postcode" -> "ZZ1 1ZZ")
        await(journeyDataRepository.insertJourneyVatNumber(testJourneyId, testInternalId, testVatNumber))

        result must have(
          httpStatus(SEE_OTHER),
          redirectUri(routes.CaptureSubmittedVatReturnController.show(testJourneyId).url)
        )
      }

      "the postcode does not contain a space" in {
        stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
        lazy val result = post(s"/$testJourneyId/business-postcode")("business_postcode" -> "ZZ11ZZ")
        await(journeyDataRepository.insertJourneyVatNumber(testJourneyId, testInternalId, testVatNumber))

        result must have(
          httpStatus(SEE_OTHER),
          redirectUri(routes.CaptureSubmittedVatReturnController.show(testJourneyId).url)
        )
      }
    }

    "return a view with errors" when {
      "the user has submitted an empty form" should {
        lazy val result = {
          stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
          post(s"/$testJourneyId/business-postcode")()
        }

        "return a BAD_REQUEST" in {
          result.status mustBe BAD_REQUEST
        }

        testCaptureBusinessPostcodeMissingErrorViewTests(result)
      }

      "the user has submitted an invalid postcode" should {
        lazy val result = {
          stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
          post(s"/$testJourneyId/business-postcode")("business_postcode" -> "invalid")
        }

        "return a BAD_REQUEST" in {
          result.status mustBe BAD_REQUEST
        }

        testCaptureBusinessPostcodeInvalidErrorViewTests(result)
      }
    }
  }

  "clicking the skip postcode link (GET /no-business-postcode)" should {
    "redirect to Submitted VAT Returns page" when {
      "there is no postcode in the database" in {
        stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
        await(journeyDataRepository.insertJourneyVatNumber(testJourneyId, testInternalId, testVatNumber))
        lazy val result = get(s"/$testJourneyId/no-business-postcode")

        result must have(
          httpStatus(SEE_OTHER),
          redirectUri(routes.CaptureSubmittedVatReturnController.show(testJourneyId).url)
        )

      }
    }

    "remove the postcode field and redirect to Submitted VAT Returns page" when {
      "there is a postcode in the database" in {
        stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
        await(journeyDataRepository.collection.insert(true).one(
          Json.obj(
            "_id" -> testJourneyId,
            "authInternalId" -> testInternalId,
            "creationTimestamp" -> Json.obj("$date" -> Instant.now.toEpochMilli)
          ) ++ Json.toJsObject(testVatKnownFactsNoReturns)
        ))
        lazy val result = get(s"/$testJourneyId/no-business-postcode")

        result must have(
          httpStatus(SEE_OTHER),
          redirectUri(routes.CaptureSubmittedVatReturnController.show(testJourneyId).url)
        )
        await(
          journeyDataRepository.collection.find[JsObject, VatKnownFacts](
            Json.obj("_id" -> testJourneyId),
            None
          ).one[VatKnownFacts]
        ) mustBe Some(testVatKnownFactsNoReturnsNoPostcode)
      }
    }
  }

}
