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

import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.internalId
import uk.gov.hmrc.auth.core.{AuthConnector, AuthorisedFunctions}
import uk.gov.hmrc.claimvatenrolmentfrontend.config.AppConfig
import uk.gov.hmrc.claimvatenrolmentfrontend.forms.CaptureBox5FigureForm
import uk.gov.hmrc.claimvatenrolmentfrontend.services.{JourneyService, JourneyValidateService, StoreBox5FigureService}
import uk.gov.hmrc.claimvatenrolmentfrontend.views.html.capture_box5_figure_page
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.LoggingUtil

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CaptureBox5FigureController @Inject()(mcc: MessagesControllerComponents,
                                            view: capture_box5_figure_page,
                                            storeBox5FigureService: StoreBox5FigureService,
                                            journeyService: JourneyService,
                                            val authConnector: AuthConnector,
                                            journeyValidateService: JourneyValidateService
                                           )(implicit val config: AppConfig,
                                             ec: ExecutionContext) extends FrontendController(mcc) with AuthorisedFunctions with LoggingUtil {

  def show(journeyId: String): Action[AnyContent] = Action.async {
    implicit request =>
      authorised().retrieve(internalId) {
        case Some(authId) =>
          journeyService.retrieveJourneyConfig(journeyId, authId).flatMap {
              case Some(_) =>
                journeyValidateService.continueIfJourneyIsNotLocked(journeyId, authId)(
                  Ok(view(CaptureBox5FigureForm.form, routes.CaptureBox5FigureController.submit(journeyId)))
                )
              case None =>
                errorLog(s"[CaptureBox5FigureController][show] - Journey config could not be retrieved from the journeyConfigRepository for journey: $journeyId")
                Future.successful(Redirect(errorPages.routes.ServiceTimeoutController.show()))
            }
        case None =>
          errorLog(s"[CaptureBox5FigureController][show] - Internal ID could not be retrieved from Auth for journey: $journeyId")
          throw new InternalServerException(s"Internal ID could not be retrieved from Auth for journey: $journeyId")
      }
  }

  def submit(journeyId: String): Action[AnyContent] = Action.async {
    implicit request =>
      authorised().retrieve(internalId) {
        case Some(authId) =>
          CaptureBox5FigureForm.form.bindFromRequest().fold(
            formWithErrors =>
              Future.successful(
                BadRequest(view(formWithErrors, routes.CaptureBox5FigureController.submit(journeyId)))
              ),
            box5Figure =>
              storeBox5FigureService.storeBox5Figure(journeyId, box5Figure, authId).map {
                    matched => if (matched) {
                      Redirect(routes.CaptureLastMonthSubmittedController.show(journeyId).url)
                    } else {
                      errorLog(s"[CaptureBox5FigureController][submit] - The box 5 figure could not be updated for journey $journeyId")
                      throw new InternalServerException(s"The box 5 figure could not be updated for journey $journeyId")
                    }
              }
          )
        case None =>
          errorLog(s"[CaptureBox5FigureController][submit] - Internal ID could not be retrieved from Auth for journey: $journeyId")
          throw new InternalServerException(s"Internal ID could not be retrieved from Auth for journey: $journeyId")
      }
  }
}
