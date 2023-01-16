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

package uk.gov.hmrc.claimvatenrolmentfrontend.controllers.errorPages

import play.api.test.Helpers._
import uk.gov.hmrc.claimvatenrolmentfrontend.assets.TestConstants.testInternalId
import uk.gov.hmrc.claimvatenrolmentfrontend.stubs.AuthStub
import uk.gov.hmrc.claimvatenrolmentfrontend.utils.ComponentSpecHelper
import uk.gov.hmrc.claimvatenrolmentfrontend.views.errorPages.KnownFactsMismatchViewTests

class KnownFactsMismatchControllerISpec extends ComponentSpecHelper with KnownFactsMismatchViewTests with AuthStub {

  "GET /error/can-not-confirm-business" should {
    lazy val result = {
      stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
      get("/error/can-not-confirm-business")
    }

    "return OK" in {
      result.status mustBe OK
    }

    testKnownFactsMismatchView(result)
  }

}
