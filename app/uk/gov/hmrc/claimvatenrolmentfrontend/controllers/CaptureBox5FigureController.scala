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

package uk.gov.hmrc.claimvatenrolmentfrontend.controllers

import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.claimvatenrolmentfrontend.auth.{AuthenticatedIdentifierAction, JourneyDataRetrievalAction}
import uk.gov.hmrc.claimvatenrolmentfrontend.config.AppConfig
import uk.gov.hmrc.claimvatenrolmentfrontend.forms.CaptureBox5FigureForm
import uk.gov.hmrc.claimvatenrolmentfrontend.services.{LockService, StoreBox5FigureService}
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
                                            identify: AuthenticatedIdentifierAction,
                                            getData: JourneyDataRetrievalAction,
                                            lockService: LockService
                                           )(implicit val config: AppConfig, ec: ExecutionContext)
  extends FrontendController(mcc) with I18nSupport with LoggingUtil {

  def show(journeyId: String): Action[AnyContent] = (identify andThen getData).async { implicit request =>
    lockService.continueIfJourneyIsNotLocked(request.journeyData.vatNumber, request.userId)(
      Ok(view(CaptureBox5FigureForm.form, routes.CaptureBox5FigureController.submit(journeyId)))
    )
  }

  def submit(journeyId: String): Action[AnyContent] = identify.async { implicit request =>
    CaptureBox5FigureForm.form.bindFromRequest().fold(
      formWithErrors =>
        Future.successful(BadRequest(view(formWithErrors, routes.CaptureBox5FigureController.submit(journeyId)))),
      box5Figure =>
        storeBox5FigureService.storeBox5Figure(journeyId, box5Figure, request.userId) map {
          case true => Redirect(routes.CaptureLastMonthSubmittedController.show(journeyId).url)
          case _ =>
            errorLog(s"[CaptureBox5FigureController][submit] - The box 5 figure could not be updated for journey $journeyId")
            throw new InternalServerException(s"The box 5 figure could not be updated for journey $journeyId")
        }
    )
  }
}
