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
        val result = await(find(testVatNumber, testInternalId))

        result.exists(_.identifier == testVatNumber) mustBe true
        result.exists(_.identifier == testInternalId) mustBe true
        result.forall(_.failedAttempts == 1) mustBe true
      }
    }

    "return None when the journey does not exist" in {
      await(find(testVatNumber, testInternalId)) mustBe Seq.empty[Lock]
    }
  }

  "updateAttempts" should {
    "successfully upsert data" in {
      await(UserLockRepository.updateAttempts(testVatNumber, testInternalId)).values.forall(_ == 1) mustBe true

      await(find(testVatNumber)) match {
        case Some(Lock(vrn, attempts, _)) =>
          vrn mustBe testVatNumber
          attempts mustBe 1
        case None => fail("A vrn lock should have been retrieved from the journey data database")
      }

      await(find(testInternalId)) match {
        case Some(Lock(userId, attempts, _)) =>
          userId mustBe testInternalId
          attempts mustBe 1
        case None => fail("A user lock should have been retrieved from the journey data database")
      }
    }

    "successfully update data when data is already stored against a key" in {
      await(UserLockRepository.updateAttempts(testVatNumber, testInternalId)).values.forall(_ == 1) mustBe true
      await(UserLockRepository.updateAttempts(testVatNumber, testInternalId)).values.forall(_ == 2) mustBe true
      await(UserLockRepository.updateAttempts(testVatNumber, testInternalId)).values.forall(_ == 3) mustBe true

      await(find(testVatNumber)) match {
        case Some(Lock(vrn, attempts, _)) =>
          vrn mustBe testVatNumber
          attempts mustBe 3
        case None => fail("A document should have been retrieved from the journey data database")
      }

      await(find(testInternalId)) match {
        case Some(Lock(userId, attempts, _)) =>
          userId mustBe testInternalId
          attempts mustBe 3
        case None => fail("A document should have been retrieved from the journey data database")
      }
    }
  }

  "isVrnOrUserLocked" should {
    "return false for a VRN with 1 less than the configured attempt limit" in {
      val one = await(UserLockRepository.updateAttempts(testVatNumber, testInternalId))
      val two = await(UserLockRepository.updateAttempts(testVatNumber, testInternalId))

      one("vrn") mustBe 1
      one("user") mustBe 1

      two("vrn") mustBe 2
      two("user") mustBe 2

      await(UserLockRepository.isVrnOrUserLocked(testVatNumber, testInternalId)) mustBe false
    }

    "return true for a VRN when it has reached the limit, regardless of user, but without locking user" in {
      val one = await(UserLockRepository.updateAttempts(testVatNumber, testInternalId))
      val two = await(UserLockRepository.updateAttempts(testVatNumber, testCredentialId))
      val three = await(UserLockRepository.updateAttempts(testVatNumber, testInternalId))

      one("vrn") mustBe 1
      one("user") mustBe 1

      two("vrn") mustBe 2
      two("user") mustBe 1

      three("vrn") mustBe 3
      three("user") mustBe 2


      await(UserLockRepository.isVrnOrUserLocked(testVatNumber, testInternalId)) mustBe true
      await(UserLockRepository.isVrnOrUserLocked(testVatNumber, testCredentialId)) mustBe true
      await(UserLockRepository.isVrnOrUserLocked(differentTestVatNumber, testCredentialId)) mustBe false
      await(UserLockRepository.isVrnOrUserLocked(differentTestVatNumber, testCredentialId)) mustBe false
    }

    "return true for a VRN when it hasn't reached the limit but the user ID is locked from another VRN" in {
      val one = await(UserLockRepository.updateAttempts(testVatNumber, testInternalId))
      val two = await(UserLockRepository.updateAttempts(testVatNumber, testInternalId))

      val three = await(UserLockRepository.updateAttempts(differentTestVatNumber, testCredentialId))

      await(UserLockRepository.isVrnOrUserLocked(testVatNumber, testInternalId)) mustBe false

      one("vrn") mustBe 1
      one("user") mustBe 1

      two("vrn") mustBe 2
      two("user") mustBe 2

      three("vrn") mustBe 1
      three("user") mustBe 1

      val four = await(UserLockRepository.updateAttempts(differentTestVatNumber, testInternalId))

      four("vrn") mustBe 2
      four("user") mustBe 3

      await(UserLockRepository.isVrnOrUserLocked(testVatNumber, testInternalId)) mustBe true
    }
  }
}
