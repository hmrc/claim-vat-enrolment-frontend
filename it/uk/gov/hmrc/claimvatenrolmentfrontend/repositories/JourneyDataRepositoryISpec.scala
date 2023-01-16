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

package uk.gov.hmrc.claimvatenrolmentfrontend.repositories

import play.api.libs.json.{JsObject, JsString, Json}
import play.api.test.Helpers._
import uk.gov.hmrc.claimvatenrolmentfrontend.assets.TestConstants._
import uk.gov.hmrc.claimvatenrolmentfrontend.repositories.JourneyDataRepository._
import uk.gov.hmrc.claimvatenrolmentfrontend.utils.JourneyMongoHelper

class JourneyDataRepositoryISpec extends JourneyMongoHelper {

  "insertJourneyVatNumber" should {
    "successfully insert the vatNumber" in {

      val expectedJson: JsObject = Json.obj(
        JourneyIdKey -> testJourneyId,
        AuthInternalIdKey -> testInternalId,
        VatNumberKey -> testVatNumber
      )

      await(journeyDataRepository.insertJourneyVatNumber(testJourneyId, testInternalId, testVatNumber))

      await(retrieveJourneyDataAsJsObject(testJourneyId, testInternalId)) match {
        case Some(document: JsObject) => document - CreationTimestampKey mustBe expectedJson
        case None => fail("A document should have been retrieved from the journey data database")
      }
    }
  }

  s"getJourneyData($testJourneyId)" should {
    "successfully return a full VatKnownFacts" when {
      "all data is populated" in {
        await(insertVatKnownFactsData(
          testJourneyId,
          testInternalId,
          testFullVatKnownFacts
        ))

        await(journeyDataRepository.getJourneyData(testJourneyId, testInternalId)) mustBe Some(testFullVatKnownFacts)
      }
    }

    "successfully return partial VatKnownFacts" when {
      "there is no postcode" in {
        await(insertVatKnownFactsData(
          testJourneyId,
          testInternalId,
          testVatKnownFactsNoPostcode
        ))

        await(journeyDataRepository.getJourneyData(testJourneyId, testInternalId)) mustBe Some(testVatKnownFactsNoPostcode)
      }

      "there is no postcode and no returns information" in {
        await(insertVatKnownFactsData(
          testJourneyId,
          testInternalId,
          testVatKnownFactsNoReturnsNoPostcode
        ))

        await(journeyDataRepository.getJourneyData(testJourneyId, testInternalId)) mustBe Some(testVatKnownFactsNoReturnsNoPostcode)
      }

      "there is no returns information" in {
        await(insertVatKnownFactsData(
          testJourneyId,
          testInternalId,
          testVatKnownFactsNoReturns
        ))

        await(journeyDataRepository.getJourneyData(testJourneyId, testInternalId)) mustBe Some(testVatKnownFactsNoReturns)
      }
    }
  }

  "updateJourneyData" should {
    "successfully insert data" in {

      val expectedJson: JsObject = Json.obj(
        JourneyIdKey -> testJourneyId,
        AuthInternalIdKey -> testInternalId,
        VatNumberKey -> testVatNumber,
        testKey -> testData
      )

      await(journeyDataRepository.insertJourneyVatNumber(testJourneyId, testInternalId, testVatNumber))

      await(journeyDataRepository.updateJourneyData(testJourneyId, testKey, JsString(testData), testInternalId)) mustBe true

      await(retrieveJourneyDataAsJsObject(testJourneyId, testInternalId)) match {
        case Some(document) => document - CreationTimestampKey mustBe expectedJson
        case None => fail("A document should have been retrieved from the journey data database")
      }
    }

    "successfully update data when data is already stored against a key" in {

      await(journeyDataRepository.insertJourneyVatNumber(testJourneyId, testInternalId, testVatNumber))
      await(journeyDataRepository.updateJourneyData(testJourneyId, testKey, JsString(testData), testInternalId)) mustBe true
      await(journeyDataRepository.updateJourneyData(testJourneyId, testKey, JsString(updatedData), testInternalId)) mustBe true

      await(retrieveJourneyDataAsJsObject(testJourneyId, testInternalId)).map(
        json => (json \ testKey).as[String]) mustBe Some(updatedData)
    }

    "return false when the journey does not exist" in {

      await(journeyDataRepository.insertJourneyVatNumber(testJourneyId, testInternalId, testVatNumber))

      await(journeyDataRepository.updateJourneyData(testJourneyId + 1, testKey, JsString(testData), testInternalId)) mustBe false
    }

  }

