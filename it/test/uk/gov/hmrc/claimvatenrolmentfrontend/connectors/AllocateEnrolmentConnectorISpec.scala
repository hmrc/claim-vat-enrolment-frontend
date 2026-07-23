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

package uk.gov.hmrc.claimvatenrolmentfrontend.connectors

import play.api.http.Status.BAD_REQUEST
import play.api.libs.json.Json
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.claimvatenrolmentfrontend.assets.TestConstants._
import uk.gov.hmrc.claimvatenrolmentfrontend.featureswitch.core.config.{AllocateEnrolmentStub, FeatureSwitching}
import uk.gov.hmrc.claimvatenrolmentfrontend.models._
import uk.gov.hmrc.claimvatenrolmentfrontend.stubs.AllocationEnrolmentStub
import uk.gov.hmrc.claimvatenrolmentfrontend.utils.ComponentSpecHelper
import uk.gov.hmrc.claimvatenrolmentfrontend.utils.WiremockHelper.{verifyNoPost, verifyPost}
import uk.gov.hmrc.http.HeaderCarrier

import java.time.LocalDate

class AllocateEnrolmentConnectorISpec extends ComponentSpecHelper with AllocationEnrolmentStub with FeatureSwitching {

  private lazy val allocateEnrolmentConnector = app.injector.instanceOf[AllocateEnrolmentConnector]

  private implicit val headerCarrier: HeaderCarrier = HeaderCarrier()

  private implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()

  val fullKnownFactsAnswers: VatKnownFacts = VatKnownFacts(
    testVatNumber,
    Some(testPostcode),
    Some(LocalDate.of(2021, 1, 1)),
    Some(ReturnsInformation(Some(testBoxFive), Some(testLastReturnMonth))),
    Some(testFormBundleReference)
  )

  private val allocateEnrolmentUrl = s"/tax-enrolments/groups/$testGroupId/enrolments/HMRC-MTD-VAT~VRN~$testVatNumber"
  private val stubbedUrl = s"/claim-vat-enrolment/test-only/groups/$testGroupId/enrolments/HMRC-MTD-VAT~VRN~$testVatNumber"

