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

import play.api.libs.json.JsResultException
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.{credentials, groupIdentifier, internalId}
import uk.gov.hmrc.auth.core.retrieve.{Credentials, ~}
import uk.gov.hmrc.auth.core.{AuthConnector, AuthorisedFunctions}
import uk.gov.hmrc.claimvatenrolmentfrontend.config.AppConfig
import uk.gov.hmrc.claimvatenrolmentfrontend.services.ClaimVatEnrolmentService._
import uk.gov.hmrc.claimvatenrolmentfrontend.services.{ClaimVatEnrolmentService, JourneyService, JourneyValidateService}
import uk.gov.hmrc.claimvatenrolmentfrontend.views.html.check_your_answers_page
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.LoggingUtil

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CheckYourAnswersController @Inject()(mcc: MessagesControllerComponents,
                                           view: check_your_answers_page,
                                           journeyService: JourneyService,
                                           claimVatEnrolmentService: ClaimVatEnrolmentService,
                                           val authConnector: AuthConnector,
                                           journeyValidateService: JourneyValidateService
                                          )(implicit appConfig: AppConfig,
                                            ec: ExecutionContext) extends FrontendController(mcc) with AuthorisedFunctions with LoggingUtil {

  def show(journeyId: String): Action[AnyContent] = Action.async {
    implicit request =>
      authorised().retrieve(internalId) {
        case Some(authId) =>
          journeyService.retrieveJourneyData(journeyId, authId).flatMap {
            case Some(journeyData) =>
              journeyValidateService.continueIfJourneyIsNotLocked(journeyId, authId)(
                Ok(view(routes.CheckYourAnswersController.submit(journeyId), journeyId, journeyData))
              )
            case None =>
              errorLog(s"[CheckYourAnswersController][show] - Journey data could not be retrieved from the journeyDataRepository for journey: $journeyId")
              Future.successful(Redirect(errorPages.routes.ServiceTimeoutController.show()))
          }.recover {
            case _: JsResultException =>
              warnLog(s"[CheckYourAnswersController][show] - A JsResultException was thrown while retrieving the journey data from the journeyDataRepository for journey: $journeyId")
              Redirect(routes.CaptureVatRegistrationDateController.show(journeyId))
          }
        case None =>
          errorLog(s"[CheckYourAnswersController][show] - Internal ID could not be retrieved from Auth for journey: $journeyId")
          throw new InternalServerException(s"Internal ID could not be retrieved from Auth for journey: $journeyId")
      }
  }

  def submit(journeyId: String): Action[AnyContent] = Action.async {
    implicit request =>
      authorised().retrieve(credentials and groupIdentifier and internalId) {
        case Some(Credentials(credentialId, "GovernmentGateway")) ~ Some(groupId) ~ Some(internalId) =>
          claimVatEnrolmentService.claimVatEnrolment(credentialId, groupId, internalId, journeyId).map {
            case Right(_) =>
              Redirect(routes.SignUpCompleteController.signUpComplete(journeyId))
            case Left(KnownFactsMismatchLevel1) =>
              Redirect(errorPages.routes.KnownFactsMismatchController.show())
            case Left(KnownFactsMismatchLevel2) =>
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
        case _ =>
          errorLog(s"[CheckYourAnswersController][submit] - Internal ID could not be retrieved from Auth for journey: $journeyId")
          throw new InternalServerException(s"Internal ID could not be retrieved from Auth for journey: $journeyId")
      }
  }

}
