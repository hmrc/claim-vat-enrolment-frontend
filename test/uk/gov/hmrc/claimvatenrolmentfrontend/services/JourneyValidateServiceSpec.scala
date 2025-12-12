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
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.mvc._
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.claimvatenrolmentfrontend.config.AppConfig
import uk.gov.hmrc.claimvatenrolmentfrontend.featureswitch.core.config.{FeatureSwitching, KnownFactsCheckFlag}
import uk.gov.hmrc.claimvatenrolmentfrontend.helpers.TestConstants._
import uk.gov.hmrc.claimvatenrolmentfrontend.repositories.mocks.{MockJourneyDataRepository, MockJourneySubmissionRepository}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContextExecutor, Future}

class JourneyValidateServiceSpec extends AnyWordSpec with GuiceOneAppPerSuite with Matchers with MockJourneyDataRepository
                with MockJourneySubmissionRepository with FeatureSwitching {

  implicit val hc: HeaderCarrier = HeaderCarrier()
  implicit val request: Request[_] = FakeRequest()
  implicit val appConfig: AppConfig = app.injector.instanceOf[AppConfig]

  implicit val ec: ExecutionContextExecutor = scala.concurrent.ExecutionContext.global

  class TestService(flag: Boolean) extends JourneyValidateService(mockJourneyDataRepository, mockJourneySubmissionRepository, appConfig){
    override def isJourneyLocked(journeyId: String, authInternalId: String)(implicit request: Request[_]) =
      Future.successful(flag)
  }

  "the journey continue" when {
    "knownFactsCheck is disabled" in {
      disable(KnownFactsCheckFlag)

      val result = new TestService(false).continueIfJourneyIsNotLocked(testJourneyId, testInternalId)(continueResult)

      result.map(_.header.status mustBe 200)

      contentAsString(result) shouldBe "next action executed"
    }
  }

  "the journey redirect to errorPage " when {
    "knownFactsCheck is enabled" in {
      enable(KnownFactsCheckFlag)

      mockGetVrnInfo(testJourneyId, testInternalId)(Future.successful(None))

      val result = new TestService(true).continueIfJourneyIsNotLocked(testJourneyId, testInternalId)(continueResult)

      result.map(_.header.status mustBe Some(SEE_OTHER))
    }
  }
}
