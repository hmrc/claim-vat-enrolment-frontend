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

import play.api.mvc.Results.Redirect
import play.api.mvc.{Request, Result}
import uk.gov.hmrc.claimvatenrolmentfrontend.config.AppConfig
import uk.gov.hmrc.claimvatenrolmentfrontend.controllers.errorPages
import uk.gov.hmrc.claimvatenrolmentfrontend.repositories.{JourneyDataRepository, JourneySubmissionRepository}
import utils.LoggingUtil

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class JourneyValidateService @Inject()(
                                       journeyDataRepository: JourneyDataRepository,
                                       journeySubmissionRepository: JourneySubmissionRepository,
                                       config: AppConfig
                              )(implicit ec: ExecutionContext) extends LoggingUtil{

  def continueIfJourneyIsNotLocked(journeyId: String, authInternalId: String)(continue: Result)(implicit request: Request[_]): Future[Result] = {
    isJourneyLocked(journeyId, authInternalId).map { isLocked =>
        if(isLocked) { println("true redirect")
                       Redirect(errorPages.routes.KnownFactsMismatchWithin24hrsController.show()) } else {println("false block")
          continue}
    }
  }

  def isJourneyLocked(journeyId: String, authInternalId: String)(implicit request: Request[_]): Future[Boolean] = {
      if (config.isKnownFactsCheckEnabled) {
        println("isJourneyLocked>>true")
        journeyDataRepository.getVRNInfo(journeyId, authInternalId).flatMap {
          case Some(vrn) =>
              journeySubmissionRepository.isBlockedJourney(vrn)
          case None =>
            errorLog(s"[JourneyValidateService][journeyIsLocked] - Journey data was not found for journey ID $journeyId")
            Future.successful(false)
        }
      } else {
        println("isJourneyLocked>>false")
        Future.successful(false)
      }
  }
}
