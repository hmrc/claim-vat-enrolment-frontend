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

import play.api.test.Helpers._
import uk.gov.hmrc.claimvatenrolmentfrontend.assets.TestConstants.{testInternalId, testJourneyId, testVatNumber}
import uk.gov.hmrc.claimvatenrolmentfrontend.stubs.AuthStub
import uk.gov.hmrc.claimvatenrolmentfrontend.utils.ComponentSpecHelper
import uk.gov.hmrc.claimvatenrolmentfrontend.views.CaptureBusinessPostcodeViewTests

class CaptureBusinessPostcodeControllerISpec extends ComponentSpecHelper with CaptureBusinessPostcodeViewTests with AuthStub {

  s"GET /$testJourneyId/business-postcode" should {
    lazy val result = {
      stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
      get(s"/$testJourneyId/business-postcode")
    }

    "return OK" in {
      result.status mustBe OK
    }

    testCaptureBusinessPostcodeViewTests(result)
  }

  s"POST /$testJourneyId/business-postcode" should {
    "redirect to CaptureSubmittedVatReturn" when {
      "the postcode contains a space" in {
        stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
        lazy val result = post(s"/$testJourneyId/business-postcode")("business_postcode" -> "ZZ1 1ZZ")
        await(journeyDataRepository.insertJourneyData(testJourneyId, testInternalId, testVatNumber))

        result must have(
          httpStatus(SEE_OTHER),
          redirectUri(routes.CaptureSubmittedVatReturnController.show(testJourneyId).url)
        )
      }

      "the postcode does not contain a space" in {
        stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
        lazy val result = post(s"/$testJourneyId/business-postcode")("business_postcode" -> "ZZ11ZZ")
        await(journeyDataRepository.insertJourneyData(testJourneyId, testInternalId, testVatNumber))

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

}
