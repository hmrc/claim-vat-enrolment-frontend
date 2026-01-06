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
import uk.gov.hmrc.claimvatenrolmentfrontend.featureswitch.core.config.KnownFactsCheckFlag
import uk.gov.hmrc.claimvatenrolmentfrontend.stubs.AuthStub
import uk.gov.hmrc.claimvatenrolmentfrontend.utils.JourneyMongoHelper
import uk.gov.hmrc.claimvatenrolmentfrontend.views.CaptureVatRegistrationDateViewTests

import java.time.Instant

class CaptureVatRegistrationDateControllerISpec extends JourneyMongoHelper with CaptureVatRegistrationDateViewTests with AuthStub {

  s"GET /$testJourneyId/vat-registration-date" should {
    lazy val result = {
      await(insertVatKnownFactsData(testJourneyId, testInternalId, testVatKnownFactsDefault))
      stubAuth(OK, successfulAuthResponse(Some(testGroupId), Some(testInternalId)))
      get(s"/$testJourneyId/vat-registration-date")
    }
    "return OK" in {
      result.status mustBe OK
    }
    testCaptureVatRegistrationDateViewTests(result)

    "When a Blocked VRN is passed in the request" when {

      lazy val result = {
        enable(KnownFactsCheckFlag)

        await(insertVatKnownFactsData(testJourneyId, testInternalId, testVatKnownFactsDefault))
        await(insertLockData(testVatNumber, testInternalId, testSubmissionNumber3))

        stubAuth(OK, successfulAuthResponse(Some(testGroupId), Some(testInternalId)))

        get(s"/$testJourneyId/vat-registration-date")
      }

      "show the access blocked page" in {
        result must have(
          httpStatus(SEE_OTHER),
          redirectUri(errorPages.routes.KnownFactsMismatchWithin24hrsController.show().url)
        )
      }
    }

    "Show an error page" when {
      "there is no Journey Config" in {
        stubAuth(OK, successfulAuthResponse(Some(testGroupId), Some(testInternalId)))

        lazy val result = get(s"/$testJourneyId/vat-registration-date")

        result must have(
          httpStatus(SEE_OTHER),
          redirectUri(errorPages.routes.ServiceTimeoutController.show().url)
        )
      }

      "the internal Ids do not match" in {
        stubAuth(OK, successfulAuthResponse(Some(testGroupId), Some(testInternalId)))
        await(insertJourneyConfig(testJourneyId, testContinueUrl, "testInternalId"))

        lazy val result = get(s"/$testJourneyId/vat-registration-date")

        result must have(
          httpStatus(SEE_OTHER),
          redirectUri(errorPages.routes.ServiceTimeoutController.show().url)
        )
      }

      "the journey Id has no internal Id stored" in {
        stubAuth(OK, successfulAuthResponse(Some(testGroupId), Some(testInternalId)))
        await(journeyConfigRepository.collection.insertOne(
          Json.obj(
            "_id" -> testJourneyId,
            "creationTimestamp" -> Json.obj("$date" -> Instant.now.toEpochMilli)
          ) ++ Json.toJsObject(testJourneyConfig)
        ).toFuture())

        lazy val result = get(s"/$testJourneyId/vat-registration-date")

        result must have(
          httpStatus(SEE_OTHER),
          redirectUri(errorPages.routes.ServiceTimeoutController.show().url)
        )
      }
    }

    "return 500" when {
      "there is no auth id" in {
        await(insertVatKnownFactsData(testJourneyId, testInternalId, testVatKnownFactsDefault))
        stubAuth(OK, successfulAuthResponse(None))
        lazy val result = get(s"/$testJourneyId/vat-registration-date")

        result.status mustBe INTERNAL_SERVER_ERROR
      }
    }
  }

