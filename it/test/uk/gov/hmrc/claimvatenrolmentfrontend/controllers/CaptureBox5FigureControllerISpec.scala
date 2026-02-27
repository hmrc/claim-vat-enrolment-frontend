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

package uk.gov.hmrc.claimvatenrolmentfrontend.controllers

import play.api.libs.json.Json
import play.api.test.Helpers._
import uk.gov.hmrc.claimvatenrolmentfrontend.assets.TestConstants._
import uk.gov.hmrc.claimvatenrolmentfrontend.featureswitch.core.config.KnownFactsCheckFlag
import uk.gov.hmrc.claimvatenrolmentfrontend.stubs.AuthStub
import uk.gov.hmrc.claimvatenrolmentfrontend.utils.JourneyMongoHelper
import uk.gov.hmrc.claimvatenrolmentfrontend.views.CaptureBox5FigureViewTests

import java.time.Instant

class CaptureBox5FigureControllerISpec extends JourneyMongoHelper with CaptureBox5FigureViewTests with AuthStub {

  s"GET /$testJourneyId/box-5-figure" should {
    lazy val result = {
      await(insertVatKnownFactsData(testJourneyId, testInternalId, baseVatKnownFacts))
      stubAuth(OK, successfulAuthResponse(Some(testGroupId), Some(testInternalId)))
      get(s"/$testJourneyId/box-5-figure")
    }
    "return OK" in {
      result.status mustBe OK
    }
    testCaptureBox5FigureViewTests(result)

    "return a access blocked page" when {

      lazy val result = {
        enable(KnownFactsCheckFlag)

        await(insertVatKnownFactsData(testJourneyId, testInternalId, baseVatKnownFacts))
        await(insertLockData(testVatNumber, testInternalId, testSubmissionNumber3))

        stubAuth(OK, successfulAuthResponse(Some(testGroupId), Some(testInternalId)))

        get(s"/$testJourneyId/box-5-figure")
      }

      "a Blocked VRN is accessed" in {
        result must have(
          httpStatus(SEE_OTHER),
          redirectUri(errorPages.routes.KnownFactsMismatchWithin24hrsController.show().url)
        )
      }
    }

    "Show an error page" when {
      "there is no Journey Config" in {
        stubAuth(OK, successfulAuthResponse(Some(testGroupId), Some(testInternalId)))

        lazy val result = get(s"/$testJourneyId/box-5-figure")

        result must have(
          httpStatus(SEE_OTHER),
          redirectUri(errorPages.routes.ServiceTimeoutController.show().url)
        )
      }

      "the internal Ids do not match" in {
        stubAuth(OK, successfulAuthResponse(Some(testGroupId), Some(testInternalId)))
        await(insertJourneyConfig(testJourneyId, testContinueUrl, "testInternalId"))

        lazy val result = get(s"/$testJourneyId/box-5-figure")

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

        lazy val result = get(s"/$testJourneyId/box-5-figure")

        result must have(
          httpStatus(SEE_OTHER),
          redirectUri(errorPages.routes.ServiceTimeoutController.show().url)
        )
      }
    }

    "return 500" when {
      "there is no auth id" in {
        await(insertVatKnownFactsData(testJourneyId, testInternalId, baseVatKnownFacts))
        stubAuth(OK, successfulAuthResponse(None))
        lazy val result = get(s"/$testJourneyId/box-5-figure")

        result.status mustBe INTERNAL_SERVER_ERROR
      }
    }
  }

