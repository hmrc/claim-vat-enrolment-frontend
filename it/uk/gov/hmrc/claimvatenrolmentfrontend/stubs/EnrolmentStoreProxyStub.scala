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

package uk.gov.hmrc.claimvatenrolmentfrontend.stubs

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import uk.gov.hmrc.claimvatenrolmentfrontend.utils.WireMockMethods

trait EnrolmentStoreProxyStub extends WireMockMethods {

  private def getUserIdsUrl(enrolmentKey: String): String = s"/enrolment-store-proxy/enrolment-store/enrolments/$enrolmentKey/users\\?type=principal"
  private def getUserIdsUrlStub(enrolmentKey: String): String = s"/claim-vat-enrolment/test-only/enrolments/$enrolmentKey/users\\?type=principal"

  def stubGetUserIds(vatNumber: String)(status: Int): StubMapping =
    when(method = GET, uri = getUserIdsUrl(s"HMRC-MTD-VAT~VRN~$vatNumber")).thenReturn(status)

  def stubGetUserIdsForStub(vatNumber: String)(status: Int): StubMapping =
    when(method = GET, uri = getUserIdsUrlStub(s"HMRC-MTD-VAT~VRN~$vatNumber")).thenReturn(status)
}

