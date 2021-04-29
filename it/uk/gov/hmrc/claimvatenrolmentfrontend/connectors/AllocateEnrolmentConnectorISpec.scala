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

package uk.gov.hmrc.claimvatenrolmentfrontend.connectors

import play.api.libs.json.Json
import play.api.test.Helpers._
import uk.gov.hmrc.claimvatenrolmentfrontend.assets.TestConstants._
import uk.gov.hmrc.claimvatenrolmentfrontend.models.{ClaimVatEnrolmentModel, EnrolmentSuccess, InvalidKnownFacts, ReturnsInformationModel, _}
import uk.gov.hmrc.claimvatenrolmentfrontend.stubs.AllocationEnrolmentStub
import uk.gov.hmrc.claimvatenrolmentfrontend.utils.ComponentSpecHelper
import uk.gov.hmrc.http.HeaderCarrier

import java.time.LocalDate

class AllocateEnrolmentConnectorISpec extends ComponentSpecHelper with AllocationEnrolmentStub {

  private lazy val allocateEnrolmentConnector = app.injector.instanceOf[AllocateEnrolmentConnector]

  private implicit val headerCarrier: HeaderCarrier = HeaderCarrier()

  val testClaimVatEnrolmentModel: ClaimVatEnrolmentModel = ClaimVatEnrolmentModel(testVatNumber, Some(testPostcode), LocalDate.of(2021, 1, 1), Some(ReturnsInformationModel(testBoxFive, testLastReturnMonth)))


  "allocateEnrolment" should {
    "return EnrolmentSuccess" when {
      "enrolment store returns a success" when {
        "all fields are populated" in {
          stubAllocateEnrolment(testClaimVatEnrolmentModel, testCredentialId, testGroupId)(CREATED, Json.obj())

          val result = await(allocateEnrolmentConnector.allocateEnrolment(testClaimVatEnrolmentModel, testCredentialId, testGroupId))

          result mustBe EnrolmentSuccess
        }
        "the user is overseas and so has not provided a postcode" in {
          val testClaimVatEnrolmentModelNoPostcode = testClaimVatEnrolmentModel.copy(optPostcode = None)

          stubAllocateEnrolment(testClaimVatEnrolmentModelNoPostcode, testCredentialId, testGroupId)(CREATED, Json.obj())

          val result = await(allocateEnrolmentConnector.allocateEnrolment(testClaimVatEnrolmentModelNoPostcode, testCredentialId, testGroupId))

          result mustBe EnrolmentSuccess
        }
        "the user has not yet filed and so has not provided any returns information" in {
          val testClaimVatEnrolmentModelNoReturnsInformation = testClaimVatEnrolmentModel.copy(optReturnsInformation = None)

          stubAllocateEnrolment(testClaimVatEnrolmentModelNoReturnsInformation, testCredentialId, testGroupId)(CREATED, Json.obj())

          val result = await(allocateEnrolmentConnector.allocateEnrolment(testClaimVatEnrolmentModelNoReturnsInformation, testCredentialId, testGroupId))

          result mustBe EnrolmentSuccess
        }
        "the user is both overseas, and has not yet filed" in {
          val testClaimVatEnrolmentModelNoReturnsInformationOrPostcode = testClaimVatEnrolmentModel.copy(optReturnsInformation = None, optPostcode = None)

          stubAllocateEnrolment(testClaimVatEnrolmentModelNoReturnsInformationOrPostcode, testCredentialId, testGroupId)(CREATED, Json.obj())

          val result = await(allocateEnrolmentConnector.allocateEnrolment(testClaimVatEnrolmentModelNoReturnsInformationOrPostcode, testCredentialId, testGroupId))

          result mustBe EnrolmentSuccess
        }
      }
    }


    "return Multiple Enrolments Invalid" when {
      "tax enrolments returns a single error indicating multiple enrolments" in {
        stubAllocateEnrolment(testClaimVatEnrolmentModel, testCredentialId, testGroupId)(CONFLICT, Json.obj("code" -> "MULTIPLE_ENROLMENTS_INVALID"))

        val result = await(allocateEnrolmentConnector.allocateEnrolment(testClaimVatEnrolmentModel, testCredentialId, testGroupId))

        result mustBe MultipleEnrolmentsInvalid
      }
      "tax enrolments returns multiple errors including an error indicating multiple enrolments" in {
        stubAllocateEnrolment(testClaimVatEnrolmentModel, testCredentialId, testGroupId)(
          status = CONFLICT,
          jsonBody = Json.obj(
            "code" -> "MULTIPLE_ERRORS",
            "message" -> "Multiple errors have occurred",
            "errors" -> Json.arr(
              Json.obj(
                "code" -> "MULTIPLE_ENROLMENTS_INVALID",
                "message" -> "Multiple Enrolments are not valid for this service"
              ),
              Json.obj(
                "code" -> "OTHER_CODE",
                "message" -> "Other message"
              )
            )
          ))

        val result = await(allocateEnrolmentConnector.allocateEnrolment(testClaimVatEnrolmentModel, testCredentialId, testGroupId))

        result mustBe MultipleEnrolmentsInvalid
      }
    }

    "return EnrolmentFailure" in {
      stubAllocateEnrolment(testClaimVatEnrolmentModel, testCredentialId, testGroupId)(BAD_REQUEST, Json.obj())

      val result = await(allocateEnrolmentConnector.allocateEnrolment(testClaimVatEnrolmentModel, testCredentialId, testGroupId))

      result mustBe InvalidKnownFacts
    }
  }

}
