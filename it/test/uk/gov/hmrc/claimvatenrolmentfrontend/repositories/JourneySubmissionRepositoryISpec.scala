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

import play.api.libs.json.{JsObject, Json}
import play.api.test.Helpers._
import uk.gov.hmrc.claimvatenrolmentfrontend.assets.TestConstants._
import uk.gov.hmrc.claimvatenrolmentfrontend.repositories.JourneySubmissionRepository._
import uk.gov.hmrc.claimvatenrolmentfrontend.utils.JourneyMongoHelper

class JourneySubmissionRepositoryISpec extends JourneyMongoHelper {

  "insertSubmissionData" should {
    "successfully insert the SubmissionData" in {
      val expectedJson: JsObject = Json.obj(
        JourneySubmissionIdKey -> testJourneyId,
        SubmissionVrnKey -> testVatNumber,
        SubmissionNumberKey -> testSubmissionNumber1,
        AccountStatusKey -> testAccountStatusUnLocked
      )

      await(journeySubmissionRepository.insertSubmissionData(testJourneyId, testVatNumber, testSubmissionNumber1, testAccountStatusUnLocked))
      await(retrieveSubmissionData(testJourneyId, testVatNumber)) match {
        case Some(document: JsObject) => document - UniqueId - LastAttemptAtKey mustBe expectedJson
        case None => fail("A document should have been retrieved from the journey data database")
      }
    }
  }

  s"getSubmissionData(${testJourneyId})" should {
    "successfully return a full SubmissionData" in {
        await(insertSubmissionData(
          testJourneyId,
          testVatNumber,
          testSubmissionNumber2,
          testAccountStatusUnLocked,
          testSubmissionDataAttempt1
        ))
        await(journeySubmissionRepository.findSubmissionData(testJourneyId, testVatNumber)) mustBe Some(testSubmissionDataAttempt1)
    }
  }

  "updateSubmissionData" should {
    "successfully insert data" in {
      val expectedJson: JsObject = Json.obj(
        JourneySubmissionIdKey -> testJourneyId,
        SubmissionVrnKey -> testVatNumber,
        SubmissionNumberKey -> testSubmissionNumber2,
        AccountStatusKey -> testAccountStatusUnLocked
      )
      await(journeySubmissionRepository.insertSubmissionData(testJourneyId, testVatNumber, testSubmissionNumber1, testAccountStatusUnLocked))
      await(journeySubmissionRepository.updateSubmissionData(testJourneyId, testVatNumber, testSubmissionNumber2, testAccountStatusUnLocked)) mustBe testSubmissionUpdateStatusTrue

      await(retrieveSubmissionData(testJourneyId, testVatNumber)) match {
        case Some(document) => document - UniqueId - LastAttemptAtKey mustBe expectedJson
        case None => fail("A document should have been retrieved from the journey data database")
      }
    }

    "successfully update data when data is already stored against a key" in {
      await(journeySubmissionRepository.insertSubmissionData(testJourneyId, testVatNumber, testSubmissionNumber1, testAccountStatusUnLocked))
      await(journeySubmissionRepository.updateSubmissionData(testJourneyId, testVatNumber, testSubmissionNumber2, testAccountStatusUnLocked)) mustBe testSubmissionUpdateStatusTrue
      await(journeySubmissionRepository.updateSubmissionData(testJourneyId, testVatNumber, testSubmissionNumber3, testAccountStatusLocked)) mustBe testSubmissionUpdateStatusTrue

      await(retrieveSubmissionData(testJourneyId, testVatNumber)).map(
        json => (json \ accountStatusKey).as[String]) mustBe Some(testAccountStatusLocked)
    }

    "return false when the journey does not exist" in {
      await(journeySubmissionRepository.insertSubmissionData(testJourneyId, testVatNumber, testSubmissionNumber1, testAccountStatusUnLocked))
      await(journeySubmissionRepository.updateSubmissionData(s"${testJourneyId}1", testVatNumber, testSubmissionNumber2, testAccountStatusUnLocked)) mustBe testSubmissionUpdateStatusFalse
    }
  }

}
