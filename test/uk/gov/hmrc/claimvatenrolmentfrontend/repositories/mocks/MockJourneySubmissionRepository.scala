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

package uk.gov.hmrc.claimvatenrolmentfrontend.repositories.mocks

import org.mockito.ArgumentMatchers
import org.mockito.Mockito.{reset, verify, when}
import org.mockito.stubbing.OngoingStubbing
import org.scalatest.{BeforeAndAfterEach, Suite}
import org.scalatestplus.mockito.MockitoSugar
import uk.gov.hmrc.claimvatenrolmentfrontend.models.JourneySubmission
import uk.gov.hmrc.claimvatenrolmentfrontend.repositories.JourneySubmissionRepository

import scala.concurrent.Future

trait MockJourneySubmissionRepository extends MockitoSugar with BeforeAndAfterEach {
  self: Suite =>

  val mockJourneySubmissionRepository: JourneySubmissionRepository = mock[JourneySubmissionRepository]

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockJourneySubmissionRepository)
  }

  def mockFindSubmissionData(journeyId: String,
                             vrn: String
                        )(response: Future[Option[JourneySubmission]]): OngoingStubbing[_] =
    when(mockJourneySubmissionRepository.findSubmissionData(
      ArgumentMatchers.eq(journeyId),
      ArgumentMatchers.eq(vrn)
    )).thenReturn(response)

  def mockInsertSubmissionData(journeyId: String,
                               vrn: String, submissionNumber: Int, accountStatus: String)(response: Future[String]): OngoingStubbing[_] =
    when(mockJourneySubmissionRepository.insertSubmissionData(
      ArgumentMatchers.eq(journeyId),
      ArgumentMatchers.eq(vrn),
      ArgumentMatchers.eq(submissionNumber),
      ArgumentMatchers.eq(accountStatus)
    )).thenReturn(response)

  def mockUpdateSubmissionData(journeyId: String, vrn: String, submissionNumber: Int, accountStatus: String)
                                  (response: Future[Boolean]): OngoingStubbing[_] =
    when(mockJourneySubmissionRepository.updateSubmissionData(
      ArgumentMatchers.eq(journeyId),
      ArgumentMatchers.eq(vrn),
      ArgumentMatchers.eq(submissionNumber),
      ArgumentMatchers.eq(accountStatus)
    )).thenReturn(response)

  def verifyInsertSubmissionData(journeyId: String,
                                 vrn: String, submissionNumber: Int, accountStatus: String): Unit =
    verify(mockJourneySubmissionRepository).insertSubmissionData(
      ArgumentMatchers.eq(journeyId),
      ArgumentMatchers.eq(vrn),
      ArgumentMatchers.eq(submissionNumber),
      ArgumentMatchers.eq(accountStatus)
    )

  def verifyUpdateSubmissionData(journeyId: String, vrn: String, submissionNumber: Int, accountStatus: String): Unit =
    verify(mockJourneySubmissionRepository).updateSubmissionData(
      ArgumentMatchers.eq(journeyId),
      ArgumentMatchers.eq(vrn),
      ArgumentMatchers.eq(submissionNumber),
      ArgumentMatchers.eq(accountStatus)
    )

  def verifyFindSubmissionData(journeyId: String,
                               vrn: String): Unit =
    verify(mockJourneySubmissionRepository).findSubmissionData(
      ArgumentMatchers.eq(journeyId),
      ArgumentMatchers.eq(vrn)
    )

}
