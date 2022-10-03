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

package uk.gov.hmrc.claimvatenrolmentfrontend.services

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.Json
import play.api.test.Helpers._
import com.mongodb.MongoException
import uk.gov.hmrc.claimvatenrolmentfrontend.helpers.TestConstants.{testInternalId, testJourneyId, testVatRegDate}
import uk.gov.hmrc.claimvatenrolmentfrontend.repositories.mocks.MockJourneyDataRepository

import scala.concurrent.Future

class StoreVatRegistrationDateServiceSpec extends AnyWordSpec with Matchers with MockJourneyDataRepository {

  object TestService extends StoreVatRegistrationDateService(mockJourneyDataRepository)

  "storeVatRegistrationDate" should {
    "successfully update the document in the database with the vat reg date" in {
      mockUpdateJourneyData(
        journeyId = testJourneyId,
        dataKey = "vatRegistrationDate",
        data = Json.toJson(testVatRegDate),
        authId = testInternalId
      )(Future.successful(true))

      val result: Boolean = await(TestService.storeVatRegistrationDate(testJourneyId, testVatRegDate, testInternalId))

      result mustBe true

      verifyUpdateJourneyData(
        journeyId = testJourneyId,
        dataKey = "vatRegistrationDate",
        data = Json.toJson(testVatRegDate),
        authId = testInternalId
      )
    }

    "throw an exception" when {
      "updating the document fails" in {
        mockUpdateJourneyData(
          journeyId = testJourneyId,
          dataKey = "vatRegistrationDate",
          data = Json.toJson(testVatRegDate),
          authId = testInternalId
        )(response = Future.failed(new MongoException("failed to update")))

        intercept[MongoException](
          await(TestService.storeVatRegistrationDate(testJourneyId, testVatRegDate, testInternalId))
        )
        verifyUpdateJourneyData(
          journeyId = testJourneyId,
          dataKey = "vatRegistrationDate",
          data = Json.toJson(testVatRegDate),
          authId = testInternalId
        )
      }
    }
  }

}
