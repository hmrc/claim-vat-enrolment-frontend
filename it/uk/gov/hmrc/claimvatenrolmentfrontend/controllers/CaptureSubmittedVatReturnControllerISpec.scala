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
import uk.gov.hmrc.claimvatenrolmentfrontend.assets.TestConstants.testJourneyId
import uk.gov.hmrc.claimvatenrolmentfrontend.utils.ComponentSpecHelper
import uk.gov.hmrc.claimvatenrolmentfrontend.views.CaptureSubmittedVatReturnViewTests

class CaptureSubmittedVatReturnControllerISpec extends ComponentSpecHelper with CaptureSubmittedVatReturnViewTests {

  s"GET /$testJourneyId/submitted-vat-return" should {
    "return OK" in {
      lazy val result = get(s"/$testJourneyId/submitted-vat-return")

      result.status mustBe OK
    }
    "return a view" should {
      lazy val result = get(s"/$testJourneyId/submitted-vat-return")

      testCaptureSubmittedVatReturnViewTests(result)
    }
  }

  "POST /submitted-vat-return" should {
    "redirect to CaptureBox5Figure" in {
      lazy val result = post(s"/$testJourneyId/submitted-vat-return")("vat_return" -> "yes")

      result must have(
        httpStatus(SEE_OTHER),
        redirectUri(routes.CaptureBox5FigureController.show(testJourneyId).url)
      )
    }

    "return a BAD_REQUEST if the user has not given an answer and the correct errors" should {
      lazy val result = post(s"/$testJourneyId/submitted-vat-return")(
        "vat_return" -> ""
      )

      result.status mustBe BAD_REQUEST
      testCaptureSubmittedVatReturnErrorViewTests(result)
    }
  }

}
