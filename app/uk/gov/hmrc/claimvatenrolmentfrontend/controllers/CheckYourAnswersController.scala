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

package uk.gov.hmrc.claimvatenrolmentfrontend.controllers

import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.claimvatenrolmentfrontend.auth.{AuthenticatedIdentifierAction, JourneyDataRetrievalAction}
import uk.gov.hmrc.claimvatenrolmentfrontend.config.AppConfig
import uk.gov.hmrc.claimvatenrolmentfrontend.services.ClaimVatEnrolmentService._
import uk.gov.hmrc.claimvatenrolmentfrontend.services.{ClaimVatEnrolmentService, LockService}
import uk.gov.hmrc.claimvatenrolmentfrontend.views.html.check_your_answers_page
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.LoggingUtil

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class CheckYourAnswersController @Inject()(mcc: MessagesControllerComponents,
                                           view: check_your_answers_page,
                                           claimVatEnrolmentService: ClaimVatEnrolmentService,
                                           identify: AuthenticatedIdentifierAction,
                                           getData: JourneyDataRetrievalAction,
                                           journeyValidateService: LockService
                                          )(implicit appConfig: AppConfig, ec: ExecutionContext)
  extends FrontendController(mcc) with I18nSupport with LoggingUtil {

  def show(journeyId: String): Action[AnyContent] = (identify andThen getData).async { implicit request =>
    journeyValidateService.continueIfJourneyIsNotLocked(request.journeyData.vatNumber, request.userId)(
      Ok(view(routes.CheckYourAnswersController.submit(journeyId), journeyId, request.journeyData))
    )
  }

  def submit(journeyId: String): Action[AnyContent] = (identify andThen getData).async { implicit request =>
    claimVatEnrolmentService.claimVatEnrolment(request.credId, request.groupId, request.userId, journeyId) map {
      case Right(_) =>
        Redirect(routes.SignUpCompleteController.signUpComplete(journeyId))
      case Left(KnownFactsMismatchNotLocked) =>
        Redirect(errorPages.routes.KnownFactsMismatchController.show())
      case Left(KnownFactsMismatchLocked) =>
        Redirect(errorPages.routes.ThirdAttemptLockoutController.show())
      case Left(EnrolmentAlreadyAllocated) =>
        Redirect(errorPages.routes.EnrolmentAlreadyAllocatedController.show())
      case Left(CannotAssignMultipleMtdvatEnrolments) =>
        Redirect(errorPages.routes.UnmatchedUserErrorController.show())
      case Left(JourneyConfigFailure) =>
        errorLog(s"[CheckYourAnswersController][submit] - Journey config could not be retrieved from the journeyConfigRepository for journey: $journeyId")
        Redirect(errorPages.routes.ServiceTimeoutController.show())
      case Left(JourneyDataFailure) =>
        errorLog(s"[CheckYourAnswersController][submit] - Journey data could not be retrieved from the journeyDataRepository for journey: $journeyId")
        Redirect(errorPages.routes.ServiceTimeoutController.show())
    }
  }

}
