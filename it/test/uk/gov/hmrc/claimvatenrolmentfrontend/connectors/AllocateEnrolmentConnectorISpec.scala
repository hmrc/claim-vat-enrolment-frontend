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

package uk.gov.hmrc.claimvatenrolmentfrontend.connectors

import play.api.libs.json.Json
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.claimvatenrolmentfrontend.assets.TestConstants._
import uk.gov.hmrc.claimvatenrolmentfrontend.featureswitch.core.config.{AllocateEnrolmentStub, FeatureSwitching}
import uk.gov.hmrc.claimvatenrolmentfrontend.models._
import uk.gov.hmrc.claimvatenrolmentfrontend.stubs.AllocationEnrolmentStub
import uk.gov.hmrc.claimvatenrolmentfrontend.utils.ComponentSpecHelper
import uk.gov.hmrc.http.HeaderCarrier

import java.time.LocalDate

class AllocateEnrolmentConnectorISpec extends ComponentSpecHelper with AllocationEnrolmentStub with FeatureSwitching {

  private lazy val allocateEnrolmentConnector = app.injector.instanceOf[AllocateEnrolmentConnector]

  private implicit val headerCarrier: HeaderCarrier = HeaderCarrier()

  private implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()

  val testVatKnownFacts: VatKnownFacts = VatKnownFacts(testVatNumber, Some(testPostcode), Some(LocalDate.of(2021, 1, 1)),
    Some(ReturnsInformation(testBoxFive, testLastReturnMonth)), Some(testFormBundleReference))

