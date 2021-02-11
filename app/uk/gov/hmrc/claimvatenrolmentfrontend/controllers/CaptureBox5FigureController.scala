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

import javax.inject.{Inject, Singleton}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.claimvatenrolmentfrontend.config.AppConfig
import uk.gov.hmrc.claimvatenrolmentfrontend.forms.CaptureBox5FigureForm
import uk.gov.hmrc.claimvatenrolmentfrontend.views.html.capture_box5_figure_page
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import scala.concurrent.Future

@Singleton
class CaptureBox5FigureController @Inject()(mcc: MessagesControllerComponents,
                                            view: capture_box5_figure_page
                                           )(implicit val config: AppConfig) extends FrontendController(mcc) {

  def show: Action[AnyContent] = Action.async {
    implicit request =>
      Future.successful(Ok(view(CaptureBox5FigureForm.form, routes.CaptureBox5FigureController.submit())))
  }

  def submit: Action[AnyContent] = Action.async {
    implicit request =>
      CaptureBox5FigureForm.form.bindFromRequest.fold(
        formWithErrors => Future.successful(
          BadRequest(view(formWithErrors, routes.CaptureBox5FigureController.submit()))
        ),
        box5Figure =>
          Future.successful(Redirect(routes.CaptureLastMonthSubmittedController.show().url))
      )
  }
}
