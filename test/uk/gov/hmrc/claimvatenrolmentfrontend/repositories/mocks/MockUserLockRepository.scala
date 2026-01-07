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

package uk.gov.hmrc.claimvatenrolmentfrontend.repositories.mocks

import org.mockito.ArgumentMatchers
import org.mockito.Mockito.{reset, verify, when}
import org.mockito.stubbing.OngoingStubbing
import org.scalatest.{BeforeAndAfterEach, Suite}
import org.scalatestplus.mockito.MockitoSugar
import uk.gov.hmrc.claimvatenrolmentfrontend.models.Lock
import uk.gov.hmrc.claimvatenrolmentfrontend.repositories.UserLockRepository

import scala.concurrent.Future

trait MockUserLockRepository extends MockitoSugar with BeforeAndAfterEach {
  self: Suite =>

  val mockUserLockRepository: UserLockRepository = mock[UserLockRepository]

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockUserLockRepository)
  }

  def mockIsVrnLocked(vrn: String, userId: String)(response: Future[Boolean]): OngoingStubbing[_] =
    when(mockUserLockRepository.isVrnOrUserLocked(
      ArgumentMatchers.eq(vrn),
      ArgumentMatchers.eq(userId),
    )).thenReturn(response)

  def mockUpdateSubmissionData(vrn: String, userId: String)(response: Future[Lock]): OngoingStubbing[_] =
    when(mockUserLockRepository.updateAttempts(ArgumentMatchers.eq(vrn), ArgumentMatchers.eq(userId: String))).thenReturn(response)

  def verifyUpdateSubmissionData(vrn: String, userId: String): Unit =
    verify(mockUserLockRepository.updateAttempts(ArgumentMatchers.eq(vrn), ArgumentMatchers.eq(userId: String)))

  def verifyFindSubmissionData(vrn: String, userId: String): Unit =
    verify(mockUserLockRepository).find(ArgumentMatchers.eq(vrn), ArgumentMatchers.eq(userId: String))

}
