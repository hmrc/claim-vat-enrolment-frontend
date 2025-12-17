/*
 * Copyright 2025 HM Revenue & Customs
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

import org.scalatest.PrivateMethodTester
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.mvc._
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.claimvatenrolmentfrontend.config.AppConfig
import uk.gov.hmrc.claimvatenrolmentfrontend.featureswitch.core.config.{FeatureSwitching, KnownFactsCheckFlag}
import uk.gov.hmrc.claimvatenrolmentfrontend.helpers.TestConstants._
import uk.gov.hmrc.claimvatenrolmentfrontend.repositories.mocks.{MockJourneyDataRepository, MockJourneySubmissionRepository}
import uk.gov.hmrc.claimvatenrolmentfrontend.services.mocks.MockJourneyValidationService
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContextExecutor, Future}

class JourneyValidateServiceSpec
    extends AnyWordSpec
    with GuiceOneAppPerSuite
    with Matchers
    with MockJourneyDataRepository
    with MockJourneySubmissionRepository
    with FeatureSwitching
    with PrivateMethodTester
    with MockJourneyValidationService
    with ScalaFutures {

  implicit val hc: HeaderCarrier    = HeaderCarrier()
  implicit val request: Request[_]  = FakeRequest()
  implicit val appConfig: AppConfig = app.injector.instanceOf[AppConfig]

  implicit val ec: ExecutionContextExecutor = scala.concurrent.ExecutionContext.global

  object TestService extends JourneyValidateService(mockJourneyDataRepository, mockJourneySubmissionRepository, appConfig)

  "continueIfJourneyIsNotLocked" should {

    "redirect to the KnownFactsMismatchWithin24hrs Error Page" when {
      "isJourneyLocked method returns 'true'" in {
        enable(KnownFactsCheckFlag)
        mockGetVrnInfo(testJourneyId, testInternalId)(Future.successful(Some(testVatNumber)))
        mockIsVrnLocked(testVatNumber)(Future.successful(true))

        val result = TestService.continueIfJourneyIsNotLocked(testJourneyId, testInternalId)(continueResult)(request)

        result.map(_.header.status mustBe Some(SEE_OTHER))
        result.map(_.header.headers.get("location") mustBe "/claim-vat-enrolment/error/access-still-locked")
      }
    }

    "follow the 'continue' result argument" when {
      "isJourneyLocked method returns 'false'" in {
        enable(KnownFactsCheckFlag)
        mockGetVrnInfo(testJourneyId, testInternalId)(Future.successful(Some(testVatNumber)))
        mockIsVrnLocked(testVatNumber)(Future.successful(false))

        val result = TestService.continueIfJourneyIsNotLocked(testJourneyId, testInternalId)(continueResult)
        result.map(_.header.status mustBe OK)
        contentAsString(result) mustBe "next action executed"
      }
    }

    "return an Exception" when {
      "'isJourneyLocked' method returns an error" in {
        enable(KnownFactsCheckFlag)
        mockGetVrnInfo(testJourneyId, testInternalId)(Future.failed(new RuntimeException("Internal Server Error")))

        val result = TestService.continueIfJourneyIsNotLocked(testJourneyId, testInternalId)(continueResult)
        result.failed.map { ex =>
          ex mustBe a[RuntimeException]
          ex.getMessage mustBe "Internal Server Error"
        }
      }
    }

    "isJourneyLocked" when {
      "'KnownFactsCheck' switch is enabled" should {
        "return 'true' in a future" when {
          "user has VRN journey data and the VRN is locked" in {
            enable(KnownFactsCheckFlag)
            mockGetVrnInfo(testJourneyId, testInternalId)(Future.successful(Some(testVatNumber)))
            mockIsVrnLocked(testVatNumber)(Future.successful(true))

            val result = TestService.isJourneyLocked(testJourneyId, testInternalId)(request)

            whenReady(result) { isUserLocked =>
              isUserLocked mustBe true
            }
          }
        }

        "return 'false' in a future" when {
          "user has VRN journey data and the VRN is not locked" in {
            enable(KnownFactsCheckFlag)
            mockGetVrnInfo(testJourneyId, testInternalId)(Future.successful(Some(testVatNumber)))
            mockIsVrnLocked(testVatNumber)(Future.successful(false))

            val result = TestService.isJourneyLocked(testJourneyId, testInternalId)(request)

            whenReady(result) { isUserLocked =>
              isUserLocked mustBe false
            }
          }
          "user has no VRN journey data" in {
            enable(KnownFactsCheckFlag)
            mockGetVrnInfo(testJourneyId, testInternalId)(Future.successful(None))
            mockIsVrnLocked(testVatNumber)(Future.successful(true))

            val result = TestService.isJourneyLocked(testJourneyId, testInternalId)(request)

            whenReady(result) { isUserLocked =>
              isUserLocked mustBe false
            }
          }
        }
      }

      "'KnownFactsCheck' switch is disabled" should {
        "always return 'false' in a future" in {
          disable(KnownFactsCheckFlag)
          val result = TestService.isJourneyLocked(testJourneyId, testInternalId)(request)

          whenReady(result) { isUserLocked =>
            isUserLocked mustBe false
          }
        }
      }
    }
  }
}
