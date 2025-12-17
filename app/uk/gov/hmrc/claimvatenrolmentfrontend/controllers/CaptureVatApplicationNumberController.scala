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
import uk.gov.hmrc.claimvatenrolmentfrontend.forms.CaptureVatApplicationNumberForm
import uk.gov.hmrc.claimvatenrolmentfrontend.services.{JourneyService, JourneyValidateService, StoreSubmittedVANService}
import uk.gov.hmrc.claimvatenrolmentfrontend.views.html.capture_vat_application_number_page
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.LoggingUtil

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CaptureVatApplicationNumberController @Inject()(mcc: MessagesControllerComponents,
                                                      view: capture_vat_application_number_page,
                                                      storeSubmittedVanService: StoreSubmittedVANService,
                                                      journeyService: JourneyService,
                                                      val authConnector: AuthConnector,
                                                      journeyValidateService: JourneyValidateService
                                                     )(implicit val config: AppConfig, ec: ExecutionContext) extends FrontendController(mcc) with AuthorisedFunctions with LoggingUtil {

  def show(journeyId: String): Action[AnyContent] = Action.async {
    implicit request =>
      authorised().retrieve(internalId) {
        case Some(authId) =>
          journeyService.retrieveJourneyConfig(journeyId, authId).flatMap {
            case Some(_) =>
              journeyValidateService.continueIfJourneyIsNotLocked(journeyId, authId)(
                Ok(view(CaptureVatApplicationNumberForm.form, routes.CaptureVatApplicationNumberController.submit(journeyId)))
              )
            case None =>
              errorLog(s"[CaptureVatApplicationNumberController][show] - Journey config could not be retrieved from the journeyConfigRepository for journey: $journeyId")
              Future.successful(Redirect(errorPages.routes.ServiceTimeoutController.show()))
          }
        case None =>
          errorLog(s"CaptureVatApplicationNumberController][show] - Internal ID could not be retrieved from Auth for journey: $journeyId")
          throw new InternalServerException(s"Internal ID could not be retrieved from Auth for journey: $journeyId")
      }
  }

  def submit(journeyId: String): Action[AnyContent] = Action.async {
    implicit request =>
      authorised().retrieve(internalId) {
        case Some(authId) =>
          CaptureVatApplicationNumberForm.form.bindFromRequest().fold(
            formWithErrors =>
              Future.successful(
                BadRequest(view(formWithErrors, routes.CaptureVatApplicationNumberController.submit(journeyId)))
              ),
            vatApplicationNumber =>
              storeSubmittedVanService.storeSubmittedVan(
                journeyId,
                vatApplicationNumber,
                authId
              ).map {
                matched =>
                  if (matched) {
                    Redirect(routes.CheckYourAnswersController.show(journeyId).url)
                  } else {
                    errorLog(s"[CaptureVatApplicationNumberController][submit] - The date of Vat registration could not be updated for journey $journeyId")
                    throw new InternalServerException(s"The date of Vat registration could not be updated for journey $journeyId")
                  }
              }
          )
        case None =>
          errorLog(s"[CaptureVatApplicationNumberController][submit] - Internal ID could not be retrieved from Auth for journey: $journeyId")
          throw new InternalServerException(s"Internal ID could not be retrieved from Auth for journey: $journeyId")
      }
  }
}

