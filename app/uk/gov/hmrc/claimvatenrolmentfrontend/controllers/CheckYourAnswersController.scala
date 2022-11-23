/*
 * Copyright 2022 HM Revenue & Customs
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
import uk.gov.hmrc.claimvatenrolmentfrontend.services.ClaimVatEnrolmentService.{CannotAssignMultipleMtdvatEnrolments, EnrolmentAlreadyAllocated, KnownFactsMismatch}
import uk.gov.hmrc.claimvatenrolmentfrontend.services.{ClaimVatEnrolmentService, JourneyService}
import uk.gov.hmrc.claimvatenrolmentfrontend.views.html.check_your_answers_page
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class CheckYourAnswersController @Inject()(mcc: MessagesControllerComponents,
                                           view: check_your_answers_page,
                                           journeyService: JourneyService,
                                           claimVatEnrolmentService: ClaimVatEnrolmentService,
                                           val authConnector: AuthConnector
                                          )(implicit appConfig: AppConfig,
                                            ec: ExecutionContext) extends FrontendController(mcc) with AuthorisedFunctions {

  def show(journeyId: String): Action[AnyContent] = Action.async {
    implicit request =>
      authorised().retrieve(internalId) {
        case Some(authId) =>
          journeyService.retrieveJourneyData(journeyId, authId).map {
            journeyData =>
              Ok(view(routes.CheckYourAnswersController.submit(journeyId), journeyId, journeyData))
          }.recover {
            case _: JsResultException => Redirect(routes.CaptureVatRegistrationDateController.show(journeyId))
          }
        case None => throw new InternalServerException("Internal ID could not be retrieved from Auth")
      }
  }

  def submit(journeyId: String): Action[AnyContent] = Action.async {
    implicit request =>
      authorised().retrieve(credentials and groupIdentifier and internalId) {
        case Some(Credentials(credentialId, "GovernmentGateway")) ~ Some(groupId) ~ Some(internalId) =>
          claimVatEnrolmentService.claimVatEnrolment(credentialId, groupId, internalId, journeyId).map {
            case Right(_) =>
              Redirect(routes.SignUpCompleteController.signUpComplete(journeyId))
            case Left(KnownFactsMismatch) =>
              Redirect(errorPages.routes.KnownFactsMismatchController.show())
            case Left(EnrolmentAlreadyAllocated) =>
              Redirect(errorPages.routes.EnrolmentAlreadyAllocatedController.show())
            case Left(CannotAssignMultipleMtdvatEnrolments) =>
              Redirect(errorPages.routes.UnmatchedUserErrorController.show())
          }
        case _ =>
          throw new InternalServerException("Internal ID could not be retrieved from Auth")
      }
  }

}
