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
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.internalId
import uk.gov.hmrc.auth.core.{AuthConnector, AuthorisedFunctions}
import uk.gov.hmrc.claimvatenrolmentfrontend.config.AppConfig
import uk.gov.hmrc.claimvatenrolmentfrontend.forms.CaptureBusinessPostcodeForm
import uk.gov.hmrc.claimvatenrolmentfrontend.services.{JourneyService, StoreBusinessPostcodeService}
import uk.gov.hmrc.claimvatenrolmentfrontend.views.html.capture_business_postcode_page
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class CaptureBusinessPostcodeController @Inject()(mcc: MessagesControllerComponents,
                                                  view: capture_business_postcode_page,
                                                  storeBusinessPostcodeService: StoreBusinessPostcodeService,
                                                  journeyService: JourneyService,
                                                  val authConnector: AuthConnector
                                                 )(implicit val config: AppConfig) extends FrontendController(mcc) with AuthorisedFunctions {

  def show(journeyId: String): Action[AnyContent] = Action.async {
    implicit request =>
      authorised() {
        Future.successful(
          Ok(view(routes.CaptureBusinessPostcodeController.submit(journeyId), CaptureBusinessPostcodeForm.form, journeyId)))
      }
  }

  def submit(journeyId: String): Action[AnyContent] = Action.async {
    implicit request =>
      authorised().retrieve(internalId) {
        case Some(authId) =>
          CaptureBusinessPostcodeForm.form.bindFromRequest.fold(
            formWithErrors => Future.successful(
              BadRequest(view(routes.CaptureBusinessPostcodeController.submit(journeyId), formWithErrors, journeyId))
            ),
            businessPostcode =>
              storeBusinessPostcodeService.storeBusinessPostcodeService(
                journeyId,
                businessPostcode,
                authId
              ).map {
                _ => Redirect(routes.CaptureSubmittedVatReturnController.show(journeyId).url)
              }
          )
        case None =>
          Future.successful(Unauthorized)
      }
  }

  def noPostcode(journeyId: String): Action[AnyContent] = Action.async {
    implicit request =>
      authorised().retrieve(internalId) {
        case Some(authId) =>
          journeyService.removePostcodeField(journeyId, authId).map {
            _ => Redirect(routes.CaptureSubmittedVatReturnController.show(journeyId))
          }
        case None =>
          Future.successful(Unauthorized)
      }
  }

}
