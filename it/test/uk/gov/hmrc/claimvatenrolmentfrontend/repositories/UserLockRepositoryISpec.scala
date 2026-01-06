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

package uk.gov.hmrc.claimvatenrolmentfrontend.repositories

import play.api.test.Helpers._
import uk.gov.hmrc.claimvatenrolmentfrontend.assets.TestConstants._
import uk.gov.hmrc.claimvatenrolmentfrontend.models.Lock
import uk.gov.hmrc.claimvatenrolmentfrontend.utils.JourneyMongoHelper

class UserLockRepositoryISpec extends JourneyMongoHelper {

  "find" should {
    "return Some when the journey data exists" should {
      "successfully return a lock" in {
        await(insertLockData(testVatNumber, testInternalId, testSubmissionNumber1))
        val result = await(UserLockRepository.find(testVatNumber, testInternalId)).get

        result.vrn mustBe testVatNumber
        result.userId mustBe testInternalId
        result.failedAttempts mustBe 1
      }
    }

    "return None when the journey does not exist" in {
      await(UserLockRepository.find(testVatNumber, testInternalId)) mustBe None
    }
  }

  "updateAttempts" should {
    "successfully upsert data" in {
      await(UserLockRepository.updateAttempts(testVatNumber, testInternalId)).failedAttempts mustBe 1

      await(UserLockRepository.find(testVatNumber, testInternalId)) match {
        case Some(Lock(vrn, userId, attempts, _)) =>
          vrn mustBe testVatNumber
          userId mustBe testInternalId
          attempts mustBe 1
        case None => fail("A document should have been retrieved from the journey data database")
      }
    }

    "successfully update data when data is already stored against a key" in {
      await(UserLockRepository.updateAttempts(testVatNumber, testInternalId)).failedAttempts mustBe 1
      await(UserLockRepository.updateAttempts(testVatNumber, testInternalId)).failedAttempts mustBe 2
      await(UserLockRepository.updateAttempts(testVatNumber, testInternalId)).failedAttempts mustBe 3

      await(UserLockRepository.find(testVatNumber, testInternalId)) match {
        case Some(Lock(vrn, userId, attempts, _)) =>
          vrn mustBe testVatNumber
          userId mustBe testInternalId
          attempts mustBe 3
        case None => fail("A document should have been retrieved from the journey data database")
      }
    }
  }

  "isVrnOrUserLocked" should {
    "return false for a VRN with 1 less than the configured attempt limit" in {
      await(UserLockRepository.updateAttempts(testVatNumber, testInternalId)).failedAttempts mustBe 1
      await(UserLockRepository.updateAttempts(testVatNumber, testInternalId)).failedAttempts mustBe 2

      await(UserLockRepository.isVrnOrUserLocked(testVatNumber, testInternalId)) mustBe false
    }

    "return false for a VRN when it hasn't reached the limit, even when the limit is exceeded across all records" in {
      await(UserLockRepository.updateAttempts(testVatNumber, testInternalId)).failedAttempts mustBe 1
      await(UserLockRepository.updateAttempts(testVatNumber, testInternalId)).failedAttempts mustBe 2
      await(UserLockRepository.updateAttempts(differentTestVatNumber, testInternalId)).failedAttempts mustBe 1
      await(UserLockRepository.updateAttempts(differentTestVatNumber, testInternalId)).failedAttempts mustBe 2

      await(UserLockRepository.isVrnOrUserLocked(testVatNumber, testInternalId)) mustBe false
    }

    "return true for a VRN when it has reached the limit, regardless of user" in {
      await(UserLockRepository.updateAttempts(testVatNumber, testInternalId)).failedAttempts mustBe 1
      await(UserLockRepository.updateAttempts(testVatNumber, testInternalId)).failedAttempts mustBe 2
      await(UserLockRepository.updateAttempts(testVatNumber, testInternalId)).failedAttempts mustBe 3

      await(UserLockRepository.isVrnOrUserLocked(testVatNumber, testInternalId)) mustBe true
      await(UserLockRepository.isVrnOrUserLocked(testVatNumber, testCredentialId)) mustBe true
    }

    "return true for a VRN when it hasn't reached the limit but the user ID is locked from another VRN" in {
      await(UserLockRepository.updateAttempts(testVatNumber, testInternalId)).failedAttempts mustBe 1
      await(UserLockRepository.updateAttempts(testVatNumber, testInternalId)).failedAttempts mustBe 2
      await(UserLockRepository.updateAttempts(differentTestVatNumber, testInternalId)).failedAttempts mustBe 1
      await(UserLockRepository.updateAttempts(differentTestVatNumber, testInternalId)).failedAttempts mustBe 2
      await(UserLockRepository.updateAttempts(differentTestVatNumber, testInternalId)).failedAttempts mustBe 3

      await(UserLockRepository.isVrnOrUserLocked(testVatNumber, testInternalId)) mustBe true
    }
  }

}
