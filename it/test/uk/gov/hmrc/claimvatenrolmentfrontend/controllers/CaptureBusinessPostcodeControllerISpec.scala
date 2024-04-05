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

package uk.gov.hmrc.claimvatenrolmentfrontend.controllers

import play.api.libs.json.Json
import play.api.test.Helpers._
import uk.gov.hmrc.claimvatenrolmentfrontend.assets.TestConstants._
import uk.gov.hmrc.claimvatenrolmentfrontend.repositories.JourneyDataRepository._
import uk.gov.hmrc.claimvatenrolmentfrontend.stubs.AuthStub
import uk.gov.hmrc.claimvatenrolmentfrontend.utils.JourneyMongoHelper
import uk.gov.hmrc.claimvatenrolmentfrontend.views.CaptureBusinessPostcodeViewTests

import java.time.Instant

class CaptureBusinessPostcodeControllerISpec extends JourneyMongoHelper with CaptureBusinessPostcodeViewTests with AuthStub {

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

    "Show an error page" when {
      "there is no journey config" in {
        stubAuth(OK, successfulAuthResponse(Some(testInternalId)))

        lazy val result = get(s"/$testJourneyId/business-postcode")

        result must have(
          httpStatus(SEE_OTHER),
          redirectUri(errorPages.routes.ServiceTimeoutController.show().url)
        )
      }
      "the internal Ids do not match" in {
        stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
        await(insertJourneyConfig(testJourneyId, testContinueUrl, "testInternalId"))

        lazy val result = get(s"/$testJourneyId/business-postcode")

        result must have(
          httpStatus(SEE_OTHER),
          redirectUri(errorPages.routes.ServiceTimeoutController.show().url)
        )
      }
      "the journey Id has no internal Id stored" in {
        stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
        await(journeyConfigRepository.collection.insertOne(
          Json.obj(
            "_id" -> testJourneyId,
            "creationTimestamp" -> Json.obj("$date" -> Instant.now.toEpochMilli)
          ) ++ Json.toJsObject(testJourneyConfig)
        ).toFuture())

        lazy val result = get(s"/$testJourneyId/business-postcode")

        result must have(
          httpStatus(SEE_OTHER),
          redirectUri(errorPages.routes.ServiceTimeoutController.show().url)
        )
      }
    }

    "return 500" when {
      "there is no auth id" in {
        await(insertJourneyConfig(testJourneyId, testContinueUrl, testInternalId))
        stubAuth(OK, successfulAuthResponse(None))
        lazy val result = get(s"/$testJourneyId/business-postcode")

        result.status mustBe INTERNAL_SERVER_ERROR
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
    "raise an internal server exception" when {
      "the journey data is missing" in {
        lazy val result = {
          stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
          post(s"/$testJourneyId/business-postcode")("business_postcode" -> "ZZ1 1ZZ")
        }

        result.status mustBe INTERNAL_SERVER_ERROR
      }

    }

    "raise an internal server exception" when {
      "the auth id is missing" in {
        lazy val result = {
          stubAuth(OK, successfulAuthResponse(None))
          post(s"/$testJourneyId/business-postcode")("business_postcode" -> "ZZ1 1ZZ")
        }

        result.status mustBe INTERNAL_SERVER_ERROR
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
        await(journeyDataRepository.collection.insertOne(
          Json.obj(
            "_id" -> testJourneyId,
            "authInternalId" -> testInternalId,
            "creationTimestamp" -> Json.obj("$date" -> Instant.now.toEpochMilli)
          ) ++ Json.toJsObject(testVatKnownFactsNoReturns)
        ).toFuture())
        lazy val result = get(s"/$testJourneyId/no-business-postcode")

        result must have(
          httpStatus(SEE_OTHER),
          redirectUri(routes.CaptureSubmittedVatReturnController.show(testJourneyId).url)
        )
        await(
          journeyDataRepository.getJourneyData(testJourneyId, testInternalId)
        ) mustBe Some(testVatKnownFactsNoReturnsNoPostcode)
      }
    }
    "raise an exception" when {
      "the journey data is missing" in {
        stubAuth(OK, successfulAuthResponse(Some(testInternalId)))

        lazy val result = get(s"/$testJourneyId/no-business-postcode")

        result.status mustBe INTERNAL_SERVER_ERROR
      }
    }

    "raise an exception" when {
      "the auth id is missing" in {
        stubAuth(OK, successfulAuthResponse(None))

        lazy val result = get(s"/$testJourneyId/no-business-postcode")

        result.status mustBe INTERNAL_SERVER_ERROR
      }
    }
  }

}
