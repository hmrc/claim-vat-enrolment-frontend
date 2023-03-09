/*
 * Copyright 2023 HM Revenue & Customs
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
import uk.gov.hmrc.claimvatenrolmentfrontend.config.{AppConfig, ErrorHandler}
import uk.gov.hmrc.claimvatenrolmentfrontend.forms.VatRegistrationDateForm.vatRegistrationDateForm
import uk.gov.hmrc.claimvatenrolmentfrontend.services.{JourneyService, StoreVatRegistrationDateService}
import uk.gov.hmrc.claimvatenrolmentfrontend.views.html.capture_vat_registration_date_page
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.LoggingUtil

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CaptureVatRegistrationDateController @Inject()(mcc: MessagesControllerComponents,
                                                     view: capture_vat_registration_date_page,
                                                     storeVatRegistrationDateService: StoreVatRegistrationDateService,
                                                     journeyService: JourneyService,
                                                     val authConnector: AuthConnector,
                                                     errorHandler: ErrorHandler
                                                    )(implicit ec: ExecutionContext,
                                                      appConfig: AppConfig) extends FrontendController(mcc) with AuthorisedFunctions with LoggingUtil{

  def show(journeyId: String): Action[AnyContent] = Action.async {
    implicit request =>
      authorised().retrieve(internalId) {
        case Some(authId) =>
          journeyService.retrieveJourneyConfig(journeyId, authId).map {
            case Some(value) => Ok(view(vatRegistrationDateForm, routes.CaptureVatRegistrationDateController.submit(journeyId)))
            case None =>
              errorLog(s"[CaptureVatRegistrationDateController][show] - Journey config could not be retrieved from the journeyConfigRepository for journey: $journeyId")
              BadRequest(errorHandler.internalServerErrorTemplate)
          }
        case None =>
          errorLog(s"[CaptureVatRegistrationDateController][show] - Internal ID could not be retrieved from Auth for journey: $journeyId")
          throw new InternalServerException(s"Internal ID could not be retrieved from Auth for journey: $journeyId")
      }
  }


  def submit(journeyId: String): Action[AnyContent] = Action.async {
    implicit request =>
      authorised().retrieve(internalId) {
        case Some(authId) =>
          vatRegistrationDateForm.bindFromRequest.fold(
            formWithErrors =>
              Future.successful(
                BadRequest(view(formWithErrors, routes.CaptureVatRegistrationDateController.submit(journeyId)))
              ),
            vatRegistrationDate =>
              storeVatRegistrationDateService.storeVatRegistrationDate(
                journeyId,
                vatRegistrationDate,
                authId
              ).map {
                    matched => if (matched) {
                      Redirect(routes.CaptureBusinessPostcodeController.show(journeyId).url)
                    } else {
                      errorLog(s"[CaptureVatRegistrationDateController][submit] - The date of Vat registration could not be updated for journey $journeyId")
                      throw new InternalServerException(s"The date of Vat registration could not be updated for journey $journeyId")
                    }
              }
          )
        case None =>
          errorLog(s"[CaptureVatRegistrationDateController][submit] - Internal ID could not be retrieved from Auth for journey: $journeyId")
          throw new InternalServerException(s"Internal ID could not be retrieved from Auth for journey: $journeyId")
      }
  }

}
