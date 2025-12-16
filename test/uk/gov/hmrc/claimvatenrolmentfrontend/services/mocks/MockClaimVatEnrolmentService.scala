/*
 * Copyright 2025 HM Revenue & Customs
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
import org.mockito.Mockito.{reset, when}
import org.mockito.stubbing.OngoingStubbing
import org.scalatest.{BeforeAndAfterEach, Suite}
import org.scalatestplus.mockito.MockitoSugar
import uk.gov.hmrc.auth.core.{AffinityGroup, AuthConnector}
import uk.gov.hmrc.claimvatenrolmentfrontend.services.ClaimVatEnrolmentService
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

trait MockClaimVatEnrolmentService extends MockitoSugar with BeforeAndAfterEach {
  self: Suite =>

  val mockClaimVatEnrolmentService: ClaimVatEnrolmentService = mock[ClaimVatEnrolmentService]
  val mockAuthConnector: AuthConnector = mock[AuthConnector]
  val agentAffinity: AffinityGroup = AffinityGroup.Agent

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockClaimVatEnrolmentService)
  }

  def mockAuthRetrieval()(implicit hc: HeaderCarrier, ec: ExecutionContext, response: Future[AffinityGroup]): OngoingStubbing[Future[AffinityGroup]] =
    when(mockClaimVatEnrolmentService.retrieveIdentityDetails()(ArgumentMatchers.eq(hc), ArgumentMatchers.eq(ec))
    ).thenReturn(response)

  def mockAuthorise() {
    when(
      mockAuthConnector.authorise[Option[AffinityGroup]](ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any(), ArgumentMatchers.any())
    ).thenReturn(Future.successful(Some(agentAffinity)))
  }

}
