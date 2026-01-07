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

import play.api.mvc.Result
import play.api.mvc.Results.Redirect
import uk.gov.hmrc.claimvatenrolmentfrontend.config.AppConfig
import uk.gov.hmrc.claimvatenrolmentfrontend.controllers.errorPages
import uk.gov.hmrc.claimvatenrolmentfrontend.repositories.UserLockRepository
import utils.LoggingUtil

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class LockService @Inject()(userLockRepository: UserLockRepository,
                            config: AppConfig)(implicit ec: ExecutionContext) extends LoggingUtil{

  def continueIfJourneyIsNotLocked(vrn: String, userId: String)
                                  (continue: Result): Future[Result] = {
    isJourneyLocked(vrn, userId) map {
      case true => Redirect(errorPages.routes.KnownFactsMismatchWithin24hrsController.show())
      case _ => continue
    }
  }

  def isJourneyLocked(vrn: String, userId: String): Future[Boolean] = {
    if (config.isKnownFactsCheckEnabled) {
      userLockRepository.isVrnOrUserLocked(vrn, userId)
    } else {
      Future.successful(false)
    }
  }
}
