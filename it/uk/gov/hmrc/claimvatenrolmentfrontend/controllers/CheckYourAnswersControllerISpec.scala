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

package uk.gov.hmrc.claimvatenrolmentfrontend.controllers

import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.test.Helpers._
import reactivemongo.play.json._
import uk.gov.hmrc.claimvatenrolmentfrontend.assets.TestConstants._
import uk.gov.hmrc.claimvatenrolmentfrontend.controllers.errorPages.{routes => errorRoutes}
import uk.gov.hmrc.claimvatenrolmentfrontend.models.AllocateEnrolmentResponseHttpParser.MultipleEnrolmentsInvalidKey
import uk.gov.hmrc.claimvatenrolmentfrontend.repositories.JourneyDataRepository.vatKnownFactsWrites
import uk.gov.hmrc.claimvatenrolmentfrontend.stubs.{AllocationEnrolmentStub, AuthStub, EnrolmentStoreProxyStub}
import uk.gov.hmrc.claimvatenrolmentfrontend.utils.ComponentSpecHelper
import uk.gov.hmrc.claimvatenrolmentfrontend.utils.WiremockHelper._
import uk.gov.hmrc.claimvatenrolmentfrontend.views.CheckYourAnswersViewTests

import java.time.Instant
import scala.concurrent.ExecutionContext.Implicits.global


class CheckYourAnswersControllerISpec extends ComponentSpecHelper with CheckYourAnswersViewTests with AuthStub with AllocationEnrolmentStub with EnrolmentStoreProxyStub {

  def extraConfig = Map(
    "auditing.enabled" -> "true",
    "auditing.consumer.baseUri.host" -> mockHost,
    "auditing.consumer.baseUri.port" -> mockPort
  )

  override lazy val app: Application = new GuiceApplicationBuilder()
    .configure(config ++ extraConfig)
    .build

  override def beforeEach(): Unit = {
    await(journeyDataRepository.drop)
    super.beforeEach()
  }

  s"GET /$testJourneyId/check-your-answers-vat" when {
    "there is a full VatKnownFacts stored in the database" should {
      lazy val result = {
        await(journeyDataRepository.collection.insert(true).one(
          Json.obj(
            "_id" -> testJourneyId,
            "authInternalId" -> testInternalId,
            "creationTimestamp" -> Json.obj("$date" -> Instant.now.toEpochMilli)
          ) ++ Json.toJsObject(testFullVatKnownFacts)
        ))
        stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
        stubAudit
        get(s"/$testJourneyId/check-your-answers-vat")
      }
      "return OK" in {
        result.status mustBe OK
      }
      testCheckYourAnswersViewFull(result)
    }

    "there is a VatKnownFacts with no postcode stored in the database" should {
      lazy val result = {
        await(journeyDataRepository.collection.insert(true).one(
          Json.obj(
            "_id" -> testJourneyId,
            "authInternalId" -> testInternalId,
            "creationTimestamp" -> Json.obj("$date" -> Instant.now.toEpochMilli)
          ) ++ Json.toJsObject(testVatKnownFactsNoPostcode)
        ))
        stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
        stubAudit
        get(s"/$testJourneyId/check-your-answers-vat")
      }

      "return OK" in {
        result.status mustBe OK
      }

      testCheckYourAnswersViewNoPostcode(result)
    }

    "there is a VatKnownFacts with no returns stored in the database" should {
      lazy val result = {
        await(journeyDataRepository.collection.insert(true).one(
          Json.obj(
            "_id" -> testJourneyId,
            "authInternalId" -> testInternalId,
            "creationTimestamp" -> Json.obj("$date" -> Instant.now.toEpochMilli)
          ) ++ Json.toJsObject(testVatKnownFactsNoReturns)
        ))
        stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
        stubAudit
        get(s"/$testJourneyId/check-your-answers-vat")
      }

      "return OK" in {
        result.status mustBe OK
      }

      testCheckYourAnswersViewNoReturnsInformation(result)
    }

    "there is a VatKnownFacts with no returns and no postcode stored in the database" should {
      lazy val result = {
        await(journeyDataRepository.collection.insert(true).one(
          Json.obj(
            "_id" -> testJourneyId,
            "authInternalId" -> testInternalId,
            "creationTimestamp" -> Json.obj("$date" -> Instant.now.toEpochMilli)
          ) ++ Json.toJsObject(testVatKnownFactsNoReturnsNoPostcode)
        ))
        stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
        stubAudit
        get(s"/$testJourneyId/check-your-answers-vat")
      }

      "return OK" in {
        result.status mustBe OK
      }

      testCheckYourAnswersViewNoReturnsNoPostcode(result)
    }
  }

