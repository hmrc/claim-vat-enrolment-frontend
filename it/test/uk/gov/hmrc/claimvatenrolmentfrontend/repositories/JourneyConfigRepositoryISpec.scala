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

package uk.gov.hmrc.claimvatenrolmentfrontend.repositories

import org.scalatest.concurrent.{AbstractPatienceConfiguration, Eventually}
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers._
import uk.gov.hmrc.claimvatenrolmentfrontend.assets.TestConstants.{testContinueUrl, testInternalId, testJourneyId}
import uk.gov.hmrc.claimvatenrolmentfrontend.models.JourneyConfig
import uk.gov.hmrc.claimvatenrolmentfrontend.utils.JourneyMongoHelper

class JourneyConfigRepositoryISpec extends JourneyMongoHelper with AbstractPatienceConfiguration with Eventually {

  override lazy val app: Application = new GuiceApplicationBuilder()
    .configure(config)
    .configure("application.router" -> "testOnlyDoNotUseInAppConf.Routes")
    .build()

  "documents" should {
    "successfully insert a new document" in {
      await(journeyConfigRepository.insertJourneyConfig(testJourneyId, JourneyConfig(testContinueUrl), testInternalId))
      await(countConfigRepo) mustBe 1
    }

    "successfully insert journeyConfig" in {
      await(journeyConfigRepository.insertJourneyConfig(testJourneyId, JourneyConfig(testContinueUrl), testInternalId))
      await(journeyConfigRepository.retrieveJourneyConfig(testJourneyId, testInternalId)) must contain(JourneyConfig(testContinueUrl))
    }

    "return None if the journey does not exist" in {
      await(journeyConfigRepository.insertJourneyConfig(testJourneyId, JourneyConfig(testContinueUrl), testInternalId))
      await(journeyConfigRepository.retrieveJourneyConfig(testJourneyId + 1, testInternalId)) mustBe None
    }

  }
}
