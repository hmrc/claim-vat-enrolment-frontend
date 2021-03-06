/*
 * Copyright 2021 HM Revenue & Customs
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

import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.internalId
import uk.gov.hmrc.auth.core.{AuthConnector, AuthorisedFunctions}
import uk.gov.hmrc.claimvatenrolmentfrontend.config.AppConfig
import uk.gov.hmrc.claimvatenrolmentfrontend.forms.CaptureLastMonthSubmittedForm
import uk.gov.hmrc.claimvatenrolmentfrontend.services.{JourneyService, StoreLastMonthSubmittedService}
import uk.gov.hmrc.claimvatenrolmentfrontend.views.html.capture_last_month_submitted_page
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CaptureLastMonthSubmittedController @Inject()(mcc: MessagesControllerComponents,
                                                    view: capture_last_month_submitted_page,
                                                    storeLastMonthSubmittedService: StoreLastMonthSubmittedService,
                                                    journeyService: JourneyService,
                                                    val authConnector: AuthConnector
                                                   )(implicit val config: AppConfig,
                                                     ec: ExecutionContext) extends FrontendController(mcc) with AuthorisedFunctions {

  def show(journeyId: String): Action[AnyContent] = Action.async {
    implicit request =>
      authorised().retrieve(internalId) {
        case Some(authId) =>
          journeyService.retrieveJourneyConfig(journeyId, authId).map {
            _ => Ok(view(routes.CaptureLastMonthSubmittedController.submit(journeyId), CaptureLastMonthSubmittedForm.form))
          }
        case None =>
          throw new InternalServerException("Internal ID could not be retrieved from Auth")
      }
  }

  def submit(journeyId: String): Action[AnyContent] = Action.async {
    implicit request =>
      authorised().retrieve(internalId) {
        case Some(authId) =>
          CaptureLastMonthSubmittedForm.form.bindFromRequest.fold(
            formWithErrors => Future.successful(
              BadRequest(view(routes.CaptureLastMonthSubmittedController.submit(journeyId), formWithErrors))
            ),
            lastMonthSubmitted =>
              storeLastMonthSubmittedService.storeLastMonthSubmitted(journeyId, lastMonthSubmitted, authId).map {
                _ => Redirect(routes.CheckYourAnswersController.show(journeyId).url)
              }
          )
        case None =>
          throw new InternalServerException("Internal ID could not be retrieved from Auth")
      }
  }
}
