/*
 * Copyright 2020 HM Revenue & Customs
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

import play.api.libs.ws.WSResponse
import play.api.test.Helpers._
import uk.gov.hmrc.claimvatenrolmentfrontend.assets.TestConstants.{testContinueUrl, testJourneyId}
import uk.gov.hmrc.claimvatenrolmentfrontend.utils.ComponentSpecHelper
import uk.gov.hmrc.claimvatenrolmentfrontend.views.CheckYourAnswersViewTests


class CheckYourAnswersControllerISpec extends ComponentSpecHelper with CheckYourAnswersViewTests {

  s"GET /$testJourneyId/check-your-answers-vat" should {
    "return OK" in {
      lazy val result: WSResponse = get(s"/$testJourneyId/check-your-answers-vat")

      result.status mustBe OK
    }

    "return a view" should {
      lazy val result = get(s"/$testJourneyId/check-your-answers-vat")

      testCheckYourAnswersView(result)
    }
  }

  s"POST /$testJourneyId/check-your-answers-vat" should {
    "redirect to Known Facts Mismatch " in {
      lazy val result = post(s"/$testJourneyId/check-your-answers-vat")()

      result must have(
        httpStatus(SEE_OTHER),
        redirectUri(routes.KnownFactsMismatchController.show(testJourneyId).url)
      )
    }
  }

}
