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

package uk.gov.hmrc.claimvatenrolmentfrontend.services

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.Json
import play.api.test.Helpers._
import com.mongodb.MongoException
import uk.gov.hmrc.claimvatenrolmentfrontend.helpers.TestConstants.{testInternalId, testJourneyId, testVatReturn}
import uk.gov.hmrc.claimvatenrolmentfrontend.repositories.mocks.MockJourneyDataRepository
import uk.gov.hmrc.claimvatenrolmentfrontend.services.StoreSubmittedVatReturnService.SubmittedVatReturnKey

import scala.concurrent.Future


class StoreSubmittedVatReturnServiceSpec extends AnyWordSpec with Matchers with MockJourneyDataRepository {

  object TestReturnService$ extends StoreSubmittedVatReturnService(mockJourneyDataRepository)

  "storeSubmittedVatService" should {
    "successfully update the document in the database with the submitted vat answer" in {
      mockUpdateJourneyData(
        journeyId = testJourneyId,
        dataKey = SubmittedVatReturnKey,
        data = Json.toJson(testVatReturn),
        authId = testInternalId
      )(Future.successful(true))

      val result: Boolean = await(TestReturnService$.storeStoreSubmittedVat(testJourneyId, testVatReturn, testInternalId))

      result mustBe true

      verifyUpdateJourneyData(
        journeyId = testJourneyId,
        dataKey = SubmittedVatReturnKey,
        data = Json.toJson(testVatReturn),
        authId = testInternalId
      )
    }

    "throw an exception" when {
      "updating the document fails" in {
        mockUpdateJourneyData(
          journeyId = testJourneyId,
          dataKey = SubmittedVatReturnKey,
          data = Json.toJson(testVatReturn),
          authId = testInternalId
        )(response = Future.failed(new MongoException("failed to update")))

        intercept[MongoException](
          await(TestReturnService$.storeStoreSubmittedVat(testJourneyId, testVatReturn, testInternalId))
        )

        verifyUpdateJourneyData(
          journeyId = testJourneyId,
          dataKey = SubmittedVatReturnKey,
          data = Json.toJson(testVatReturn),
          authId = testInternalId
        )
      }
    }
  }

}