  s"POST /$testJourneyId/box-5-figure" should {
    "redirect to CaptureLastMonthSubmitted if the box 5 figure is valid" in {
      stubAuth(OK, successfulAuthResponse(Some(testGroupId), Some(testInternalId)))

      await(journeyDataRepository.insertJourneyVatNumber(testJourneyId, testInternalId, testVatNumber))

      lazy val result = post(s"/$testJourneyId/box-5-figure")(
        "box5_figure" -> "1234.56"
      )

      result must have(
        httpStatus(SEE_OTHER),
        redirectUri(routes.CaptureLastMonthSubmittedController.show(testJourneyId).url)
      )
    }

    "redirect to CaptureLastMonthSubmitted if the box 5 figure is a negative value " in {
      stubAuth(OK, successfulAuthResponse(Some(testGroupId), Some(testInternalId)))

      await(journeyDataRepository.insertJourneyVatNumber(testJourneyId, testInternalId, testVatNumber))

      lazy val result = post(s"/$testJourneyId/box-5-figure")(
        "box5_figure" -> "-100.00"
      )

      result must have(
        httpStatus(SEE_OTHER),
        redirectUri(routes.CaptureLastMonthSubmittedController.show(testJourneyId).url)
      )
    }

    "when the user does not submit a box 5 figure" should {
      lazy val authStub = stubAuth(OK, successfulAuthResponse(Some(testGroupId), Some(testInternalId)))
      lazy val result = post(s"/$testJourneyId/box-5-figure")(
        "box5_figure" -> ""
      )

      testCaptureBox5MissingErrorViewTests(result, authStub)

      "return a BAD_REQUEST" in {
        stubAuth(OK, successfulAuthResponse(Some(testGroupId), Some(testInternalId)))

        lazy val result = post(s"/$testJourneyId/box-5-figure")(
          "box5_figure" -> ""
        )

        result.status mustBe BAD_REQUEST
      }
    }

    "when the user submits an invalid box 5 figure" should {
      lazy val authStub = stubAuth(OK, successfulAuthResponse(Some(testGroupId), Some(testInternalId)))

      lazy val result = post(s"/$testJourneyId/box-5-figure")(
        "box5_figure" -> "1234.5"
      )

      testCaptureBox5InvalidFormatErrorViewTests(result, authStub)

      "return a BAD_REQUEST" in {
        stubAuth(OK, successfulAuthResponse(Some(testGroupId), Some(testInternalId)))

        lazy val result = post(s"/$testJourneyId/box-5-figure")(
          "box5_figure" -> "1234.5"
        )

        result.status mustBe BAD_REQUEST
      }
    }

    "when the user submits a box 5 figure with invalid characters" should {
      lazy val authStub = stubAuth(OK, successfulAuthResponse(Some(testGroupId), Some(testInternalId)))

      lazy val result = post(s"/$testJourneyId/box-5-figure")(
        "box5_figure" -> "1234.oo"
      )

      testCaptureBox5InvalidFormatErrorViewTests(result, authStub)

      "return a BAD_REQUEST" in {
        stubAuth(OK, successfulAuthResponse(Some(testGroupId), Some(testInternalId)))

        lazy val result = post(s"/$testJourneyId/box-5-figure")(
          "box5_figure" -> "1234.oo"
        )

        result.status mustBe BAD_REQUEST
      }
    }

    "when the user submits a box 5 figure that is more than 14 digits" should {
      lazy val authStub = stubAuth(OK, successfulAuthResponse(Some(testGroupId), Some(testInternalId)))


      lazy val result = post(s"/$testJourneyId/box-5-figure")(
        "box5_figure" -> "0123456789012345"
      )

      testCaptureBox5InvalidLengthErrorViewTests(result, authStub)

      "return a BAD_REQUEST" in {
        stubAuth(OK, successfulAuthResponse(Some(testGroupId), Some(testInternalId)))

        lazy val result = post(s"/$testJourneyId/box-5-figure")(
          "box5_figure" -> "0123456789012345"
        )

        result.status mustBe BAD_REQUEST
      }
    }

    "when the user submits a box 5 figure that is more than 14 digits and negative" should {
      lazy val authStub = stubAuth(OK, successfulAuthResponse(Some(testGroupId), Some(testInternalId)))


      lazy val result = post(s"/$testJourneyId/box-5-figure")(
        "box5_figure" -> "-100000000000000000000.00"
      )

      testCaptureBox5InvalidLengthErrorViewTests(result, authStub)

      "return a BAD_REQUEST" in {
        stubAuth(OK, successfulAuthResponse(Some(testGroupId), Some(testInternalId)))

        lazy val result = post(s"/$testJourneyId/box-5-figure")(
          "box5_figure" -> "-100000000000000000000.00"
        )

        result.status mustBe BAD_REQUEST
      }
    }

    "return an internal server error" when {
      "the journey data is missing" in {
        stubAuth(OK, successfulAuthResponse(Some(testGroupId), Some(testInternalId)))

        lazy val result = post(s"/$testJourneyId/box-5-figure")(
          "box5_figure" -> "1234.56"
        )

        result.status mustBe INTERNAL_SERVER_ERROR
      }
    }

    "return an internal server error" when {
      "there is no auth id" in {
        stubAuth(OK, successfulAuthResponse(None))

        lazy val result = post(s"/$testJourneyId/box-5-figure")(
          "box5_figure" -> "1234.56"
        )

        result.status mustBe INTERNAL_SERVER_ERROR
      }
    }

    s"POST /$testJourneyId/box-5-figure" should {
      "redirect to CaptureLastMonthSubmitted if the user submits a valid box 5 figure with commas" in {
        stubAuth(OK, successfulAuthResponse(Some(testGroupId), Some(testInternalId)))

        await(journeyDataRepository.insertJourneyVatNumber(testJourneyId, testInternalId, testVatNumber))

        lazy val result = post(s"/$testJourneyId/box-5-figure")(
          "box5_figure" -> "12,123,123.45"
        )

        result must have(
          httpStatus(SEE_OTHER),
          redirectUri(routes.CaptureLastMonthSubmittedController.show(testJourneyId).url)
        )
      }
    }
  }
}

