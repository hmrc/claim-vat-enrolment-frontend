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

import com.mongodb.MongoException
import org.scalatest.matchers.must.Matchers
import org.scalatest.prop.TableDrivenPropertyChecks
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.Json
import play.api.test.Helpers._
import uk.gov.hmrc.claimvatenrolmentfrontend.helpers.TestConstants.{testFormBundleReference, testInternalId, testJourneyId}
import uk.gov.hmrc.claimvatenrolmentfrontend.models.VatApplicationNumber
import uk.gov.hmrc.claimvatenrolmentfrontend.repositories.mocks.MockJourneyDataRepository

import scala.concurrent.Future

class StoreKnownFactServiceSpec extends AnyWordSpec with Matchers with TableDrivenPropertyChecks with MockJourneyDataRepository {

  object TestService extends StoreKnownFactService(mockJourneyDataRepository)

  val dataKey = "dataKey"

  "storeKnownFact" should {
    "successfully update the document in the database with the given data" when {
      "valid data is given that matches the type" in {
        mockUpdateJourneyData(
          journeyId = testJourneyId,
          dataKey = dataKey,
          data = Json.toJson(testFormBundleReference),
          authId = testInternalId
        )(Future.successful(true))

        val result: Boolean = await(TestService.storeKnownFact[VatApplicationNumber](testJourneyId, testFormBundleReference, dataKey, testInternalId))

        result mustBe true

        verifyUpdateJourneyData(
          journeyId = testJourneyId,
          dataKey = dataKey,
          data = Json.toJson(testFormBundleReference),
          authId = testInternalId
        )
      }
    }
    "throw an exception" when {
      "updating the document fails" in {
        mockUpdateJourneyData(
          journeyId = testJourneyId,
          dataKey = dataKey,
          data = Json.toJson(testFormBundleReference),
          authId = testInternalId
        )(response = Future.failed(new MongoException("failed to update")))

        intercept[MongoException](
          await(TestService.storeKnownFact[VatApplicationNumber](testJourneyId, testFormBundleReference, dataKey, testInternalId))
        )

        verifyUpdateJourneyData(
          journeyId = testJourneyId,
          dataKey = dataKey,
          data = Json.toJson(testFormBundleReference),
          authId = testInternalId
        )
      }
    }
  }

}
