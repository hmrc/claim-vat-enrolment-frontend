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
import uk.gov.hmrc.claimvatenrolmentfrontend.models.{JourneyConfig, VatKnownFacts}
import uk.gov.hmrc.claimvatenrolmentfrontend.services.JourneyService

import scala.concurrent.Future

trait MockJourneyService extends MockitoSugar with BeforeAndAfterEach {
  self: Suite =>

  val mockJourneyService: JourneyService = mock[JourneyService]

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockJourneyService)
  }

  def mockRetrieveJourneyConfig(journeyId: String,
                                authInternalId: String)
                               (implicit response: Future[Option[JourneyConfig]]): OngoingStubbing[Future[Option[JourneyConfig]]] =
    when(mockJourneyService.retrieveJourneyConfig(ArgumentMatchers.eq(journeyId), ArgumentMatchers.eq(authInternalId))(any())).thenReturn(response)

  def mockFailRetrieveJourneyConfig(journeyId: String,
                                authInternalId: String): OngoingStubbing[Future[Option[JourneyConfig]]] =
    when(mockJourneyService.retrieveJourneyConfig(ArgumentMatchers.eq(journeyId), ArgumentMatchers.eq(authInternalId))(any())).thenReturn(Future.successful(None))

  def mockRetrieveJourneyData(journeyId: String,
                              authInternalId: String
                             )(implicit response: Future[Option[VatKnownFacts]]): OngoingStubbing[Future[Option[VatKnownFacts]]] =
    when(mockJourneyService.retrieveJourneyData(
      ArgumentMatchers.eq(journeyId),
      ArgumentMatchers.eq(authInternalId))(any())
    ).thenReturn(response)

  def mockFailRetrieveJourneyData(journeyId: String,
                              authInternalId: String): OngoingStubbing[Future[Option[VatKnownFacts]]] =
    when(mockJourneyService.retrieveJourneyData(
      ArgumentMatchers.eq(journeyId),
      ArgumentMatchers.eq(authInternalId))(any())
    ).thenReturn(Future.successful(None))
}
