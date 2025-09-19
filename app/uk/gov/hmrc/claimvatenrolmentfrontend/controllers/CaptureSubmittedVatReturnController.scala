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
import uk.gov.hmrc.claimvatenrolmentfrontend.forms.CaptureSubmittedVatReturnForm
import uk.gov.hmrc.claimvatenrolmentfrontend.services.{JourneyService, StoreSubmittedVatReturnService}
import uk.gov.hmrc.claimvatenrolmentfrontend.views.html.capture_submitted_vat_return_page
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.LoggingUtil

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CaptureSubmittedVatReturnController @Inject()(mcc: MessagesControllerComponents,
                                                    view: capture_submitted_vat_return_page,
                                                    storeSubmittedVatService: StoreSubmittedVatReturnService,
                                                    journeyService: JourneyService,
                                                    val authConnector: AuthConnector,
                                                    errorHandler: ErrorHandler
                                                   )(implicit val config: AppConfig, ec: ExecutionContext) extends FrontendController(mcc) with AuthorisedFunctions with LoggingUtil {

  def show(journeyId: String): Action[AnyContent] = Action.async {
    implicit request =>
      authorised().retrieve(internalId) {
        case Some(authId) =>
          journeyService.retrieveJourneyConfig(journeyId, authId).map {
               case Some(value) => Ok(view(routes.CaptureSubmittedVatReturnController.submit(journeyId), CaptureSubmittedVatReturnForm.form))
               case None =>
                 errorLog(s"[CaptureSubmittedVatReturnController][show] - Journey config could not be retrieved from the journeyConfigRepository for journey: $journeyId")
                 Redirect(errorPages.routes.ServiceTimeoutController.show())
          }
        case None =>
          errorLog(s"CaptureSubmittedVatReturnController][show] - Internal ID could not be retrieved from Auth for journey: $journeyId")
          throw new InternalServerException(s"Internal ID could not be retrieved from Auth for journey: $journeyId")
      }
  }

  def submit(journeyId: String): Action[AnyContent] = Action.async {
    implicit request =>
      authorised().retrieve(internalId) {
        case Some(authId) =>
          CaptureSubmittedVatReturnForm.form.bindFromRequest().fold(
            formWithErrors => Future.successful(
              BadRequest(view(routes.CaptureSubmittedVatReturnController.submit(journeyId), formWithErrors))
            ),
            submittedReturn =>
              storeSubmittedVatService.storeStoreSubmittedVat(
                journeyId,
                submittedReturn,
                authId
              ).flatMap {
                storeMatched =>
                  if(storeMatched) {
                    if (submittedReturn) {
                      Future.successful(Redirect(routes.CaptureBox5FigureController.show(journeyId).url))
                    } else {
                      journeyService.removeAdditionalVatReturnFields(journeyId, authId).map {
                        removeMatched => if (removeMatched) {
                          Redirect(routes.CheckYourAnswersController.show(journeyId).url)
                        } else {
                          errorLog(s"[CaptureSubmittedVatReturnController][submit] - The additional Vat return fields could not be removed for journey $journeyId")
                          throw new InternalServerException(s"The additional Vat return fields could not be removed for journey $journeyId")
                        }
                      }
                    }
                  } else {
                    errorLog(s"[CaptureSubmittedVatReturnController][submit] - The Vat return submitted flag could not be updated for journey $journeyId")
                    throw new InternalServerException(s"The Vat return submitted flag could not be updated for journey $journeyId")
                  }
              }
          )
        case None =>
          errorLog(s"[CaptureSubmittedVatReturnController][submit] - Internal ID could not be retrieved from Auth for journey: $journeyId")
          throw new InternalServerException(s"Internal ID could not be retrieved from Auth for journey: $journeyId")
      }
  }
}

