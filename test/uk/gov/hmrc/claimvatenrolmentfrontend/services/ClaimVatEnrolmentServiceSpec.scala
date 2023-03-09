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
import play.api.mvc.{AnyContent, Request}
import play.api.test.FakeRequest
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import uk.gov.hmrc.claimvatenrolmentfrontend.connectors.AllocateEnrolmentConnector.etmpDateFormat
import uk.gov.hmrc.claimvatenrolmentfrontend.connectors.mocks.MockAuditConnector
import uk.gov.hmrc.claimvatenrolmentfrontend.helpers.TestConstants._
import uk.gov.hmrc.claimvatenrolmentfrontend.httpparsers.QueryUsersHttpParser.{NoUsersFound, UsersFound}
import uk.gov.hmrc.claimvatenrolmentfrontend.models._
import uk.gov.hmrc.claimvatenrolmentfrontend.services.ClaimVatEnrolmentService.{CannotAssignMultipleMtdvatEnrolments, EnrolmentAlreadyAllocated, JourneyConfigFailure, JourneyDataFailure, KnownFactsMismatch}
import uk.gov.hmrc.claimvatenrolmentfrontend.services.mocks.{MockAllocateEnrolmentService, MockJourneyService}
import uk.gov.hmrc.http.{HeaderCarrier, InternalServerException}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ClaimVatEnrolmentServiceSpec extends AnyWordSpec with Matchers with MockJourneyService with MockAllocateEnrolmentService with MockAuditConnector {

  implicit val hc: HeaderCarrier = HeaderCarrier()

  implicit val request: Request[AnyContent] = FakeRequest()
  implicit val responseVatKnownFacts: Future[Option[VatKnownFacts]] = Future.successful(testFullVatKnownFacts)
  implicit val responseJourneyConfig: Future[Option[JourneyConfig]] = Future.successful(testJourneyConfig)

  object TestService extends ClaimVatEnrolmentService(mockAuditConnector, mockAllocateEnrolmentService, mockJourneyService)

  def testAuditDetails(vatKnownFacts: VatKnownFacts,
                       isSuccessful: Boolean,
                       optFailureMessage: Option[String] = None
                      ): Map[String, String] = Map(
    "vatNumber" -> vatKnownFacts.vatNumber,
    "businessPostcode" -> vatKnownFacts.optPostcode.map(_.sanitisedPostcode).getOrElse(""),
    "vatRegistrationDate" -> vatKnownFacts.vatRegistrationDate.format(etmpDateFormat),
    "boxFiveAmount" -> vatKnownFacts.optReturnsInformation.map(_.boxFive).getOrElse(""),
    "latestMonthReturn" -> vatKnownFacts.optReturnsInformation.map(_.lastReturnMonth.getValue.formatted("%02d")).getOrElse(""),
    "vatSubscriptionClaimSuccessful" -> isSuccessful.toString,
    "enrolmentAndClientDatabaseFailureReason" -> optFailureMessage.getOrElse("")
  ).filter { case (_, value) => value.nonEmpty }

  "claimVatEnrolment" should {
    "return a Right(continueUrl)" when {
      "the enrolment is successfully claimed" in {
        mockRetrieveJourneyData(testJourneyId, testInternalId)
        mockAllocateEnrolment(testFullVatKnownFacts.get, testCredentialId, testGroupId)(Future.successful(EnrolmentSuccess))
        mockRetrieveJourneyConfig(testJourneyId, testInternalId)

        val result = await(TestService.claimVatEnrolment(testCredentialId, testGroupId, testInternalId, testJourneyId))

        result mustBe Right(testContinueUrl)
        verifyAuditEvent
        auditEventCaptor.getValue.detail mustBe testAuditDetails(testFullVatKnownFacts.get, isSuccessful = true)
      }
    }

    "return a Left(KnownFactsMismatch)" when {
      "the enrolment cannot be claimed due to invalid known facts" in {
        mockRetrieveJourneyData(testJourneyId, testInternalId)
        mockAllocateEnrolment(testFullVatKnownFacts.get, testCredentialId, testGroupId)(Future.successful(InvalidKnownFacts))
        mockGetUserIds(testVatNumber)(Future.successful(NoUsersFound))

        val result = await(TestService.claimVatEnrolment(testCredentialId, testGroupId, testInternalId, testJourneyId))

        result mustBe Left(KnownFactsMismatch)
        verifyAuditEvent
        auditEventCaptor.getValue.detail mustBe testAuditDetails(testFullVatKnownFacts.get, isSuccessful = false, Some(InvalidKnownFacts.message))
      }
    }

    "return a Left(EnrolmentAlreadyAllocated)" when {
      "the enrolment cannot be claimed due to invalid known facts but enrolment store proxy returns existing user" in {
        mockRetrieveJourneyData(testJourneyId, testInternalId)
        mockAllocateEnrolment(testFullVatKnownFacts.get, testCredentialId, testGroupId)(Future.successful(InvalidKnownFacts))
        mockGetUserIds(testVatNumber)(Future.successful(UsersFound))

        val result = await(TestService.claimVatEnrolment(testCredentialId, testGroupId, testInternalId, testJourneyId))

        result mustBe Left(EnrolmentAlreadyAllocated)
        verifyAuditEvent
        auditEventCaptor.getValue.detail mustBe testAuditDetails(testFullVatKnownFacts.get, isSuccessful = false, Some(UsersFound.message))
      }
    }

    "return a Left(CannotAssignMultipleMtdvatEnrolments)" when {
      "the user already has an MTDVAT enrolment on their credential" in {
        mockRetrieveJourneyData(testJourneyId, testInternalId)
        mockAllocateEnrolment(testFullVatKnownFacts.get, testCredentialId, testGroupId)(Future.successful(MultipleEnrolmentsInvalid))

        val result = await(TestService.claimVatEnrolment(testCredentialId, testGroupId, testInternalId, testJourneyId))

        result mustBe Left(CannotAssignMultipleMtdvatEnrolments)
        verifyAuditEvent
        auditEventCaptor.getValue.detail mustBe testAuditDetails(testFullVatKnownFacts.get, isSuccessful = false, Some(MultipleEnrolmentsInvalid.message))
      }
    }

    "return a Left(EnrolmentAlreadyAllocated)" when {
      "the enrolment is already allocated to a different credential" in {
        mockRetrieveJourneyData(testJourneyId, testInternalId)
        mockAllocateEnrolment(testFullVatKnownFacts.get, testCredentialId, testGroupId)(Future.successful(EnrolmentFailure("Failure")))
        mockGetUserIds(testVatNumber)(Future.successful(UsersFound))

        val result = await(TestService.claimVatEnrolment(testCredentialId, testGroupId, testInternalId, testJourneyId))

        result mustBe Left(EnrolmentAlreadyAllocated)
        verifyAuditEvent
        auditEventCaptor.getValue.detail mustBe testAuditDetails(testFullVatKnownFacts.get, isSuccessful = false, Some(UsersFound.message))
      }
    }

    "return a Left(JourneyConfigFailure)" when {
      "the enrolment is already allocated to a different credential" in {
        mockRetrieveJourneyData(testJourneyId, testInternalId)
        mockAllocateEnrolment(testFullVatKnownFacts.get, testCredentialId, testGroupId)(Future.successful(EnrolmentSuccess))
        mockFailRetrieveJourneyConfig(testJourneyId, testInternalId)

        val result = await(TestService.claimVatEnrolment(testCredentialId, testGroupId, testInternalId, testJourneyId))

        result mustBe Left(JourneyConfigFailure)
      }
    }

    "return a Left(JourneyDataFailure)" when {
      "the enrolment is already allocated to a different credential" in {
        mockFailRetrieveJourneyData(testJourneyId, testInternalId)

        val result = await(TestService.claimVatEnrolment(testCredentialId, testGroupId, testInternalId, testJourneyId))

        result mustBe Left(JourneyDataFailure)
      }
    }

    "throw an exception" when {
      "the enrolment cannot be claimed but is not allocated to another user" in {
        mockRetrieveJourneyData(testJourneyId, testInternalId)
        mockAllocateEnrolment(testFullVatKnownFacts.get, testCredentialId, testGroupId)(Future.successful(EnrolmentFailure("Failure")))
        mockGetUserIds(testVatNumber)(Future.successful(NoUsersFound))

        intercept[InternalServerException] {
          await(TestService.claimVatEnrolment(testCredentialId, testGroupId, testInternalId, testJourneyId))
          verifyAuditEvent
          auditEventCaptor.getValue.detail mustBe testAuditDetails(testFullVatKnownFacts.get, isSuccessful = false, Some(NoUsersFound.message))
        }

      }
    }
  }

}
