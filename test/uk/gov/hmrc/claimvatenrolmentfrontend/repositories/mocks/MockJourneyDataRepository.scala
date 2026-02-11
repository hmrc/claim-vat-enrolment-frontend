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

package uk.gov.hmrc.claimvatenrolmentfrontend.repositories.mocks

import org.mockito.ArgumentMatchers
import org.mockito.Mockito.{reset, verify, when}
import org.mockito.stubbing.OngoingStubbing
import org.scalatest.{BeforeAndAfterEach, Suite}
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.json.JsValue
import uk.gov.hmrc.claimvatenrolmentfrontend.models.VatKnownFacts
import uk.gov.hmrc.claimvatenrolmentfrontend.repositories.JourneyDataRepository

import scala.concurrent.Future

trait MockJourneyDataRepository extends MockitoSugar with BeforeAndAfterEach {
  self: Suite =>

  val mockJourneyDataRepository: JourneyDataRepository = mock[JourneyDataRepository]

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockJourneyDataRepository)
  }

  def mockInsertJourneyData(journeyId: String,
                            authId: String,
                            vatNumber: String
                           )(response: Future[String]): OngoingStubbing[_] =
    when(mockJourneyDataRepository.insertJourneyVatNumber(
      ArgumentMatchers.eq(journeyId),
      ArgumentMatchers.eq(authId),
      ArgumentMatchers.eq(vatNumber),
    )).thenReturn(response)

  def mockGetJourneyData(journeyId: String,
                         authId: String
                        )(response: Future[Option[VatKnownFacts]]): OngoingStubbing[_] =
    when(mockJourneyDataRepository.getJourneyData(
      ArgumentMatchers.eq(journeyId),
      ArgumentMatchers.eq(authId)
    )).thenReturn(response)

  def mockGetVrnInfo(journeyId: String,
                     authId: String
                    )(response: Future[Option[String]]): OngoingStubbing[_] =
    when(mockJourneyDataRepository.getVRNInfo(
      ArgumentMatchers.eq(journeyId),
      ArgumentMatchers.eq(authId)
    )).thenReturn(response)

  def mockUpdateJourneyData(journeyId: String,
                            dataKey: String,
                            data: JsValue,
                            authId: String,
                           )(response: Future[Boolean]): OngoingStubbing[_] =
    when(mockJourneyDataRepository.updateJourneyData(
      ArgumentMatchers.eq(journeyId),
      ArgumentMatchers.eq(dataKey),
      ArgumentMatchers.eq(data),
      ArgumentMatchers.eq(authId)
    )).thenReturn(response)

  def verifyCreateJourney(journeyId: String,
                          authId: String,
                          vatNumber: String): Unit =
    verify(mockJourneyDataRepository).insertJourneyVatNumber(
      ArgumentMatchers.eq(journeyId),
      ArgumentMatchers.eq(authId),
      ArgumentMatchers.eq(vatNumber)
    )

  def verifyGetJourneyData(journeyId: String,
                           authId: String): Unit =
    verify(mockJourneyDataRepository).getJourneyData(
      ArgumentMatchers.eq(journeyId),
      ArgumentMatchers.eq(authId)
    )

  def verifyUpdateJourneyData(journeyId: String,
                              dataKey: String,
                              data: JsValue,
                              authId: String): Unit =
    verify(mockJourneyDataRepository).updateJourneyData(
      ArgumentMatchers.eq(journeyId),
      ArgumentMatchers.eq(dataKey),
      ArgumentMatchers.eq(data),
      ArgumentMatchers.eq(authId)
    )

}
