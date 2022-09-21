/*
 * Copyright 2022 HM Revenue & Customs
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
import reactivemongo.play.json._
import uk.gov.hmrc.claimvatenrolmentfrontend.assets.TestConstants._
import uk.gov.hmrc.claimvatenrolmentfrontend.models.JourneyData
import uk.gov.hmrc.claimvatenrolmentfrontend.repositories.JourneyDataRepository._
import uk.gov.hmrc.claimvatenrolmentfrontend.utils.ComponentSpecHelper

import scala.concurrent.ExecutionContext.Implicits.global

class JourneyDataRepositoryISpec extends ComponentSpecHelper {

  val repo: JourneyDataRepository = app.injector.instanceOf[JourneyDataRepository]

  override def beforeEach(): Unit = {
    super.beforeEach()
    await(repo.drop)
  }

  "insertJourneyVatNumber" should {
    "successfully insert the vatNumber" in {
      await(repo.insertJourneyVatNumber(testJourneyId, testInternalId, testVatNumber))
      await(repo.findById(testJourneyId)) mustBe Some(JourneyData(testJourneyId))
      await(repo.collection.find[JsObject, JsObject](
        Json.obj("_id" -> testJourneyId),
        Some(Json.obj(
          JourneyIdKey -> 0,
          AuthInternalIdKey -> 0,
          "creationTimestamp" -> 0
        ))).one[JsObject]) mustBe Some(Json.obj(VatNumberKey -> testVatNumber))
    }
  }

  s"getJourneyData($testJourneyId)" should {
    "successfully return a full VatKnownFacts" when {
      "all data is populated" in {
        await(repo.collection.insert(ordered = false).one(
          Json.obj(
            JourneyIdKey -> testJourneyId,
            AuthInternalIdKey -> testInternalId
          ) ++ Json.toJsObject(testFullVatKnownFacts)
        ))

        await(repo.getJourneyData(testJourneyId, testInternalId)) mustBe Some(testFullVatKnownFacts)
      }
    }

    "successfully return partial VatKnownFacts" when {
      "there is no postcode" in {
        await(repo.collection.insert(ordered = false).one(
          Json.obj(
            JourneyIdKey -> testJourneyId,
            AuthInternalIdKey -> testInternalId
          ) ++ Json.toJsObject(testVatKnownFactsNoPostcode)
        ))

        await(repo.getJourneyData(testJourneyId, testInternalId)) mustBe Some(testVatKnownFactsNoPostcode)
      }

      "there is no postcode and no returns information" in {
        await(repo.collection.insert(ordered = false).one(
          Json.obj(
            JourneyIdKey -> testJourneyId,
            AuthInternalIdKey -> testInternalId
          ) ++ Json.toJsObject(testVatKnownFactsNoReturnsNoPostcode)
        ))

        await(repo.getJourneyData(testJourneyId, testInternalId)) mustBe Some(testVatKnownFactsNoReturnsNoPostcode)
      }

      "there is no returns information" in {
        await(repo.collection.insert(ordered = false).one(
          Json.obj(
            JourneyIdKey -> testJourneyId,
            AuthInternalIdKey -> testInternalId
          ) ++ Json.toJsObject(testVatKnownFactsNoReturns)
        ))

        await(repo.getJourneyData(testJourneyId, testInternalId)) mustBe Some(testVatKnownFactsNoReturns)
      }
    }
  }

  "updateJourneyData" should {
    "successfully insert data" in {
      val testKey = "testKey"
      val testData = "test"
      await(repo.insertJourneyVatNumber(testJourneyId, testInternalId, testVatNumber))
      await(repo.updateJourneyData(testJourneyId, testKey, JsString(testData), testInternalId))
      await(repo.collection.find[JsObject, JsObject](
        Json.obj(JourneyIdKey -> testJourneyId),
        None
      ).one[JsObject]).map(json => (json \ testKey).as[String]) mustBe Some(testData)
    }

    "successfully update data when data is already stored against a key" in {
      val testKey = "testKey"
      val testData = "test"
      val updatedData = "updated"
      await(repo.insertJourneyVatNumber(testJourneyId, testInternalId, testVatNumber))
      await(repo.updateJourneyData(testJourneyId, testKey, JsString(testData), testInternalId))
      await(repo.updateJourneyData(testJourneyId, testKey, JsString(updatedData), testInternalId))
      await(repo.collection.find[JsObject, JsObject](
        Json.obj(JourneyIdKey -> testJourneyId),
        None
      ).one[JsObject]).map(json => (json \ testKey).as[String]) mustBe Some(updatedData)
    }

  }

  "removeJourneyDataFields" should {
    "successfully remove one field" in {
      val testKey = "testKey"
      val testData = "test"
      val testSecondKey = "secondKey"
      val testSecondData = "secondTest"

      await(repo.collection.insert(ordered = false).one(
        Json.obj(
          JourneyIdKey -> testJourneyId,
          AuthInternalIdKey -> testInternalId,
          testKey -> testData,
          testSecondKey -> testSecondData
        )
      ))

      await(repo.removeJourneyDataFields(testJourneyId, testInternalId, Seq(testSecondKey)))
      await(repo.collection.find[JsObject, JsObject](
        Json.obj(JourneyIdKey -> testJourneyId),
        None
      ).one[JsObject]) mustBe Some(Json.obj(
        JourneyIdKey -> testJourneyId,
        AuthInternalIdKey -> testInternalId,
        testKey -> testData))
    }
    "pass successfully when the field is not present" in {
      val testKey = "testKey"
      val testData = "test"
      val testSecondKey = "secondKey"

      await(repo.collection.insert(ordered = false).one(
        Json.obj(
          JourneyIdKey -> testJourneyId,
          AuthInternalIdKey -> testInternalId,
          testKey -> testData,
        )
      ))

      await(repo.removeJourneyDataFields(testJourneyId, testInternalId, Seq(testSecondKey)))
      await(repo.collection.find[JsObject, JsObject](
        Json.obj(JourneyIdKey -> testJourneyId),
        None
      ).one[JsObject]) mustBe Some(Json.obj(
        JourneyIdKey -> testJourneyId,
        AuthInternalIdKey -> testInternalId,
        testKey -> testData))
    }
    "successfully remove two fields" in {
      val testKey = "testKey"
      val testData = "test"
      val testSecondKey = "secondKey"
      val testSecondData = "secondTest"

      await(repo.collection.insert(ordered = false).one(
        Json.obj(
          JourneyIdKey -> testJourneyId,
          AuthInternalIdKey -> testInternalId,
          testKey -> testData,
          testSecondKey -> testSecondData
        )
      ))

      await(repo.removeJourneyDataFields(testJourneyId, testInternalId, Seq(testKey, testSecondKey)))
      await(repo.collection.find[JsObject, JsObject](
        Json.obj(JourneyIdKey -> testJourneyId),
        None
      ).one[JsObject]) mustBe Some(Json.obj(
        JourneyIdKey -> testJourneyId,
        AuthInternalIdKey -> testInternalId))
    }
    "successfully remove one field if the second field is not present" in {
      val testKey = "testKey"
      val testData = "test"
      val testSecondKey = "secondKey"

      await(repo.collection.insert(ordered = false).one(
        Json.obj(
          JourneyIdKey -> testJourneyId,
          AuthInternalIdKey -> testInternalId,
          testKey -> testData,
        )
      ))

      await(repo.removeJourneyDataFields(testJourneyId, testInternalId, Seq(testKey, testSecondKey)))
      await(repo.collection.find[JsObject, JsObject](
        Json.obj(JourneyIdKey -> testJourneyId),
        None
      ).one[JsObject]) mustBe Some(Json.obj(
        JourneyIdKey -> testJourneyId,
        AuthInternalIdKey -> testInternalId))
    }
    "pass successfully when two keys are passed in but neither field is present" in {
      val testKey = "testKey"
      val testSecondKey = "secondKey"

      await(repo.collection.insert(ordered = false).one(
        Json.obj(
          JourneyIdKey -> testJourneyId,
          AuthInternalIdKey -> testInternalId
        )
      ))

      await(repo.removeJourneyDataFields(testJourneyId, testInternalId, Seq(testKey, testSecondKey)))
      await(repo.collection.find[JsObject, JsObject](
        Json.obj(JourneyIdKey -> testJourneyId),
        None
      ).one[JsObject]) mustBe Some(Json.obj(
        JourneyIdKey -> testJourneyId,
        AuthInternalIdKey -> testInternalId))
    }
  }

}
