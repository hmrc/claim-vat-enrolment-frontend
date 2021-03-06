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

package uk.gov.hmrc.claimvatenrolmentfrontend.connectors

import play.api.http.Status.NO_CONTENT
import play.api.test.Helpers._
import uk.gov.hmrc.claimvatenrolmentfrontend.assets.TestConstants._
import uk.gov.hmrc.claimvatenrolmentfrontend.httpparsers.QueryUsersHttpParser.{NoUsersFound, UsersFound}
import uk.gov.hmrc.claimvatenrolmentfrontend.stubs.EnrolmentStoreProxyStub
import uk.gov.hmrc.claimvatenrolmentfrontend.utils.ComponentSpecHelper
import uk.gov.hmrc.http.{HeaderCarrier, InternalServerException}

class EnrolmentStoreProxyConnectorISpec extends ComponentSpecHelper with EnrolmentStoreProxyStub{

  private lazy val enrolmentStoreProxyConnector = app.injector.instanceOf[EnrolmentStoreProxyConnector]

  private implicit val headerCarrier: HeaderCarrier = HeaderCarrier()

  "EnrolmentStoreProxyConnector" should {
    "return EnrolmentSuccess" in {
      stubGetUserIds(testVatNumber)(OK)

      val result = await(enrolmentStoreProxyConnector.getUserIds(testVatNumber))

      result mustBe UsersFound
    }
    "return noUsersFound" in {
      stubGetUserIds(testVatNumber)(NO_CONTENT)

      val result = await(enrolmentStoreProxyConnector.getUserIds(testVatNumber))

      result mustBe NoUsersFound
    }
    "throw exception" in {
      stubGetUserIds(testVatNumber)(INTERNAL_SERVER_ERROR)

      intercept[InternalServerException](await(enrolmentStoreProxyConnector.getUserIds(testVatNumber)))
    }
  }

}
