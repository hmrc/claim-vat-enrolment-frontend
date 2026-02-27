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

package uk.gov.hmrc.claimvatenrolmentfrontend.controllers

import org.mongodb.scala.result.InsertOneResult
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.test.Helpers._
import uk.gov.hmrc.claimvatenrolmentfrontend.assets.TestConstants._
import uk.gov.hmrc.claimvatenrolmentfrontend.controllers.errorPages.{routes => errorRoutes}
import uk.gov.hmrc.claimvatenrolmentfrontend.featureswitch.core.config.KnownFactsCheckFlag
import uk.gov.hmrc.claimvatenrolmentfrontend.models.AllocateEnrolmentResponseHttpParser.MultipleEnrolmentsInvalidKey
import uk.gov.hmrc.claimvatenrolmentfrontend.models.VatKnownFacts
import uk.gov.hmrc.claimvatenrolmentfrontend.repositories.JourneyDataRepository._
import uk.gov.hmrc.claimvatenrolmentfrontend.stubs.{AllocationEnrolmentStub, AuthStub, EnrolmentStoreProxyStub}
import uk.gov.hmrc.claimvatenrolmentfrontend.utils.JourneyMongoHelper
import uk.gov.hmrc.claimvatenrolmentfrontend.utils.WiremockHelper._
import uk.gov.hmrc.claimvatenrolmentfrontend.views.CheckYourAnswersViewTests

import java.time.Instant

