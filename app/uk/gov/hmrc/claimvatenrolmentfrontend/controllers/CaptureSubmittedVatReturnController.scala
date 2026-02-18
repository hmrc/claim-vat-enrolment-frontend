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
import uk.gov.hmrc.claimvatenrolmentfrontend.forms.CaptureSubmittedVatReturnForm
import uk.gov.hmrc.claimvatenrolmentfrontend.services.{JourneyService, LockService, StoreSubmittedVatReturnService}
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
                                                    identify: AuthenticatedIdentifierAction,
                                                    getData: JourneyDataRetrievalAction,
                                                    lockService: LockService
                                                   )(implicit val config: AppConfig, ec: ExecutionContext)
  extends FrontendController(mcc) with I18nSupport with LoggingUtil {

  def show(journeyId: String): Action[AnyContent] = (identify andThen getData).async { implicit request =>
    lockService.continueIfJourneyIsNotLocked(request.journeyData.vatNumber, request.userId)(
      Ok(view(routes.CaptureSubmittedVatReturnController.submit(journeyId), CaptureSubmittedVatReturnForm.form))
    )
  }

  def submit(journeyId: String): Action[AnyContent] = identify.async { implicit request =>
    CaptureSubmittedVatReturnForm.form.bindFromRequest().fold(
      formWithErrors => Future.successful(
        BadRequest(view(routes.CaptureSubmittedVatReturnController.submit(journeyId), formWithErrors))
      ),
      submittedReturn =>
        storeSubmittedVatService.storeStoreSubmittedVat(
          journeyId,
          submittedReturn,
          request.userId
        ) flatMap {
          case true =>
              if (submittedReturn) {
                journeyService.removeAdditionalFormBundlRefField(journeyId, request.userId).map {
                  case true =>
                    Redirect(routes.CaptureBox5FigureController.show(journeyId).url)
                  case _ =>
                    errorLog(s"[CaptureSubmittedVatReturnController][submit] - The formbundleRef field could not be removed for journey $journeyId")
                    throw new InternalServerException(s"The formbundleRef field could not be removed for journey $journeyId")
                }
              } else {
                journeyService.removeAdditionalVatReturnFields(journeyId, request.userId).map {
                  case true if config.knownFactsCheckFlag && config.knownFactsCheckWithVanFlag =>
                      Redirect(routes.CaptureVatApplicationNumberController.show(journeyId).url)
                  case true =>
                      Redirect(routes.CheckYourAnswersController.show(journeyId).url)
                  case _ =>
                    errorLog(s"[CaptureSubmittedVatReturnController][submit] - The additional Vat return fields could not be removed for journey $journeyId")
                    throw new InternalServerException(s"The additional Vat return fields could not be removed for journey $journeyId")
                  }
              }
          case _ =>
            errorLog(s"[CaptureSubmittedVatReturnController][submit] - The Vat return submitted flag could not be updated for journey $journeyId")
            throw new InternalServerException(s"The Vat return submitted flag could not be updated for journey $journeyId")
        }
    )
  }
}

