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

package uk.gov.hmrc.claimvatenrolmentfrontend.utils.services

import play.api.libs.json.Json
import play.api.test.Helpers._
import reactivemongo.api.commands.UpdateWriteResult
import reactivemongo.core.errors.GenericDriverException
import uk.gov.hmrc.claimvatenrolmentfrontend.services.StoreBusinessPostcodeService
import uk.gov.hmrc.claimvatenrolmentfrontend.utils.UnitSpec
import uk.gov.hmrc.claimvatenrolmentfrontend.utils.helpers.TestConstants.{testInternalId, testJourneyId, testPostcode}
import uk.gov.hmrc.claimvatenrolmentfrontend.utils.repositories.mocks.MockJourneyDataRepository

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class StoreVatRegistrationPostcodeServiceSpec extends UnitSpec with MockJourneyDataRepository {

  object TestService extends StoreBusinessPostcodeService(mockJourneyDataRepository)

  "storeVatRegistrationDate" should {
    "successfully update the document in the database with the vat postcode" in {
      mockUpdateJourneyData(
        journeyId = testJourneyId,
        dataKey = "vatRegPostcode",
        data = Json.toJson(testPostcode.sanitisedPostcode),
        authId = testInternalId
      )(Future.successful(mock[UpdateWriteResult]))

      val result: Unit = await(TestService.storeBusinessPostcodeService(testJourneyId, testPostcode, testInternalId))

      result mustBe()

      verifyUpdateJourneyData(
        journeyId = testJourneyId,
        dataKey = "vatRegPostcode",
        data = Json.toJson(testPostcode.sanitisedPostcode),
        authId = testInternalId
      )
    }

    "throw an exception" when {
      "updating the document fails" in {
        mockUpdateJourneyData(
          journeyId = testJourneyId,
          dataKey = "vatRegPostcode",
          data = Json.toJson(testPostcode.stringValue),
          authId = testInternalId
        )(response = Future.failed(GenericDriverException("failed to update")))

        intercept[GenericDriverException](
          await(TestService.storeBusinessPostcodeService(testJourneyId, testPostcode, testInternalId))
        )
        verifyUpdateJourneyData(
          journeyId = testJourneyId,
          dataKey = "vatRegPostcode",
          data = Json.toJson(testPostcode.sanitisedPostcode),
          authId = testInternalId
        )
      }
    }
  }

}
