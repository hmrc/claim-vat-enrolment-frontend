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
import uk.gov.hmrc.claimvatenrolmentfrontend.views.CaptureLastMonthSubmittedViewTests

import java.time.Month

class CaptureLastMonthSubmittedControllerISpec extends ComponentSpecHelper with CaptureLastMonthSubmittedViewTests with AuthStub {

  s"GET /$testJourneyId/last-vat-return-date" should {
    lazy val result = {
      stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
      get(s"/$testJourneyId/last-vat-return-date")
    }
    "return OK" in {
      result.status mustBe OK
    }

    testCaptureLastMonthSubmittedViewTests(result)

  }

  "POST /last-vat-return-date" should {
    "redirect to Check Your Answers page if a month is selected" in {
      stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
      await(journeyDataRepository.insertJourneyVatNumber(testJourneyId, testInternalId, testVatNumber))

      lazy val result = post(s"/$testJourneyId/last-vat-return-date")("return_date" -> Month.JANUARY.getValue.toString)
      result must have(
        httpStatus(SEE_OTHER),
        redirectUri(routes.CheckYourAnswersController.show(testJourneyId).url)
      )
    }
    "return BAD_REQUEST if no month is selected" in {
      stubAuth(OK, successfulAuthResponse(Some(testInternalId)))

      lazy val result = post(s"/$testJourneyId/last-vat-return-date")()

      result.status mustBe BAD_REQUEST
    }
    "return the correct view with error messages" should {
      lazy val authStub = stubAuth(OK, successfulAuthResponse(Some(testInternalId)))

      lazy val result = post(s"/$testJourneyId/last-vat-return-date")()

      testCaptureLastMonthSubmittedErrorViewTests(result, authStub)
    }
  }


}