  s"POST /$testJourneyId/vat-registration-date" should {
    "redirect to CaptureBusinessPostcode if the date is valid" in {
      stubAuth(OK, successfulAuthResponse(Some(testGroupId), Some(testInternalId)))

      await(journeyDataRepository.insertJourneyVatNumber(testJourneyId, testInternalId, testVatNumber))

      lazy val result = post(s"/$testJourneyId/vat-registration-date")(
        "date.day" -> "1",
        "date.month" -> "1",
        "date.year" -> "2020"
      )

      result must have(
        httpStatus(SEE_OTHER),
        redirectUri(routes.CaptureBusinessPostcodeController.show(testJourneyId).url)
      )
    }

    "when the user has submitted an empty form, the page" should {
      lazy val authStub = stubAuth(OK, successfulAuthResponse(Some(testGroupId), Some(testInternalId)))
      lazy val result = post(s"/$testJourneyId/vat-registration-date")()

      testCaptureVatRegistrationDateMissingErrorViewTests(result, authStub)

      "return a BAD_REQUEST" in {
        stubAuth(OK, successfulAuthResponse(Some(testGroupId), Some(testInternalId)))
        lazy val result = post(s"/$testJourneyId/vat-registration-date")()

        result.status mustBe BAD_REQUEST
      }
    }

    "when the user has submitted a date that is invalid, the page" should {
      lazy val authStub = stubAuth(OK, successfulAuthResponse(Some(testGroupId), Some(testInternalId)))
      lazy val result = post(s"/$testJourneyId/vat-registration-date")(
        "date.day" -> "1",
        "date.month" -> "1",
        "date.year" -> "invalidYear"
      )

      testCaptureVatRegistrationDateInvalidErrorViewTests(result, authStub)

      "return a BAD_REQUEST" in {
        stubAuth(OK, successfulAuthResponse(Some(testGroupId), Some(testInternalId)))
        lazy val result = post(s"/$testJourneyId/vat-registration-date")(
          "date.day" -> "1",
          "date.month" -> "1",
          "date.year" -> "invalidYear"
        )

        result.status mustBe BAD_REQUEST
      }
    }

    "when the user has submitted a date with an invalid year, the page" should {
      lazy val authStub = stubAuth(OK, successfulAuthResponse(Some(testGroupId), Some(testInternalId)))
      lazy val result = post(s"/$testJourneyId/vat-registration-date")(
        "date.day" -> "1",
        "date.month" -> "1",
        "date.year" -> "94"
      )

      testCaptureVatRegistrationDateInvalidErrorViewTests(result, authStub)

      "return a BAD_REQUEST" in {
        stubAuth(OK, successfulAuthResponse(Some(testGroupId), Some(testInternalId)))
        lazy val result = post(s"/$testJourneyId/vat-registration-date")(
          "date.day" -> "1",
          "date.month" -> "1",
          "date.year" -> "94"
        )

        result.status mustBe BAD_REQUEST
      }
    }

    "when the user enters a date that is in the future, the page" should {
      lazy val authStub = stubAuth(OK, successfulAuthResponse(Some(testGroupId), Some(testInternalId)))
      lazy val result = post(s"/$testJourneyId/vat-registration-date")(
        "date.day" -> "1",
        "date.month" -> "1",
        "date.year" -> "2100"
      )

      testCaptureVatRegistrationDateFutureErrorViewTests(result, authStub)

      "return a BAD_REQUEST" in {
        stubAuth(OK, successfulAuthResponse(Some(testGroupId), Some(testInternalId)))
        lazy val result = post(s"/$testJourneyId/vat-registration-date")(
          "date.day" -> "1",
          "date.month" -> "1",
          "date.year" -> "2100"
        )

        result.status mustBe BAD_REQUEST
      }
    }
    "raise an internal server exception" when {
      "the journey data is missing" in {
        stubAuth(OK, successfulAuthResponse(Some(testGroupId), Some(testInternalId)))

        lazy val result = post(s"/$testJourneyId/vat-registration-date")(
          "date.day" -> "1",
          "date.month" -> "1",
          "date.year" -> "2020"
        )

        result.status mustBe INTERNAL_SERVER_ERROR
      }
    }
    "raise an internal server exception" when {
      "the auh id is missing" in {
        stubAuth(OK, successfulAuthResponse(None))

        lazy val result = post(s"/$testJourneyId/vat-registration-date")(
          "date.day" -> "1",
          "date.month" -> "1",
          "date.year" -> "2020"
        )

        result.status mustBe INTERNAL_SERVER_ERROR
      }
    }
  }
}