  "allocateEnrolment" should {
    "call the stubbed Allocate Enrolment endpoint" when {
      "'AllocateEnrolmentStub' switch is ON" in {
        enable(AllocateEnrolmentStub)
        mockAllocateEnrolmentForStub(fullKnownFactsAnswers, testCredentialId, testGroupId)(CREATED, Json.obj())

        await(allocateEnrolmentConnector.allocateEnrolment(fullKnownFactsAnswers, testCredentialId, testGroupId))
        verifyPost(stubbedUrl)
        verifyNoPost(allocateEnrolmentUrl)
      }
    }

    "call the real Allocate Enrolment endpoint" when {
      "'AllocateEnrolmentStub' switch is OFF" in {
        disable(AllocateEnrolmentStub)
        mockAllocateEnrolment(fullKnownFactsAnswers, testCredentialId, includeFormBundleReference = true, testGroupId)(CREATED, Json.obj())

        await(allocateEnrolmentConnector.allocateEnrolment(fullKnownFactsAnswers, testCredentialId, testGroupId))
        verifyPost(allocateEnrolmentUrl)
        verifyNoPost(stubbedUrl)
      }
    }

    "return EnrolmentSuccess" when {
      "the Tax Enrolments service returns a 201 status" when {
        "all fields are populated in submission body" in {
          mockAllocateEnrolment(fullKnownFactsAnswers, testCredentialId, includeFormBundleReference = true, testGroupId)(CREATED, Json.obj())

          val result = await(allocateEnrolmentConnector.allocateEnrolment(fullKnownFactsAnswers, testCredentialId, testGroupId))

          result mustBe EnrolmentSuccess
        }

        "the user is overseas and so has not provided a postcode in submission body" in {
          val testVatKnownFactsNoPostcode = fullKnownFactsAnswers.copy(optPostcode = None)
          mockAllocateEnrolment(testVatKnownFactsNoPostcode, testCredentialId, includeFormBundleReference = true, testGroupId)(CREATED, Json.obj())

          val result = await(allocateEnrolmentConnector.allocateEnrolment(testVatKnownFactsNoPostcode, testCredentialId, testGroupId))

          result mustBe EnrolmentSuccess
        }

        "the user has not yet filed and so has not provided any returns information in submission body" in {
          val testVatKnownFactsNoReturnsInformation = fullKnownFactsAnswers.copy(optReturnsInformation = None)

          mockAllocateEnrolment(testVatKnownFactsNoReturnsInformation, testCredentialId, includeFormBundleReference = true, testGroupId)(
            CREATED,
            Json.obj())

          val result = await(allocateEnrolmentConnector.allocateEnrolment(testVatKnownFactsNoReturnsInformation, testCredentialId, testGroupId))

          result mustBe EnrolmentSuccess
        }

        "the user is both overseas, and has not yet filed" in {
          val testVatKnownFactsNoReturnsInformationOrPostcode = fullKnownFactsAnswers.copy(optReturnsInformation = None, optPostcode = None)

          mockAllocateEnrolment(testVatKnownFactsNoReturnsInformationOrPostcode, testCredentialId, includeFormBundleReference = true, testGroupId)(
            CREATED,
            Json.obj())

          val result =
            await(allocateEnrolmentConnector.allocateEnrolment(testVatKnownFactsNoReturnsInformationOrPostcode, testCredentialId, testGroupId))

          result mustBe EnrolmentSuccess
        }
      }
    }

    "return MultipleEnrolmentsInvalid" when {
      "the Tax Enrolments service returns a 409 with 'MULTIPLE_ENROLMENTS_INVALID' in the response body" in {
        val multipleEnrolmentsError = Json.obj("code" -> "MULTIPLE_ENROLMENTS_INVALID")
        mockAllocateEnrolment(fullKnownFactsAnswers, testCredentialId, includeFormBundleReference = true, testGroupId)(
          CONFLICT,
          multipleEnrolmentsError)

        val result = await(allocateEnrolmentConnector.allocateEnrolment(fullKnownFactsAnswers, testCredentialId, testGroupId))

        result mustBe MultipleEnrolmentsInvalid
      }
    }

    "return IncorrectKnownFacts" when {
      "the Tax Enrolments service returns a 400 with 'INVALID_IDENTIFIERS' in the response body" in {
        val incorrectKnownFacts = Json.obj("code" -> "INVALID_IDENTIFIERS", "message" -> "The enrolment identifiers provided were invalid")
        mockAllocateEnrolment(fullKnownFactsAnswers, testCredentialId, includeFormBundleReference = true, testGroupId)(
          BAD_REQUEST,
          incorrectKnownFacts)

        val result = await(allocateEnrolmentConnector.allocateEnrolment(fullKnownFactsAnswers, testCredentialId, testGroupId))

        result mustBe IncorrectKnownFacts
      }
    }

    "return EnrolmentFailure with the response reason" when {
      "the Tax Enrolments service returns a 400, WITHOUT an 'INVALID_IDENTIFIERS' in the response body" in {
        val otherError = Json.obj("code" -> "Some Other Error", "message" -> "Error Message")
        mockAllocateEnrolment(fullKnownFactsAnswers, testCredentialId, includeFormBundleReference = true, testGroupId)(BAD_REQUEST, otherError)

        val result = await(allocateEnrolmentConnector.allocateEnrolment(fullKnownFactsAnswers, testCredentialId, testGroupId))

        result mustBe EnrolmentFailure(otherError.toString())
      }

      "the Tax Enrolments service returns a 409, WITHOUT a 'MULTIPLE_ENROLMENTS_INVALID' in the response body" in {
        val otherError = Json.obj("code" -> "Some Other Error", "message" -> "Error Message")
        mockAllocateEnrolment(fullKnownFactsAnswers, testCredentialId, includeFormBundleReference = true, testGroupId)(CONFLICT, otherError)

        val result = await(allocateEnrolmentConnector.allocateEnrolment(fullKnownFactsAnswers, testCredentialId, testGroupId))

        result mustBe EnrolmentFailure(otherError.toString())
      }

      "the Tax Enrolments service returns another error status" in {
        val otherError = Json.obj("code" -> "Some Other Error", "message" -> "Error Message")
        mockAllocateEnrolment(fullKnownFactsAnswers, testCredentialId, includeFormBundleReference = true, testGroupId)(
          INTERNAL_SERVER_ERROR,
          otherError)

        val result = await(allocateEnrolmentConnector.allocateEnrolment(fullKnownFactsAnswers, testCredentialId, testGroupId))

        result mustBe EnrolmentFailure(otherError.toString())
      }
    }
  }

}
