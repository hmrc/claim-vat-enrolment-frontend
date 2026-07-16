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
import org.mockito.Mockito.when
import org.mockito.stubbing.OngoingStubbing
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.{Json, Writes}
import play.api.mvc.Results.{InternalServerError, Redirect}
import play.api.mvc.{Call, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.Html
import uk.gov.hmrc.claimvatenrolmentfrontend.config.ErrorHandler
import uk.gov.hmrc.claimvatenrolmentfrontend.helpers.TestConstants.{testInternalId, testJourneyId}
import uk.gov.hmrc.claimvatenrolmentfrontend.models.Postcode
import uk.gov.hmrc.claimvatenrolmentfrontend.repositories.mocks.MockJourneyDataRepository
import uk.gov.hmrc.play.bootstrap.tools.LogCapturing
import utils.{LinkLogger, LoggingUtil}

import java.time.{LocalDate, Month}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class StoreKnownFactsServiceSpec extends AnyWordSpec with Matchers with MockJourneyDataRepository with LoggingUtil with LogCapturing {

  private val mockErrorHandler: ErrorHandler = mock[ErrorHandler]

  object TestService extends StoreKnownFactsService(mockJourneyDataRepository, mockErrorHandler)

  private val continueOnSuccess: Result        = Redirect(Call("GET", "redirect-location"))
  private implicit val request: FakeRequest[_] = FakeRequest()

  "storeKnownFactAnswerOrHandleFailure" should {
    "call the JourneyDataRepository to update the data for a specific key, journeyId and authId" in {
      mockUpdateJourneyData(
        journeyId = testJourneyId,
        dataKey = "dataKey",
        data = Json.toJson("dataToUpdate"),
        authId = testInternalId
      )(Future.successful(true))

      await(
        TestService.storeKnownFactAnswerOrHandleFailure("dataToUpdate", "dataKey", testJourneyId, testInternalId)(
          Future.successful(continueOnSuccess)))

      verifyUpdateJourneyData(
        journeyId = testJourneyId,
        dataKey = "dataKey",
        data = Json.toJson("dataToUpdate"),
        authId = testInternalId
      )
    }

    def mockUpdateJourneyDataForValue[A](value: A)(implicit writes: Writes[A]): OngoingStubbing[_] =
      mockUpdateJourneyData(
        journeyId = testJourneyId,
        dataKey = "dataKey",
        data = Json.toJson(value),
        authId = testInternalId
      )(Future.successful(true))

    def runTestForDataValue[A](value: A)(implicit writes: Writes[A]): Future[Result] =
      TestService.storeKnownFactAnswerOrHandleFailure(value, "dataKey", testJourneyId, testInternalId)(Future.successful(continueOnSuccess))

    "return the 'continueOnSuccess' result" when {
      "the repository returns 'true', indicating it has updated the journey data" when {
        "given a String value" in {
          mockUpdateJourneyDataForValue("dataToUpdate")

          val result = await(runTestForDataValue("dataToUpdate"))

          result mustBe continueOnSuccess
        }

        "given a Boolean value" in {
          mockUpdateJourneyDataForValue(true)

          val result = await(runTestForDataValue(true))

          result mustBe continueOnSuccess
        }

        "given a Postcode object to parse" in {
          val dataToParseAndStore: Postcode = Postcode("AA1 1AA")

          mockUpdateJourneyDataForValue(Json.toJson("AA1 1AA"))

          val result = await(runTestForDataValue(dataToParseAndStore))

          result mustBe continueOnSuccess
        }

        "given a Month's Int value to parse" in {
          val dataToParseAndStore: Int = Month.JULY.getValue

          mockUpdateJourneyDataForValue(Json.toJson(7))

          val result = await(runTestForDataValue(dataToParseAndStore))

          result mustBe continueOnSuccess
        }

        "given a LocalDate object to parse" in {
          val dataToParseAndStore: LocalDate = LocalDate.of(2000, 2, 2)

          mockUpdateJourneyDataForValue(Json.toJson("2000-02-02"))

          val result = await(runTestForDataValue(dataToParseAndStore))

          result mustBe continueOnSuccess
        }
      }
    }

    "return an InternalServerError to the error page and log error reason" when {
      "the repository returns 'false', indicating it has failed to update the journey data" in {
        mockUpdateJourneyData(
          journeyId = testJourneyId,
          dataKey = "dataKey",
          data = Json.toJson("dataToUpdate"),
          authId = testInternalId
        )(Future.successful(false))
        when(mockErrorHandler.internalServerErrorTemplate)
          .thenReturn(Html("error"))

        val expectedErrorLog = s"[StoreKnownFactsService] - Unable to store user's answer (dataToUpdate) for dataKey page. Journey ID: $testJourneyId"

        withCaptureOfLoggingFrom(LinkLogger) { logs =>
          val result = await(runTestForDataValue("dataToUpdate"))

          result mustBe InternalServerError(mockErrorHandler.internalServerErrorTemplate)

          logs.exists(_.getMessage.contains(expectedErrorLog)) mustBe true

        }
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
          await(runTestForDataValue("dataToUpdate"))
        )
      }
    }
  }

}
