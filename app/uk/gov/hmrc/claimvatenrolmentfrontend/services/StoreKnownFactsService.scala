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

package uk.gov.hmrc.claimvatenrolmentfrontend.services

import play.api.libs.json.{Json, Writes}
import play.api.mvc.Results.InternalServerError
import play.api.mvc.{Request, Result}
import uk.gov.hmrc.claimvatenrolmentfrontend.config.ErrorHandler
import uk.gov.hmrc.claimvatenrolmentfrontend.repositories.JourneyDataRepository
import utils.LinkLogger.errorLog

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class StoreKnownFactsService @Inject() (journeyDataRepository: JourneyDataRepository, errorHandler: ErrorHandler) {

  def storeKnownFactAnswerOrHandleFailure[A](value: A, pageKey: String, journeyId: String, authInternalId: String)(
      continueOnSuccess: Future[Result])(implicit writes: Writes[A], request: Request[_], ec: ExecutionContext): Future[Result] =
    journeyDataRepository
      .updateJourneyData(journeyId = journeyId, dataKey = pageKey, data = Json.toJson(value), authInternalId = authInternalId)
      .flatMap(if (_) continueOnSuccess else Future.successful(logFailureAndRedirectToErrorPage(value.toString, pageKey, journeyId)))

  private def logFailureAndRedirectToErrorPage(value: String, pageKey: String, journeyId: String)(implicit request: Request[_]): Result = {
    errorLog(s"[StoreKnownFactsService] - Unable to store user's answer ($value) for $pageKey page. Journey ID: $journeyId")
    InternalServerError(errorHandler.internalServerErrorTemplate)
  }

}