  "removeJourneyDataFields" should {
    "successfully remove one field" in {

      val expectedJson: JsObject = Json.obj(
        JourneyIdKey -> testJourneyId,
        AuthInternalIdKey -> testInternalId,
        testKey -> testData
      )

      await(insertJourneyDataAsJsObject(
        testJourneyId,
        testInternalId,
        Json.obj(
          testKey -> testData,
          testSecondKey -> testSecondData
          )
        )
      )

      await(journeyDataRepository.removeJourneyDataFields(testJourneyId, testInternalId, Seq(testSecondKey))) mustBe true

      await(retrieveJourneyDataAsJsObject(testJourneyId, testInternalId)) match {
        case Some(document: JsObject) => document - CreationTimestampKey mustBe expectedJson
        case None => fail("A document should have been retrieved from the journey data database")
      }
    }
    "return false if the journey does not exist" in {

      await(insertJourneyDataAsJsObject(
        testJourneyId,
        testInternalId,
        Json.obj(
          testKey -> testData,
          testSecondKey -> testSecondData
          )
        )
      )

      await(journeyDataRepository.removeJourneyDataFields(testJourneyId + 1, testInternalId, Seq(testSecondKey))) mustBe false

    }

    "pass successfully when the field is not present" in {

      val expectedJson: JsObject = Json.obj(
        JourneyIdKey -> testJourneyId,
        AuthInternalIdKey -> testInternalId,
        testKey -> testData
      )

      await(insertJourneyDataAsJsObject(
        testJourneyId,
        testInternalId,
        Json.obj(
          testKey -> testData,
          )
        )
      )

      await(journeyDataRepository.removeJourneyDataFields(testJourneyId, testInternalId, Seq(testSecondKey))) mustBe true

      await(retrieveJourneyDataAsJsObject(testJourneyId, testInternalId)) match {
        case Some(document: JsObject) => document - CreationTimestampKey mustBe expectedJson
        case None => fail("A document should have been retrieved from the journey data database")
      }
    }
    "successfully remove two fields" in {

      await(insertJourneyDataAsJsObject(
        testJourneyId,
        testInternalId,
        Json.obj(
          testKey -> testData,
          testSecondKey -> testSecondData
          )
        )
      )

      await(journeyDataRepository.removeJourneyDataFields(testJourneyId, testInternalId, Seq(testKey, testSecondKey))) mustBe true

      await(retrieveJourneyDataAsJsObject(testJourneyId, testInternalId)) match {
        case Some(document: JsObject) => document - CreationTimestampKey mustBe emptyJourneyDataJson
        case None => fail("A document should have been retrieved from the journey data database")
      }
    }
    "successfully remove three fields" in {

      val expectedJson: JsObject = Json.obj(
        JourneyIdKey -> testJourneyId,
        AuthInternalIdKey -> testInternalId,
        testKey -> testData,
        testFourthKey -> testFourthData
      )

      await(insertJourneyDataAsJsObject(
        testJourneyId,
        testInternalId,
        Json.obj(
          testKey -> testData,
          testSecondKey -> testSecondData,
          testThirdKey -> testThirdData,
          testFourthKey -> testFourthData,
          testFifthKey -> testFifthData
          )
        )
      )

      await(journeyDataRepository.removeJourneyDataFields(testJourneyId, testInternalId,
        Seq(testSecondKey, testThirdKey, testFifthKey))) mustBe true

      await(retrieveJourneyDataAsJsObject(testJourneyId, testInternalId)) match {
        case Some(document: JsObject) => document - CreationTimestampKey mustBe expectedJson
        case None => fail("A document should have been retrieved from the journey data database")
      }
    }
    "successfully remove one field if the second field is not present" in {

      await(insertJourneyDataAsJsObject(
        testJourneyId,
        testInternalId,
        Json.obj(
          testKey -> testData
         )
        )
      )

      await(journeyDataRepository.removeJourneyDataFields(testJourneyId, testInternalId, Seq(testKey, testSecondKey))) mustBe true

      await(retrieveJourneyDataAsJsObject(testJourneyId, testInternalId)) match {
        case Some(document: JsObject) => document - CreationTimestampKey mustBe emptyJourneyDataJson
        case None => fail("A document should have been retrieved from the journey data database")
      }
    }
    "pass successfully when two keys are passed in but neither field is present" in {

      await(insertJourneyDataAsJsObject(
        testJourneyId,
        testInternalId,
        Json.obj()
      ))

      await(journeyDataRepository.removeJourneyDataFields(testJourneyId, testInternalId, Seq(testKey, testSecondKey))) mustBe true

      await(retrieveJourneyDataAsJsObject(testJourneyId, testInternalId)) match {
        case Some(document: JsObject) => document - CreationTimestampKey mustBe emptyJourneyDataJson
        case None => fail("A document should have been retrieved from the journey data database")
      }
    }
  }

}