  "allocateEnrolment" should {
    "return EnrolmentSuccess" when {
      "enrolment store returns a success" when {
        "the stub Allocate Enrolment feature switch is disable" when {
          "all fields are populated" in {
            stubAllocateEnrolment(testVatKnownFacts, testCredentialId, includeFormBundleReference = true, testGroupId)(CREATED, Json.obj())

            val result = await(allocateEnrolmentConnector.allocateEnrolment(testVatKnownFacts, testCredentialId, testGroupId))

            result mustBe EnrolmentSuccess
          }
          "the user is overseas and so has not provided a postcode" in {
            val testVatKnownFactsNoPostcode = testVatKnownFacts.copy(optPostcode = None)
            stubAllocateEnrolment(testVatKnownFactsNoPostcode, testCredentialId, includeFormBundleReference = true, testGroupId)(CREATED, Json.obj())

            val result = await(allocateEnrolmentConnector.allocateEnrolment(testVatKnownFactsNoPostcode, testCredentialId, testGroupId))

            result mustBe EnrolmentSuccess
          }
          "the user has not yet filed and so has not provided any returns information" in {
            val testVatKnownFactsNoReturnsInformation = testVatKnownFacts.copy(optReturnsInformation = None)

            stubAllocateEnrolment(testVatKnownFactsNoReturnsInformation, testCredentialId, includeFormBundleReference = true, testGroupId)(CREATED, Json.obj())

            val result = await(allocateEnrolmentConnector.allocateEnrolment(testVatKnownFactsNoReturnsInformation, testCredentialId, testGroupId))

            result mustBe EnrolmentSuccess
          }
          "the user is both overseas, and has not yet filed" in {
            val testVatKnownFactsNoReturnsInformationOrPostcode = testVatKnownFacts.copy(optReturnsInformation = None, optPostcode = None)

            stubAllocateEnrolment(testVatKnownFactsNoReturnsInformationOrPostcode, testCredentialId, includeFormBundleReference = true, testGroupId)(CREATED, Json.obj())

            val result = await(allocateEnrolmentConnector.allocateEnrolment(testVatKnownFactsNoReturnsInformationOrPostcode, testCredentialId, testGroupId))

            result mustBe EnrolmentSuccess
          }
        }

        "the stub Allocate Enrolment feature switch is enable" when {
          "all fields are populated" in {
            enable(AllocateEnrolmentStub)
            stubAllocateEnrolmentForStub(testVatKnownFacts, testCredentialId, testGroupId)(CREATED, Json.obj())

            val result = await(allocateEnrolmentConnector.allocateEnrolment(testVatKnownFacts, testCredentialId, testGroupId))

            result mustBe EnrolmentSuccess
          }
          "the user is overseas and so has not provided a postcode" in {
            enable(AllocateEnrolmentStub)
            val testVatKnownFactsNoPostcode = testVatKnownFacts.copy(optPostcode = None)
            stubAllocateEnrolmentForStub(testVatKnownFactsNoPostcode, testCredentialId, testGroupId)(CREATED, Json.obj())

            val result = await(allocateEnrolmentConnector.allocateEnrolment(testVatKnownFactsNoPostcode, testCredentialId, testGroupId))

            result mustBe EnrolmentSuccess
          }
          "the user has not yet filed and so has not provided any returns information" in {
            enable(AllocateEnrolmentStub)
            val testVatKnownFactsNoReturnsInformation = testVatKnownFacts.copy(optReturnsInformation = None)

            stubAllocateEnrolmentForStub(testVatKnownFactsNoReturnsInformation, testCredentialId, testGroupId)(CREATED, Json.obj())

            val result = await(allocateEnrolmentConnector.allocateEnrolment(testVatKnownFactsNoReturnsInformation, testCredentialId, testGroupId))

            result mustBe EnrolmentSuccess
          }
          "the user is both overseas, and has not yet filed" in {
            enable(AllocateEnrolmentStub)
            val testVatKnownFactsNoReturnsInformationOrPostcode = testVatKnownFacts.copy(optReturnsInformation = None, optPostcode = None)

            stubAllocateEnrolmentForStub(testVatKnownFactsNoReturnsInformationOrPostcode, testCredentialId, testGroupId)(CREATED, Json.obj())

            val result = await(allocateEnrolmentConnector.allocateEnrolment(testVatKnownFactsNoReturnsInformationOrPostcode, testCredentialId, testGroupId))

            result mustBe EnrolmentSuccess
          }
        }
      }
    }


    "return Multiple Enrolments Invalid" when {
      "the stub Allocate Enrolment feature switch and QueryUserIdStub are disable " when {
        "tax enrolments returns a single error indicating multiple enrolments" in {
          stubAllocateEnrolment(testVatKnownFacts, testCredentialId, includeFormBundleReference = true, testGroupId)(CONFLICT, Json.obj("code" -> "MULTIPLE_ENROLMENTS_INVALID"))

          val result = await(allocateEnrolmentConnector.allocateEnrolment(testVatKnownFacts, testCredentialId, testGroupId))

          result mustBe MultipleEnrolmentsInvalid
        }
        "tax enrolments returns multiple errors including an error indicating multiple enrolments" in {
          stubAllocateEnrolment(testVatKnownFacts, testCredentialId, includeFormBundleReference = true, testGroupId)(
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

          val result = await(allocateEnrolmentConnector.allocateEnrolment(testVatKnownFacts, testCredentialId, testGroupId))

          result mustBe MultipleEnrolmentsInvalid
        }
      }


      "the stub Allocate Enrolment feature switch is enable " when {
        "tax enrolments returns a single error indicating multiple enrolments" in {
          enable(AllocateEnrolmentStub)
          stubAllocateEnrolmentForStub(testVatKnownFacts, testCredentialId, testGroupId)(CONFLICT, Json.obj("code" -> "MULTIPLE_ENROLMENTS_INVALID"))

          val result = await(allocateEnrolmentConnector.allocateEnrolment(testVatKnownFacts, testCredentialId, testGroupId))

          result mustBe MultipleEnrolmentsInvalid
        }
        "tax enrolments returns multiple errors including an error indicating multiple enrolments" in {
          enable(AllocateEnrolmentStub)
          stubAllocateEnrolmentForStub(testVatKnownFacts, testCredentialId, testGroupId)(
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

          val result = await(allocateEnrolmentConnector.allocateEnrolment(testVatKnownFacts, testCredentialId, testGroupId))

          result mustBe MultipleEnrolmentsInvalid
        }

        "return EnrolmentFailure" in {
          stubAllocateEnrolment(testVatKnownFacts, testCredentialId, includeFormBundleReference = true, testGroupId)(BAD_REQUEST, Json.obj())

          val result = await(allocateEnrolmentConnector.allocateEnrolment(testVatKnownFacts, testCredentialId, testGroupId))

          result mustBe InvalidKnownFacts
        }
      }
    }
  }
}
