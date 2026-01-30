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

package uk.gov.hmrc.claimvatenrolmentfrontend.controllers

import play.api.libs.json.Json
import play.api.test.Helpers._
import uk.gov.hmrc.claimvatenrolmentfrontend.assets.TestConstants._
import uk.gov.hmrc.claimvatenrolmentfrontend.repositories.JourneyDataRepository._
import uk.gov.hmrc.claimvatenrolmentfrontend.stubs.AuthStub
import uk.gov.hmrc.claimvatenrolmentfrontend.utils.JourneyMongoHelper
import uk.gov.hmrc.claimvatenrolmentfrontend.views.CaptureSubmittedVatReturnViewTests

import java.time.Instant

class CaptureSubmittedVatReturnVer2ControllerISpec extends JourneyMongoHelper with CaptureSubmittedVatReturnViewTests with AuthStub {

  override def additionalConfig: Map[String, String] = Map(
    "feature-switch.knownFactsCheckFlag" -> "true",
    "feature-switch.knownFactsCheckWithVanFlag" -> "true"
  )

  s"GET /$testJourneyId/submitted-vat-return" should {
    lazy val result = {
      await(insertVatKnownFactsData(testJourneyId, testInternalId, testVatKnownFactsDefault))
      stubAuth(OK, successfulAuthResponse(Some(testGroupId), Some(testInternalId)))
      get(s"/$testJourneyId/submitted-vat-return")
    }

    "return OK" in {
      result.status mustBe OK
    }

    testCaptureSubmittedVatReturnViewTests(result)

    "Show an error page" when {
      "There is no Journey Config" in {
        stubAuth(OK, successfulAuthResponse(Some(testGroupId), Some(testInternalId)))

        lazy val result = get(s"/$testJourneyId/submitted-vat-return")

        result must have(
          httpStatus(SEE_OTHER),
          redirectUri(errorPages.routes.ServiceTimeoutController.show().url)
        )
      }
      "the internal Ids do not match" in {
        stubAuth(OK, successfulAuthResponse(Some(testGroupId), Some(testInternalId)))
        await(insertJourneyConfig(testJourneyId, testContinueUrl, "testInternalId"))

        lazy val result = get(s"/$testJourneyId/submitted-vat-return")

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

        lazy val result = get(s"/$testJourneyId/submitted-vat-return")

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
        lazy val result = get(s"/$testJourneyId/submitted-vat-return")

        result.status mustBe INTERNAL_SERVER_ERROR
      }
    }
  }


  s"POST /$testJourneyId/submitted-vat-return" should {
    "redirect to CaptureBox5Figure" when {
      "the user selects yes" in {
        stubAuth(OK, successfulAuthResponse(Some(testGroupId), Some(testInternalId)))
        await(journeyDataRepository.insertJourneyVatNumber(testJourneyId, testInternalId, testVatNumber))
        lazy val result = post(s"/$testJourneyId/submitted-vat-return")("vat_return" -> "yes")

        result must have(
          httpStatus(SEE_OTHER),
          redirectUri(routes.CaptureBox5FigureController.show(testJourneyId).url)
        )
      }
    }

    "redirect to Capture Vat Application Number page" when {
      "the user changes their answer to no" in {
        stubAuth(OK, successfulAuthResponse(Some(testGroupId), Some(testInternalId)))
        await(journeyDataRepository.collection.insertOne(
          Json.obj(
            "_id" -> testJourneyId,
            "authInternalId" -> testInternalId,
            "creationTimestamp" -> Json.obj("$date" -> Instant.now.toEpochMilli)
          ) ++ Json.toJsObject(testFullVatKnownFactsWithReturnsInformation)
        ).toFuture())
        lazy val result = post(s"/$testJourneyId/submitted-vat-return")("vat_return" -> "no")

        result must have(
          httpStatus(SEE_OTHER),
          redirectUri(routes.CaptureVatApplicationNumberController.show(testJourneyId).url)
        )
        await(
          journeyDataRepository.getJourneyData(testJourneyId, testInternalId)
        ) mustBe Some(testVatKnownFactsNoReturns)
      }

      "the user selects no" in {
        stubAuth(OK, successfulAuthResponse(Some(testGroupId), Some(testInternalId)))
        await(journeyDataRepository.collection.insertOne(
          Json.obj(
            "_id" -> testJourneyId,
            "authInternalId" -> testInternalId,
            "creationTimestamp" -> Json.obj("$date" -> Instant.now.toEpochMilli)
          ) ++ Json.obj("vatRegPostcode" -> testPostcode.stringValue, "vatRegistrationDate" -> testVatRegDate,
            "vatNumber" -> testVatNumber)
            ++ Json.obj("formBundleReference" -> Some(testFormBundleReference))
        ).toFuture())
        lazy val result = post(s"/$testJourneyId/submitted-vat-return")("vat_return" -> "no")

        result must have(
          httpStatus(SEE_OTHER),
          redirectUri(routes.CaptureVatApplicationNumberController.show(testJourneyId).url)
        )
        await(
          journeyDataRepository.getJourneyData(testJourneyId, testInternalId)
        ) mustBe Some(testVatKnownFactsNoReturns)
      }

      "the user selects no and vat application number is empty" in {
        stubAuth(OK, successfulAuthResponse(Some(testGroupId), Some(testInternalId)))
        await(journeyDataRepository.collection.insertOne(
          Json.obj(
            "_id" -> testJourneyId,
            "authInternalId" -> testInternalId,
            "creationTimestamp" -> Json.obj("$date" -> Instant.now.toEpochMilli)
          ) ++ Json.obj("vatRegPostcode" -> testPostcode.stringValue, "vatRegistrationDate" -> testVatRegDate,
            "vatNumber" -> testVatNumber)
            ++ Json.obj("formBundleReference" -> None)
        ).toFuture())
        lazy val result = post(s"/$testJourneyId/submitted-vat-return")("vat_return" -> "no")

        result must have(
          httpStatus(SEE_OTHER),
          redirectUri(routes.CaptureVatApplicationNumberController.show(testJourneyId).url)
        )
        await(
          journeyDataRepository.getJourneyData(testJourneyId, testInternalId)
        ) mustBe Some(testVatKnownFactsNoFormBundleReference)
      }
    }
    "raise an internal server error" when {
      "the journey data is missing" in {
        stubAuth(OK, successfulAuthResponse(Some(testGroupId), Some(testInternalId)))

        lazy val result = post(s"/$testJourneyId/submitted-vat-return")("vat_return" -> "yes")

        result.status mustBe INTERNAL_SERVER_ERROR
      }
    }
    "raise an internal server error" when {
      "the auth id is missing" in {
        stubAuth(OK, successfulAuthResponse(None))

        lazy val result = post(s"/$testJourneyId/submitted-vat-return")("vat_return" -> "yes")

        result.status mustBe INTERNAL_SERVER_ERROR
      }
    }
  }

  "return a view with errors" when {
    "the user submits an empty form" should {
      lazy val result = {
        stubAuth(OK, successfulAuthResponse(Some(testGroupId), Some(testInternalId)))
        post(s"/$testJourneyId/submitted-vat-return")()
      }

      "return a BAD_REQUEST" in {
        result.status mustBe BAD_REQUEST
      }

      testCaptureSubmittedVatReturnErrorViewTests(result)
    }
  }

}


