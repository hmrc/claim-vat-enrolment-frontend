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

package uk.gov.hmrc.claimvatenrolmentfrontend.services

import com.mongodb.MongoException
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.{JsValue, Json}
import play.api.test.Helpers._
import uk.gov.hmrc.claimvatenrolmentfrontend.helpers.TestConstants.{testInternalId, testJourneyId}
import uk.gov.hmrc.claimvatenrolmentfrontend.models.Postcode
import uk.gov.hmrc.claimvatenrolmentfrontend.repositories.mocks.MockJourneyDataRepository

import java.time.{LocalDate, Month}
import scala.concurrent.Future

class StoreKnownFactsServiceSpec extends AnyWordSpec with Matchers with MockJourneyDataRepository {

  object TestService extends StoreKnownFactsService(mockJourneyDataRepository)

  "storeKnownFactAnswer" should {
    "call the JourneyDataRepository to update the data for a specific key, journeyId and authId" in {
      mockUpdateJourneyData(
        journeyId = testJourneyId,
        dataKey = "dataKey",
        data = Json.toJson("dataToUpdate"),
        authId = testInternalId
      )(Future.successful(true))

      await(TestService.storeKnownFactAnswer("dataToUpdate", "dataKey", testJourneyId, testInternalId))

      verifyUpdateJourneyData(
        journeyId = testJourneyId,
        dataKey = "dataKey",
        data = Json.toJson("dataToUpdate"),
        authId = testInternalId
      )
    }

    "return 'true'" when {
      "given a String value" when {
        "the repository returns 'true', indicating it has updated the journey data" in {
          mockUpdateJourneyData(
            journeyId = testJourneyId,
            dataKey = "dataKey",
            data = Json.toJson("dataToUpdate"),
            authId = testInternalId
          )(Future.successful(true))

          val result = await(TestService.storeKnownFactAnswer("dataToUpdate", "dataKey", testJourneyId, testInternalId))

          result mustBe true
        }
      }

      "given a Boolean value" when {
        "the repository returns 'true', indicating it has updated the journey data" in {
          mockUpdateJourneyData(
            journeyId = testJourneyId,
            dataKey = "dataKey",
            data = Json.toJson(true),
            authId = testInternalId
          )(Future.successful(true))

          val result = await(TestService.storeKnownFactAnswer(value = true, "dataKey", testJourneyId, testInternalId))

          result mustBe true
        }
      }

      "given a Postcode object to parse" when {
        "parsed successfully and the repository returns 'true', indicating it has updated the journey data" in {
          val dataToParseAndStore: Postcode = Postcode("AA1 1AA")
          val parsedPostcode: JsValue       = Json.toJson("AA1 1AA")
          mockUpdateJourneyData(
            journeyId = testJourneyId,
            dataKey = "dataKey",
            data = parsedPostcode,
            authId = testInternalId
          )(Future.successful(true))

          val result = await(TestService.storeKnownFactAnswer[Postcode](dataToParseAndStore, "dataKey", testJourneyId, testInternalId))

          result mustBe true
        }
      }

      "given a Month's Int value to parse" when {
        "parsed successfully and the repository returns 'true', indicating it has updated the journey data" in {
          val dataToParseAndStore: Int = Month.JULY.getValue
          val parsedMonthInt: JsValue  = Json.toJson(7)
          mockUpdateJourneyData(
            journeyId = testJourneyId,
            dataKey = "dataKey",
            data = parsedMonthInt,
            authId = testInternalId
          )(Future.successful(true))

          val result = await(TestService.storeKnownFactAnswer[Int](dataToParseAndStore, "dataKey", testJourneyId, testInternalId))

          result mustBe true
        }
      }

      "given a LocalDate object to parse" when {
        "parsed successfully and the repository returns 'true', indicating it has updated the journey data" in {
          val dataToParseAndStore: LocalDate = LocalDate.of(2000, 2, 2)
          val parsedLocalDate: JsValue       = Json.toJson("2000-02-02")
          mockUpdateJourneyData(
            journeyId = testJourneyId,
            dataKey = "dataKey",
            data = parsedLocalDate,
            authId = testInternalId
          )(Future.successful(true))

          val result = await(TestService.storeKnownFactAnswer[LocalDate](dataToParseAndStore, "dataKey", testJourneyId, testInternalId))

          result mustBe true
        }
      }
    }

    "return 'false'" when {
      "the repository returns 'false', indicating it has failed to update the journey data" in {
        mockUpdateJourneyData(
          journeyId = testJourneyId,
          dataKey = "dataKey",
          data = Json.toJson("dataToUpdate"),
          authId = testInternalId
        )(Future.successful(false))

        val result = await(TestService.storeKnownFactAnswer("dataToUpdate", "dataKey", testJourneyId, testInternalId))

        result mustBe false
      }
    }

    "throw an exception" when {
      "the repository throws an exception" in {
        mockUpdateJourneyData(
          journeyId = testJourneyId,
          dataKey = "dataKey",
          data = Json.toJson("dataToUpdate"),
          authId = testInternalId
        )(response = Future.failed(new MongoException("failed to update")))

        intercept[MongoException](
          await(TestService.storeKnownFactAnswer("dataToUpdate", "dataKey", testJourneyId, testInternalId))
        )
      }
    }
  }

}
