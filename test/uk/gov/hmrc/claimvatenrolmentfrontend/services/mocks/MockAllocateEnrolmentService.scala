/*
 * Copyright 2026 HM Revenue & Customs
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

package uk.gov.hmrc.claimvatenrolmentfrontend.services.mocks

import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.mockito.stubbing.OngoingStubbing
import org.scalatest.{BeforeAndAfterEach, Suite}
import org.scalatestplus.mockito.MockitoSugar
import uk.gov.hmrc.claimvatenrolmentfrontend.httpparsers.QueryUsersHttpParser.QueryUsersSuccess
import uk.gov.hmrc.claimvatenrolmentfrontend.models.{AllocateEnrolmentResponse, VatKnownFacts}
import uk.gov.hmrc.claimvatenrolmentfrontend.services.AllocateEnrolmentService
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

trait MockAllocateEnrolmentService extends MockitoSugar with BeforeAndAfterEach {
  self: Suite =>

  val mockAllocateEnrolmentService: AllocateEnrolmentService = mock[AllocateEnrolmentService]

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockAllocateEnrolmentService)
  }

  def mockAllocateEnrolment(vatKnownFacts: VatKnownFacts,
                            credentialId: String,
                            groupId: String
                           )(response: Future[AllocateEnrolmentResponse]): OngoingStubbing[Future[AllocateEnrolmentResponse]] =
    when(mockAllocateEnrolmentService.allocateEnrolment(ArgumentMatchers.eq(vatKnownFacts),
      ArgumentMatchers.eq(credentialId),
      ArgumentMatchers.eq(groupId)
    )(ArgumentMatchers.any[HeaderCarrier], any())
    ).thenReturn(response)

  def mockGetUserIds(vatNumber: String)
                    (response: Future[QueryUsersSuccess]): OngoingStubbing[Future[QueryUsersSuccess]] =
    when(mockAllocateEnrolmentService.getUserIds(ArgumentMatchers.eq(vatNumber)
    )(ArgumentMatchers.any[HeaderCarrier], any())
    ).thenReturn(response)

}