class CheckYourAnswersControllerISpec
    extends JourneyMongoHelper
    with CheckYourAnswersViewTests
    with AuthStub
    with AllocationEnrolmentStub
    with EnrolmentStoreProxyStub {

  def extraConfig: Map[String, String] = Map(
    "auditing.enabled"               -> "true",
    "auditing.consumer.baseUri.host" -> mockHost,
    "auditing.consumer.baseUri.port" -> mockPort
  )

  override lazy val app: Application = new GuiceApplicationBuilder()
    .configure(config ++ extraConfig)
    .build()

  override def beforeEach(): Unit = {
    super.beforeEach()
    enable(KnownFactsCheckFlag)
  }

  "GET /<testJourneyId?/check-your-answers-vat" should {
    "render the page correctly" when {
      "user has saved data for VRN, Reg date, Currently submitting: Yes, Return total, and Last month values" should {
        val knownFactsData = vatKnownFactsWithFullReturnsInformation(hasPostcode = false)
        lazy val result = {
          createSavedJourneyData(knownFactsData)
          stubAuth(OK, successfulAuthResponse(Some(testGroupId), Some(testInternalId)))
          stubAudit
          get(s"/$testJourneyId/check-your-answers-vat")
        }

        returnOkResult(result)
        checkPageDisplaysCompulsoryDetails(result)
        checkPageDisplaysReturnTotalAndLastMonthDetailsWhenCurrentlySubmittingIsTrue(result, hasPostCode = false)
      }

      "user has saved data for VRN, Reg date, Postcode, Currently submitting: No, and VAN" should {
        val knownFactsData = vatKnownFactsWithFormBundleReference(hasPostcode = true)
        lazy val result = {
          createSavedJourneyData(knownFactsData)
          stubAuth(OK, successfulAuthResponse(Some(testGroupId), Some(testInternalId)))
          stubAudit
          get(s"/$testJourneyId/check-your-answers-vat")
        }

        returnOkResult(result)
        checkPageDisplaysCompulsoryDetails(result)
        checkPageDisplaysPostcodeDetails(result)
        checkPageDisplaysVatApplicationNumberDetailsWhenCurrentlySubmittingIsFalse(result, hasPostCode = true)
      }
    }

    "redirect to the first page in journey (CaptureVatRegistrationDate page)" when {
      "user has incomplete journey data" in {
        val incompleteJourneyData = vatKnownFactsWithFormBundleReference(hasPostcode = false).copy(vatRegistrationDate = None)
        lazy val result = {
          createSavedJourneyData(incompleteJourneyData)
          stubAuth(OK, successfulAuthResponse(Some(testGroupId), Some(testInternalId)))
          stubAudit
          get(s"/$testJourneyId/check-your-answers-vat")
        }

        result must have(
          httpStatus(SEE_OTHER),
          redirectUri(routes.CaptureVatRegistrationDateController.show(testJourneyId).url)
        )
      }
    }

    "redirect to the KnownFactsMismatchWithin24hrs lockout page" when {
      "user ID is locked in the UserLockRepository" in {
        lazy val result = {
          createSavedJourneyData(baseVatKnownFacts)
          await(insertLockData(testVatNumber, testInternalId, testSubmissionNumber3))
          stubAuth(OK, successfulAuthResponse(Some(testGroupId), Some(testInternalId)))
          stubAudit
          get(s"/$testJourneyId/check-your-answers-vat")
        }

        result must have(
          httpStatus(SEE_OTHER),
          redirectUri(errorRoutes.KnownFactsMismatchWithin24hrsController.show().url)
        )
      }
    }

    "redirect to the generic error page" when {
      "user has no journey data" in {
        lazy val result = {
          stubAuth(OK, successfulAuthResponse(Some(testGroupId), Some(testInternalId)))
          get(s"/$testJourneyId/check-your-answers-vat")
        }

        result must have(
          httpStatus(SEE_OTHER),
          redirectUri(errorPages.routes.ServiceTimeoutController.show().url)
        )
      }

      "the internal IDs do not match" in {
        lazy val result = {
          stubAuth(OK, successfulAuthResponse(Some(testGroupId), Some("aDifferentInternalID")))
          stubAudit
          get(s"/$testJourneyId/check-your-answers-vat")
        }

        result must have(
          httpStatus(SEE_OTHER),
          redirectUri(errorPages.routes.ServiceTimeoutController.show().url)
        )
      }
    }

    "return a 500 response" when {
      "there is no Auth ID" in {
        await(insertVatKnownFactsData(testJourneyId, testInternalId, baseVatKnownFacts))
        stubAuth(OK, successfulAuthResponse(None))

        lazy val result = get(s"/$testJourneyId/check-your-answers-vat")

        result.status mustBe INTERNAL_SERVER_ERROR
      }
    }
  }

  s"POST /$testJourneyId/check-your-answers-vat" should {
    val fullJourneyData = vatKnownFactsWithFormBundleReference(true)

    "redirect to the SignUpComplete page" when {
      "the allocation was successfully created" in {
        stubAuth(OK, successfulAuthResponse(Some(testGroupId), Some(testInternalId)))
        await(insertJourneyConfig(testJourneyId, testContinueUrl, testInternalId))
        createSavedJourneyData(fullJourneyData)
        stubAllocateEnrolment(fullJourneyData, testCredentialId, includeFormBundleReference = true, testGroupId)(CREATED, Json.obj())
        stubAudit

        lazy val result = post(s"/$testJourneyId/check-your-answers-vat")()

        result must have(
          httpStatus(SEE_OTHER),
          redirectUri(routes.SignUpCompleteController.signUpComplete(testJourneyId).url)
        )
        verifyAudit()
      }
    }

    "redirect to the KnownFactsMismatch page" when {
      "the enrolment returns a BAD_REQUEST and enrolment store proxy ES0 returns NO_CONTENT" in {
        stubAuth(OK, successfulAuthResponse(Some(testGroupId), Some(testInternalId)))
        createSavedJourneyData(fullJourneyData)
        stubAllocateEnrolment(fullJourneyData, testCredentialId, includeFormBundleReference = true, testGroupId)(BAD_REQUEST, Json.obj())
        stubGetUserIds(testVatNumber)(NO_CONTENT)
        stubAudit

        lazy val result = post(s"/$testJourneyId/check-your-answers-vat")()

        result must have(
          httpStatus(SEE_OTHER),
          redirectUri(errorRoutes.KnownFactsMismatchController.show().url)
        )
        verifyAudit()
      }
    }

    "redirect to the ThirdAttemptLockout page" when {
      "the enrolment returns BAD_REQUEST for 3 invalid attempts consecutively" in {
        stubAuth(OK, successfulAuthResponse(Some(testGroupId), Some(testInternalId)))
        createSavedJourneyData(fullJourneyData)
        stubAllocateEnrolment(fullJourneyData, testCredentialId, includeFormBundleReference = true, testGroupId)(BAD_REQUEST, Json.obj())
        stubGetUserIds(testVatNumber)(NO_CONTENT)
        stubAudit

        await(insertLockData(testVatNumber, testInternalId, testSubmissionNumber3))

        lazy val result = post(s"/$testJourneyId/check-your-answers-vat")()

        result must have(
          httpStatus(SEE_OTHER),
          redirectUri(errorRoutes.ThirdAttemptLockoutController.show().url)
        )
        verifyAudit()
      }
    }

    "redirect to the EnrolmentAlreadyAllocated page" when {
      "the enrolment returns BAD_REQUEST and enrolment store proxy ES0 returns OK" in {
        stubAuth(OK, successfulAuthResponse(Some(testGroupId), Some(testInternalId)))
        createSavedJourneyData(fullJourneyData)
        stubAllocateEnrolment(fullJourneyData, testCredentialId, includeFormBundleReference = true, testGroupId)(BAD_REQUEST, Json.obj())
        stubGetUserIds(testVatNumber)(OK)
        stubAudit

        lazy val result = post(s"/$testJourneyId/check-your-answers-vat")()

        result must have(
          httpStatus(SEE_OTHER),
          redirectUri(errorRoutes.EnrolmentAlreadyAllocatedController.show().url)
        )
        verifyAudit()
      }
    }

    "redirect to the UnmatchedUser page" when {
      "the user group already has a matching enrolment, but the user does not" in {
        stubAuth(OK, successfulAuthResponse(Some(testGroupId), Some(testInternalId)))
        createSavedJourneyData(fullJourneyData)
        stubAllocateEnrolment(fullJourneyData, testCredentialId, includeFormBundleReference = true, testGroupId)(
          CONFLICT,
          Json.obj("code" -> MultipleEnrolmentsInvalidKey))
        stubAudit

        lazy val result = post(s"/$testJourneyId/check-your-answers-vat")()

        result must have(
          httpStatus(SEE_OTHER),
          redirectUri(errorRoutes.UnmatchedUserErrorController.show().url)
        )
        verifyAudit()
      }
    }

    "redirect to the generic error page" when {
      "there is no journeyData" in {
        stubAuth(OK, successfulAuthResponse(Some(testGroupId), Some(testInternalId)))
        stubAllocateEnrolment(fullJourneyData, testCredentialId, includeFormBundleReference = true, testGroupId)(BAD_REQUEST, Json.obj())
        stubGetUserIds(testVatNumber)(NO_CONTENT)
        stubAudit

        lazy val result = post(s"/$testJourneyId/check-your-answers-vat")()

        result must have(
          httpStatus(SEE_OTHER),
          redirectUri(errorPages.routes.ServiceTimeoutController.show().url)
        )
      }

      "there is no journeyConfig" in {
        stubAuth(OK, successfulAuthResponse(Some(testGroupId), Some(testInternalId)))
        createSavedJourneyData(fullJourneyData)

        stubAllocateEnrolment(fullJourneyData, testCredentialId, includeFormBundleReference = true, testGroupId)(CREATED, Json.obj())
        lazy val result = post(s"/$testJourneyId/check-your-answers-vat")()

        result must have(
          httpStatus(SEE_OTHER),
          redirectUri(errorPages.routes.ServiceTimeoutController.show().url)
        )
      }
    }

    "return an InternalServerError" when {
      "no credentials or groupId are retrieved from Auth" in {
        stubAuth(OK, successfulAuthResponse(Some(testGroupId)))
        stubAudit

        lazy val result = post(s"/$testJourneyId/check-your-answers-vat")()

        result.status mustBe INTERNAL_SERVER_ERROR
      }

      "no userIds are connected with the vatNumber" in {
        stubAuth(OK, successfulAuthResponse(Some(testGroupId), Some(testInternalId)))
        createSavedJourneyData(fullJourneyData)
        stubAllocateEnrolment(fullJourneyData, testCredentialId, includeFormBundleReference = true, testGroupId)(INTERNAL_SERVER_ERROR, Json.obj())
        stubGetUserIds(testVatNumber)(NO_CONTENT)
        stubAudit

        lazy val result = post(s"/$testJourneyId/check-your-answers-vat")()

        result.status mustBe INTERNAL_SERVER_ERROR
        verifyAudit()
      }
    }

  }

  private def createSavedJourneyData(journeyData: VatKnownFacts): InsertOneResult =
    await(
      journeyDataRepository.collection
        .insertOne(
          Json.obj(
            "_id"               -> testJourneyId,
            "authInternalId"    -> testInternalId,
            "creationTimestamp" -> Json.obj("$date" -> Instant.now.toEpochMilli)
          ) ++ Json.toJsObject(journeyData)
        )
        .toFuture())

}
