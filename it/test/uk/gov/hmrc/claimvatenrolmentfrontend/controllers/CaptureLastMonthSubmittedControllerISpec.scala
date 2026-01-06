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
import uk.gov.hmrc.claimvatenrolmentfrontend.views.CaptureLastMonthSubmittedViewTests

import java.time.{Instant, Month}

class CaptureLastMonthSubmittedControllerISpec extends JourneyMongoHelper with CaptureLastMonthSubmittedViewTests with AuthStub {

  s"GET /$testJourneyId/last-vat-return-date" should {
    lazy val result = {
      await(insertVatKnownFactsData(testJourneyId, testInternalId, testVatKnownFactsDefault))
      stubAuth(OK, successfulAuthResponse(Some(testGroupId), Some(testInternalId)))
      get(s"/$testJourneyId/last-vat-return-date")
    }
    "return OK" in {
      result.status mustBe OK
    }

    testCaptureLastMonthSubmittedViewTests(result)

    "return a access blocked page" when {
      lazy val result = {
        enable(KnownFactsCheckFlag)

        await(insertVatKnownFactsData(testJourneyId, testInternalId, testVatKnownFactsDefault))
        await(insertLockData(testVatNumber, testInternalId, testSubmissionNumber3))

        stubAuth(OK, successfulAuthResponse(Some(testGroupId), Some(testInternalId)))

        get(s"/$testJourneyId/last-vat-return-date")
      }

      "a Blocked VRN is accessed" in {
        result must have(
          httpStatus(SEE_OTHER),
          redirectUri(errorPages.routes.KnownFactsMismatchWithin24hrsController.show().url)
        )
      }
    }

    "Show an error page" when {
      "There is no Journey Config" in {
        stubAuth(OK, successfulAuthResponse(Some(testGroupId), Some(testInternalId)))

        lazy val result = get(s"/$testJourneyId/last-vat-return-date")

        result must have(
          httpStatus(SEE_OTHER),
          redirectUri(errorPages.routes.ServiceTimeoutController.show().url)
        )
      }

      "the internal Ids do not match" in {
        stubAuth(OK, successfulAuthResponse(Some(testGroupId), Some(testInternalId)))
        await(insertJourneyConfig(testJourneyId, testContinueUrl, "testInternalId"))

        lazy val result = get(s"/$testJourneyId/last-vat-return-date")

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
        lazy val result = get(s"/$testJourneyId/last-vat-return-date")

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
        lazy val result = get(s"/$testJourneyId/last-vat-return-date")

        result.status mustBe INTERNAL_SERVER_ERROR
      }
    }
  }

  "POST /last-vat-return-date" should {
    "redirect to Check Your Answers page if a month is selected" in {
      stubAuth(OK, successfulAuthResponse(Some(testGroupId), Some(testInternalId)))
      await(journeyDataRepository.insertJourneyVatNumber(testJourneyId, testInternalId, testVatNumber))

      lazy val result = post(s"/$testJourneyId/last-vat-return-date")("return_date" -> Month.JANUARY.getValue.toString)
      result must have(
        httpStatus(SEE_OTHER),
        redirectUri(routes.CheckYourAnswersController.show(testJourneyId).url)
      )
    }
    "return BAD_REQUEST if no month is selected" in {
      stubAuth(OK, successfulAuthResponse(Some(testGroupId), Some(testInternalId)))

      lazy val result = post(s"/$testJourneyId/last-vat-return-date")()

      result.status mustBe BAD_REQUEST
    }
    "return the correct view with error messages" should {
      lazy val authStub = stubAuth(OK, successfulAuthResponse(Some(testGroupId), Some(testInternalId)))

      lazy val result = post(s"/$testJourneyId/last-vat-return-date")()

      testCaptureLastMonthSubmittedErrorViewTests(result, authStub)
    }
    "raise an internal server exception" when {
      "the journey data is missing" in {
        stubAuth(OK, successfulAuthResponse(Some(testGroupId), Some(testInternalId)))

        lazy val result = post(s"/$testJourneyId/last-vat-return-date")("return_date" -> Month.JANUARY.getValue.toString)

        result.status mustBe INTERNAL_SERVER_ERROR
      }
    }
    "raise an internal server exception" when {
      "the auth id is missing" in {
        stubAuth(OK, successfulAuthResponse(None))

        lazy val result = post(s"/$testJourneyId/last-vat-return-date")("return_date" -> Month.JANUARY.getValue.toString)

        result.status mustBe INTERNAL_SERVER_ERROR
      }
    }
  }


}
