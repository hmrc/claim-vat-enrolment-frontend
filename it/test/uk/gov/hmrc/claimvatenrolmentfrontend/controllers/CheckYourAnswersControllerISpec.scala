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

package uk.gov.hmrc.claimvatenrolmentfrontend.controllers

import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.test.Helpers._
import uk.gov.hmrc.claimvatenrolmentfrontend.assets.TestConstants._
import uk.gov.hmrc.claimvatenrolmentfrontend.controllers.errorPages.{routes => errorRoutes}
import uk.gov.hmrc.claimvatenrolmentfrontend.featureswitch.core.config.KnownFactsCheckFlag
import uk.gov.hmrc.claimvatenrolmentfrontend.models.AllocateEnrolmentResponseHttpParser.MultipleEnrolmentsInvalidKey
import uk.gov.hmrc.claimvatenrolmentfrontend.repositories.JourneyDataRepository._
import uk.gov.hmrc.claimvatenrolmentfrontend.stubs.{AllocationEnrolmentStub, AuthStub, EnrolmentStoreProxyStub}
import uk.gov.hmrc.claimvatenrolmentfrontend.utils.JourneyMongoHelper
import uk.gov.hmrc.claimvatenrolmentfrontend.utils.WiremockHelper._
import uk.gov.hmrc.claimvatenrolmentfrontend.views.CheckYourAnswersViewTests

import java.time.Instant


