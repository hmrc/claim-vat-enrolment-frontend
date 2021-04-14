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

package uk.gov.hmrc.claimvatenrolmentfrontend.controllers.errorPages

import play.api.test.Helpers._
import uk.gov.hmrc.claimvatenrolmentfrontend.stubs.AuthStub
import uk.gov.hmrc.claimvatenrolmentfrontend.utils.ComponentSpecHelper
import uk.gov.hmrc.claimvatenrolmentfrontend.views.errorPages.EnrolmentAlreadyAllocatedErrorViewTest

class EnrolmentAlreadyAllocatedErrorControllerISpec extends ComponentSpecHelper with EnrolmentAlreadyAllocatedErrorViewTest with AuthStub {

  s"GET /error/already-enrolled" should {
    lazy val result = {
      get("/error/already-enrolled")
    }
    "return OK" in {
      result.status mustBe OK
    }
    testEnrolmentAlreadyAllocatedErrorViewTest(result)
  }

}
