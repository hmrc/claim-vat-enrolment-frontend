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
import uk.gov.hmrc.claimvatenrolmentfrontend.stubs.AuthStub
import uk.gov.hmrc.claimvatenrolmentfrontend.utils.JourneyMongoHelper
import uk.gov.hmrc.claimvatenrolmentfrontend.views.CaptureVatApplicationNumberViewTests

import java.time.Instant

class CaptureVatApplicationNumberControllerISpec extends JourneyMongoHelper with CaptureVatApplicationNumberViewTests with AuthStub {

  s"GET VAN /$testJourneyId/vat-application-number" should {
    lazy val result = {
      await(insertJourneyConfig(testJourneyId, testContinueUrl, testInternalId))
      stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
      get(s"/$testJourneyId/vat-application-number")
    }
    "return OK" in {
      result.status mustBe OK
    }

    testCaptureVatApplicationNumberViewTests(result)

    "Show an error page VAN " when {
      "there is no Journey Config" in {
        stubAuth(OK, successfulAuthResponse(Some(testInternalId)))

        lazy val result = get(s"/$testJourneyId/vat-application-number")

        result must have(
          httpStatus(SEE_OTHER),
          redirectUri(errorPages.routes.ServiceTimeoutController.show().url)
        )
      }

      "the internal Ids do not match" in {
        stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
        await(insertJourneyConfig(testJourneyId, testContinueUrl, "testInternalId"))

        lazy val result = get(s"/$testJourneyId/vat-application-number")

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

        lazy val result = get(s"/$testJourneyId/vat-application-number")

        result must have(
          httpStatus(SEE_OTHER),
          redirectUri(errorPages.routes.ServiceTimeoutController.show().url)
        )
      }
    }

    "return 500 for VAN" when {
      "there is no auth id" in {
        await(insertJourneyConfig(testJourneyId, testContinueUrl, testInternalId))
        stubAuth(OK, successfulAuthResponse(None))
        lazy val result = get(s"/$testJourneyId/vat-application-number")

        result.status mustBe INTERNAL_SERVER_ERROR
      }
    }
  }

  s"POST /$testJourneyId/vat-application-number" should {

    "when the user does not submit a VAN number" should {
      lazy val authStub = stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
      lazy val result = post(s"/$testJourneyId/vat-application-number")(
        "vatApplicationNumber" -> ""
      )

      testCaptureVatApplicationNumberMissingErrorViewTests(result, authStub)

      "return a BAD_REQUEST" in {
        stubAuth(OK, successfulAuthResponse(Some(testInternalId)))

        lazy val result = post(s"/$testJourneyId/vat-application-number")(
          "vatApplicationNumber" -> ""
        )

        result.status mustBe BAD_REQUEST
      }
    }

    "when the user submits an 11 digit number for VAN page" should {
      lazy val authStub = stubAuth(OK, successfulAuthResponse(Some(testInternalId)))

      lazy val result = post(s"/$testJourneyId/vat-application-number")(
        "vatApplicationNumber" -> "12345678910"
      )

      testCaptureVatApplicationNumberInvalidLengthErrorViewTests(result, authStub)

      "return a BAD_REQUEST" in {
        stubAuth(OK, successfulAuthResponse(Some(testInternalId)))

        lazy val result = post(s"/$testJourneyId/vat-application-number")(
          "vatApplicationNumber" -> "12345678910"
        )

        result.status mustBe BAD_REQUEST
      }
    }

    "when the user submits a VAN with invalid characters" should {
      lazy val authStub = stubAuth(OK, successfulAuthResponse(Some(testInternalId)))

      lazy val result = post(s"/$testJourneyId/vat-application-number")(
        "vatApplicationNumber" -> "1234oo789101"
      )

      testCaptureVatApplicationNumberInvalidFormatErrorViewTests(result, authStub)

      "return a BAD_REQUEST" in {
        stubAuth(OK, successfulAuthResponse(Some(testInternalId)))

        lazy val result = post(s"/$testJourneyId/vat-application-number")(
          "vatApplicationNumber" -> "1234.oo"
        )

        result.status mustBe BAD_REQUEST
      }
    }

    "when the user submits a VAN with 14 digits" should {
      lazy val authStub = stubAuth(OK, successfulAuthResponse(Some(testInternalId)))


      lazy val result = post(s"/$testJourneyId/vat-application-number")(
        "vatApplicationNumber" -> "0123456789012345"
      )

      testCaptureVatApplicationNumberInvalidLengthErrorViewTests(result, authStub)

      "return a BAD_REQUEST" in {
        stubAuth(OK, successfulAuthResponse(Some(testInternalId)))

        lazy val result = post(s"/$testJourneyId/vat-application-number")(
          "vatApplicationNumber" -> "0123456789012345"
        )

        result.status mustBe BAD_REQUEST
      }
    }

    "return an internal server error" when {
      "there is no auth id" in {
        stubAuth(OK, successfulAuthResponse(None))

        lazy val result = post(s"/$testJourneyId/vat-application-number")(
          "vatApplicationNumber" -> "1234.56"
        )

        result.status mustBe INTERNAL_SERVER_ERROR
      }
    }

  }
}

