/*
 * Copyright 2021 HM Revenue & Customs
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

import play.api.libs.json.Json
import play.api.test.Helpers._
import reactivemongo.api.commands.UpdateWriteResult
import reactivemongo.core.errors.GenericDriverException
import uk.gov.hmrc.claimvatenrolmentfrontend.helpers.TestConstants.{testInternalId, testJourneyId, testLastMonthSubmitted}
import uk.gov.hmrc.claimvatenrolmentfrontend.repositories.mocks.MockJourneyDataRepository
import uk.gov.hmrc.claimvatenrolmentfrontend.utils.UnitSpec

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class StoreLastMonthSubmittedServiceSpec extends UnitSpec with MockJourneyDataRepository {

  object TestService extends StoreLastMonthSubmittedService(mockJourneyDataRepository)

  "storeLastMonthSubmitted" should {
    "successfully update the document in the database with the last month submitted" in {
      mockUpdateJourneyData(
        journeyId = testJourneyId,
        dataKey = "lastMonthSubmitted",
        data = Json.toJson(testLastMonthSubmitted.getValue),
        authId = testInternalId
      )(Future.successful(mock[UpdateWriteResult]))

      val result: Unit = await(TestService.storeLastMonthSubmitted(testJourneyId, testLastMonthSubmitted, testInternalId))

      result mustBe()

      verifyUpdateJourneyData(
        journeyId = testJourneyId,
        dataKey = "lastMonthSubmitted",
        data = Json.toJson(testLastMonthSubmitted.getValue),
        authId = testInternalId
      )
    }
    "throw an exception" when {
      "updating the document fails" in {
        mockUpdateJourneyData(
          journeyId = testJourneyId,
          dataKey = "lastMonthSubmitted",
          data = Json.toJson(testLastMonthSubmitted.getValue),
          authId = testInternalId
        )(response = Future.failed(GenericDriverException("failed to update")))

        intercept[GenericDriverException](
          await(TestService.storeLastMonthSubmitted(testJourneyId, testLastMonthSubmitted, testInternalId))
        )
        verifyUpdateJourneyData(
          journeyId = testJourneyId,
          dataKey = "lastMonthSubmitted",
          data = Json.toJson(testLastMonthSubmitted.getValue),
          authId = testInternalId
        )
      }
    }
  }

}