  s"POST /$testJourneyId/check-your-answers-vat" should {
    "redirect to the continue url when the allocation was successfully created" in {
      stubAuth(OK, successfulAuthResponse(Some(testGroupId), Some(testInternalId)))
      await(journeyDataRepository.collection.insert(true).one(
        Json.obj(
          "_id" -> testJourneyId,
          "authInternalId" -> testInternalId,
          "creationTimestamp" -> Json.obj("$date" -> Instant.now.toEpochMilli)
        ) ++ Json.toJsObject(testFullVatKnownFacts)
      ))
      await(insertJourneyConfig(testJourneyId, testContinueUrl))
      stubAllocateEnrolment(testFullVatKnownFacts, testCredentialId, testGroupId)(CREATED, Json.obj())
      stubAudit

      lazy val result = post(s"/$testJourneyId/check-your-answers-vat")()

      result must have(
        httpStatus(SEE_OTHER),
        redirectUri(testContinueUrl)
      )
      verifyAudit()
    }

    "redirect to UnmatchedUser if the user group already has a matching enrolment, but the user does not" in {
      stubAuth(OK, successfulAuthResponse(Some(testGroupId), Some(testInternalId)))
      await(journeyDataRepository.collection.insert(true).one(
        Json.obj(
          "_id" -> testJourneyId,
          "authInternalId" -> testInternalId,
          "creationTimestamp" -> Json.obj("$date" -> Instant.now.toEpochMilli)
        ) ++ Json.toJsObject(testFullVatKnownFacts)
      ))
      await(insertJourneyConfig(testJourneyId, testContinueUrl))
      stubAllocateEnrolment(testFullVatKnownFacts, testCredentialId, testGroupId)(CONFLICT, Json.obj("code" -> MultipleEnrolmentsInvalidKey))
      stubAudit

      lazy val result = post(s"/$testJourneyId/check-your-answers-vat")()

      result must have(
        httpStatus(SEE_OTHER),
        redirectUri(errorRoutes.UnmatchedUserErrorController.show().url)
      )
      verifyAudit()
    }

    "redirect to KnownFactsMismatch if the enrolment fails" in {
      stubAuth(OK, successfulAuthResponse(Some(testGroupId), Some(testInternalId)))
      await(journeyDataRepository.collection.insert(true).one(
        Json.obj(
          "_id" -> testJourneyId,
          "authInternalId" -> testInternalId,
          "creationTimestamp" -> Json.obj("$date" -> Instant.now.toEpochMilli)
        ) ++ Json.toJsObject(testFullVatKnownFacts)
      ))
      await(insertJourneyConfig(testJourneyId, testContinueUrl))
      stubAllocateEnrolment(testFullVatKnownFacts, testCredentialId, testGroupId)(BAD_REQUEST, Json.obj())
      stubAudit

      lazy val result = post(s"/$testJourneyId/check-your-answers-vat")()


      result must have(
        httpStatus(SEE_OTHER),
        redirectUri(errorRoutes.KnownFactsMismatchController.show().url)
      )
      verifyAudit()
    }

    "redirect to EnrolmentAlreadyAllocated error page when enrolment fails " in {
      stubAuth(OK, successfulAuthResponse(Some(testGroupId), Some(testInternalId)))
      await(journeyDataRepository.collection.insert(true).one(
        Json.obj(
          "_id" -> testJourneyId,
          "authInternalId" -> testInternalId,
          "creationTimestamp" -> Json.obj("$date" -> Instant.now.toEpochMilli)
        ) ++ Json.toJsObject(testFullVatKnownFacts)
      ))
      await(insertJourneyConfig(testJourneyId, testContinueUrl))
      stubAllocateEnrolment(testFullVatKnownFacts, testCredentialId, testGroupId)(INTERNAL_SERVER_ERROR, Json.obj())
      stubGetUserIds(testVatNumber)(OK)
      stubAudit

      lazy val result = post(s"/$testJourneyId/check-your-answers-vat")()

      result must have(
        httpStatus(SEE_OTHER),
        redirectUri(errorRoutes.EnrolmentAlreadyAllocatedController.show().url)
      )
      verifyAudit()
    }

    "throw an exception when no userIds are connected with the vatNumber" in {
      stubAuth(OK, successfulAuthResponse(Some(testGroupId), Some(testInternalId)))
      await(journeyDataRepository.collection.insert(true).one(
        Json.obj(
          "_id" -> testJourneyId,
          "authInternalId" -> testInternalId,
          "creationTimestamp" -> Json.obj("$date" -> Instant.now.toEpochMilli)
        ) ++ Json.toJsObject(testFullVatKnownFacts)
      ))
      await(insertJourneyConfig(testJourneyId, testContinueUrl))
      stubAllocateEnrolment(testFullVatKnownFacts, testCredentialId, testGroupId)(INTERNAL_SERVER_ERROR, Json.obj())
      stubGetUserIds(testVatNumber)(NO_CONTENT)
      stubAudit

      lazy val result = post(s"/$testJourneyId/check-your-answers-vat")()

      result must have(
        httpStatus(INTERNAL_SERVER_ERROR)
      )
      verifyAudit()
    }


    "return UNAUTHORISED when no credentials or groupId are retrieved from Auth" in {
      stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
      stubAudit

      lazy val result = post(s"/$testJourneyId/check-your-answers-vat")()

      result.status mustBe UNAUTHORIZED
    }

  }

}