class CheckYourAnswersControllerISpec extends JourneyMongoHelper
  with CheckYourAnswersViewTests
  with AuthStub
  with AllocationEnrolmentStub
  with EnrolmentStoreProxyStub {

  def extraConfig: Map[String, String] = Map(
    "auditing.enabled" -> "true",
    "auditing.consumer.baseUri.host" -> mockHost,
    "auditing.consumer.baseUri.port" -> mockPort
  )

  override lazy val app: Application = new GuiceApplicationBuilder()
    .configure(config ++ extraConfig)
    .build()

  override def beforeEach(): Unit = {
    super.beforeEach()
  }

  s"GET /$testJourneyId/check-your-answers-vat" when {
    "there is a full VatKnownFacts stored in the database" should {
      lazy val result = {
        await(journeyDataRepository.collection.insertOne(
          Json.obj(
            "_id" -> testJourneyId,
            "authInternalId" -> testInternalId,
            "creationTimestamp" -> Json.obj("$date" -> Instant.now.toEpochMilli)
          ) ++ Json.toJsObject(testFullVatKnownFacts)
        ).toFuture())
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
        await(journeyDataRepository.collection.insertOne(
          Json.obj(
            "_id" -> testJourneyId,
            "authInternalId" -> testInternalId,
            "creationTimestamp" -> Json.obj("$date" -> Instant.now.toEpochMilli)
          ) ++ Json.toJsObject(testVatKnownFactsNoPostcode)
        ).toFuture())
        stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
        stubAudit
        get(s"/$testJourneyId/check-your-answers-vat")
      }

      "return OK" in {
        result.status mustBe OK
      }

      testCheckYourAnswersViewNoPostcode(result)
    }

   "there is an invalid VatKnownFacts stored in the database" should {
      lazy val result = {
        await(journeyDataRepository.collection.insertOne(
          Json.obj(
            "_id" -> testJourneyId,
            "authInternalId" -> testInternalId,
            "creationTimestamp" -> Json.obj("$date" -> Instant.now.toEpochMilli),
            VatNumberKey -> testVatNumber,
            VatRegistrationDateKey -> testVatRegDate,
            SubmittedVatReturnKey -> true
          )
        ).toFuture())
        stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
        stubAudit
        get(s"/$testJourneyId/check-your-answers-vat")
      }

      "return a redirect to the registration date page" in {
        result must have(
          httpStatus(SEE_OTHER),
          redirectUri(routes.CaptureVatRegistrationDateController.show(testJourneyId).url)
        )
      }

    }

    "there is a VatKnownFacts with no returns stored in the database" should {
      lazy val result = {
        await(journeyDataRepository.collection.insertOne(
          Json.obj(
            "_id" -> testJourneyId,
            "authInternalId" -> testInternalId,
            "creationTimestamp" -> Json.obj("$date" -> Instant.now.toEpochMilli)
          ) ++ Json.toJsObject(testVatKnownFactsNoReturns)
        ).toFuture())
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
        await(journeyDataRepository.collection.insertOne(
          Json.obj(
            "_id" -> testJourneyId,
            "authInternalId" -> testInternalId,
            "creationTimestamp" -> Json.obj("$date" -> Instant.now.toEpochMilli)
          ) ++ Json.toJsObject(testVatKnownFactsNoReturnsNoPostcode)
        ).toFuture())
        stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
        stubAudit
        get(s"/$testJourneyId/check-your-answers-vat")
      }

      "return OK" in {
        result.status mustBe OK
      }

      testCheckYourAnswersViewNoReturnsNoPostcode(result)
    }

    "the internal Ids do not match" should {
      lazy val result = {
        await(journeyDataRepository.collection.insertOne(
          Json.obj(
            "_id" -> testJourneyId,
            "authInternalId" -> "testInternalId",
            "creationTimestamp" -> Json.obj("$date" -> Instant.now.toEpochMilli)
          ) ++ Json.toJsObject(testVatKnownFactsNoReturnsNoPostcode)
        ).toFuture())
        stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
        stubAudit
        get(s"/$testJourneyId/check-your-answers-vat")
      }
      "Show an error page" in {
        result must have(
          httpStatus(SEE_OTHER),
          redirectUri(errorPages.routes.ServiceTimeoutController.show().url)
        )
      }
    }

    "return 500" when {
      "there is no auth id" in {
        await(insertJourneyConfig(testJourneyId, testContinueUrl, testInternalId))
        stubAuth(OK, successfulAuthResponse(None))
        lazy val result = get(s"/$testJourneyId/check-your-answers-vat")

        result.status mustBe INTERNAL_SERVER_ERROR
      }
    }
  }

  s"POST /$testJourneyId/check-your-answers-vat" should {
    "redirect to SignUpComplete page when the allocation was successfully created" in {
      stubAuth(OK, successfulAuthResponse(Some(testGroupId), Some(testInternalId)))
      await(journeyDataRepository.collection.insertOne(
        Json.obj(
          "_id" -> testJourneyId,
          "authInternalId" -> testInternalId,
          "creationTimestamp" -> Json.obj("$date" -> Instant.now.toEpochMilli)
        ) ++ Json.toJsObject(testFullVatKnownFacts)
      ).toFuture())
      await(insertJourneyConfig(testJourneyId, testContinueUrl, testInternalId))
      stubAllocateEnrolment(testFullVatKnownFacts, testCredentialId, testGroupId)(CREATED, Json.obj())
      stubAudit

      lazy val result = post(s"/$testJourneyId/check-your-answers-vat")()

      result must have(
        httpStatus(SEE_OTHER),
        redirectUri(routes.SignUpCompleteController.signUpComplete(testJourneyId).url)
      )
      verifyAudit()
    }

    "redirect to UnmatchedUser if the user group already has a matching enrolment, but the user does not" in {
      stubAuth(OK, successfulAuthResponse(Some(testGroupId), Some(testInternalId)))
      await(journeyDataRepository.collection.insertOne(
        Json.obj(
          "_id" -> testJourneyId,
          "authInternalId" -> testInternalId,
          "creationTimestamp" -> Json.obj("$date" -> Instant.now.toEpochMilli)
        ) ++ Json.toJsObject(testFullVatKnownFacts)
      ).toFuture())
      await(insertJourneyConfig(testJourneyId, testContinueUrl, testInternalId))
      stubAllocateEnrolment(testFullVatKnownFacts, testCredentialId, testGroupId)(CONFLICT, Json.obj("code" -> MultipleEnrolmentsInvalidKey))
      stubAudit

      lazy val result = post(s"/$testJourneyId/check-your-answers-vat")()

      result must have(
        httpStatus(SEE_OTHER),
        redirectUri(errorRoutes.UnmatchedUserErrorController.show().url)
      )
      verifyAudit()
    }

    "redirect to KnownFactsMismatch if the enrolment returns BAD_REQUEST and enrolment store proxy ES0 returns NO_CONTENT" in {
      stubAuth(OK, successfulAuthResponse(Some(testGroupId), Some(testInternalId)))
      await(journeyDataRepository.collection.insertOne(
        Json.obj(
          "_id" -> testJourneyId,
          "authInternalId" -> testInternalId,
          "creationTimestamp" -> Json.obj("$date" -> Instant.now.toEpochMilli)
        ) ++ Json.toJsObject(testFullVatKnownFacts)
      ).toFuture())
      await(insertJourneyConfig(testJourneyId, testContinueUrl, testInternalId))
      stubAllocateEnrolment(testFullVatKnownFacts, testCredentialId, testGroupId)(BAD_REQUEST, Json.obj())
      stubGetUserIds(testVatNumber)(NO_CONTENT)
      stubAudit

      lazy val result = post(s"/$testJourneyId/check-your-answers-vat")()


      result must have(
        httpStatus(SEE_OTHER),
        redirectUri(errorRoutes.KnownFactsMismatchController.show().url)
      )
      verifyAudit()
    }

    "redirect to AttemptLocked if the enrolment returns BAD_REQUEST for 3 invalid attempts consecutively" in {
      enable(KnownFactsCheckFlag) //enable the knownFactsCheck Feature-Switch for the CVE-CR
      enable(KnownFactsCheckFlag) //enable the knownFactsCheck Feature-Switch for the CVE-CR

      stubAuth(OK, successfulAuthResponse(Some(testGroupId), Some(testInternalId)))

      await(journeyDataRepository.collection.insertOne(
        Json.obj(
          "_id" -> testJourneyId,
          "authInternalId" -> testInternalId,
          "creationTimestamp" -> Json.obj("$date" -> Instant.now.toEpochMilli)
        ) ++ Json.toJsObject(testFullVatKnownFacts)
      ).toFuture())
      await(insertJourneyConfig(testJourneyId, testContinueUrl, testInternalId))
      stubAllocateEnrolment(testFullVatKnownFacts, testCredentialId, testGroupId)(BAD_REQUEST, Json.obj())
      stubGetUserIds(testVatNumber)(NO_CONTENT)
      stubAudit

      await(journeySubmissionRepository.collection.insertOne(
          Json.obj(
            "journeyId"       ->  testJourneyId,
            "vrn"             ->  testVatNumber,
            "submissionNumber"  -> testSubmissionNumber2,
            "accountStatus"     -> testAccountStatusUnLocked
          ) ++ Json.toJsObject(testSubmissionDataAttempt2)
      ).toFuture())

      lazy val result = post(s"/$testJourneyId/check-your-answers-vat")()

      result must have(
        httpStatus(SEE_OTHER),
        redirectUri(errorRoutes.ThirdAttemptLockoutController.show().url)
      )
      verifyAudit()
    }

    "redirect to EnrolmentAlreadyAllocated if the enrolment returns BAD_REQUEST and enrolment store proxy ES0 returns OK" in {
      stubAuth(OK, successfulAuthResponse(Some(testGroupId), Some(testInternalId)))
      await(journeyDataRepository.collection.insertOne(
        Json.obj(
          "_id" -> testJourneyId,
          "authInternalId" -> testInternalId,
          "creationTimestamp" -> Json.obj("$date" -> Instant.now.toEpochMilli)
        ) ++ Json.toJsObject(testFullVatKnownFacts)
      ).toFuture())
      await(insertJourneyConfig(testJourneyId, testContinueUrl, testInternalId))
      stubAllocateEnrolment(testFullVatKnownFacts, testCredentialId, testGroupId)(BAD_REQUEST, Json.obj())
      stubGetUserIds(testVatNumber)(OK)
      stubAudit

      lazy val result = post(s"/$testJourneyId/check-your-answers-vat")()


      result must have(
        httpStatus(SEE_OTHER),
        redirectUri(errorRoutes.EnrolmentAlreadyAllocatedController.show().url)
      )
      verifyAudit()
    }

    "redirect to EnrolmentAlreadyAllocated error page when enrolment fails and enrolment store proxy ES0 returns OK" in {
      stubAuth(OK, successfulAuthResponse(Some(testGroupId), Some(testInternalId)))
      await(journeyDataRepository.collection.insertOne(
        Json.obj(
          "_id" -> testJourneyId,
          "authInternalId" -> testInternalId,
          "creationTimestamp" -> Json.obj("$date" -> Instant.now.toEpochMilli)
        ) ++ Json.toJsObject(testFullVatKnownFacts)
      ).toFuture())
      await(insertJourneyConfig(testJourneyId, testContinueUrl, testInternalId))
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
      await(journeyDataRepository.collection.insertOne(
        Json.obj(
          "_id" -> testJourneyId,
          "authInternalId" -> testInternalId,
          "creationTimestamp" -> Json.obj("$date" -> Instant.now.toEpochMilli)
        ) ++ Json.toJsObject(testFullVatKnownFacts)
      ).toFuture())
      await(insertJourneyConfig(testJourneyId, testContinueUrl, testInternalId))
      stubAllocateEnrolment(testFullVatKnownFacts, testCredentialId, testGroupId)(INTERNAL_SERVER_ERROR, Json.obj())
      stubGetUserIds(testVatNumber)(NO_CONTENT)
      stubAudit

      lazy val result = post(s"/$testJourneyId/check-your-answers-vat")()

      result must have(
        httpStatus(INTERNAL_SERVER_ERROR)
      )
      verifyAudit()
    }


    "Redirect to an error page when there is no journeyData" in {
      stubAuth(OK, successfulAuthResponse(Some(testGroupId), Some(testInternalId)))
      await(insertJourneyConfig(testJourneyId, testContinueUrl, testInternalId))
      stubAllocateEnrolment(testFullVatKnownFacts, testCredentialId, testGroupId)(BAD_REQUEST, Json.obj())
      stubGetUserIds(testVatNumber)(NO_CONTENT)
      stubAudit

      lazy val result = post(s"/$testJourneyId/check-your-answers-vat")()

      result must have(
        httpStatus(SEE_OTHER),
        redirectUri(errorPages.routes.ServiceTimeoutController.show().url)
      )
    }

    "Redirect to an error page when there is no journeyConfig" in {
      stubAuth(OK, successfulAuthResponse(Some(testGroupId), Some(testInternalId)))
      await(journeyDataRepository.collection.insertOne(
        Json.obj(
          "_id" -> testJourneyId,
          "authInternalId" -> testInternalId,
          "creationTimestamp" -> Json.obj("$date" -> Instant.now.toEpochMilli)
        ) ++ Json.toJsObject(testFullVatKnownFacts)
      ).toFuture())

      stubAllocateEnrolment(testFullVatKnownFacts, testCredentialId, testGroupId)(CREATED, Json.obj())
      lazy val result = post(s"/$testJourneyId/check-your-answers-vat")()

      result must have(
        httpStatus(SEE_OTHER),
        redirectUri(errorPages.routes.ServiceTimeoutController.show().url)
      )
    }


    "return Internal Server Error when no credentials or groupId are retrieved from Auth" in {
      stubAuth(OK, successfulAuthResponse(Some(testInternalId)))
      stubAudit

      lazy val result = post(s"/$testJourneyId/check-your-answers-vat")()

      result.status mustBe INTERNAL_SERVER_ERROR
    }

  }

}
