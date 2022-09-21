/*
 * Copyright 2022 HM Revenue & Customs
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

import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers.{INTERNAL_SERVER_ERROR, OK, SEE_OTHER}
import uk.gov.hmrc.claimvatenrolmentfrontend.assets.TestConstants.{testContinueUrl, testInternalId, testJourneyId, testVatNumber}
import uk.gov.hmrc.claimvatenrolmentfrontend.controllers.errorPages.{routes => errorRoutes}
import uk.gov.hmrc.claimvatenrolmentfrontend.services.JourneyIdGenerationService
import uk.gov.hmrc.claimvatenrolmentfrontend.stubs.{AuthStub, FakeJourneyIdGenerationService}
import uk.gov.hmrc.claimvatenrolmentfrontend.utils.ComponentSpecHelper

class JourneyControllerISpec extends ComponentSpecHelper with AuthStub {

  override lazy val app: Application = new GuiceApplicationBuilder()
    .overrides(bind[JourneyIdGenerationService].toInstance(new FakeJourneyIdGenerationService(testJourneyId)))
    .configure(config)
    .build()

  s"GET  /journey/$testVatNumber" should {
    "redirect to the Capture VAT Registration Date page" in {
      stubAuth(OK, successfulAuthResponse(None, Some(testInternalId)))

      lazy val result = get(s"/journey/$testVatNumber?continueUrl=$testContinueUrl")

      result.status mustBe SEE_OTHER

      result.header("Location").getOrElse("None") mustBe routes.CaptureVatRegistrationDateController.show(testJourneyId).url
    }
    "redirect to Invalid Account Error page" in {
      stubAuth(OK, successfulAuthResponse(None, Some(testInternalId), Some("Assistant")))

      lazy val result = get(s"/journey/$testVatNumber?continueUrl=$testContinueUrl")

      result.status mustBe SEE_OTHER

      result.header("Location").getOrElse("None") mustBe errorRoutes.InvalidAccountTypeController.show().url
    }
    "return Internal Server Error" in {
      stubAuth(OK, successfulAuthResponse(None, None, None))

      lazy val result = get(s"/journey/$testVatNumber?continueUrl=$testContinueUrl")

      result.status mustBe INTERNAL_SERVER_ERROR

    }
  }

}
