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

import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.claimvatenrolmentfrontend.auth.{AuthenticatedIdentifierAction, JourneyDataRetrievalAction}
import uk.gov.hmrc.claimvatenrolmentfrontend.config.AppConfig
import uk.gov.hmrc.claimvatenrolmentfrontend.forms.CaptureVatApplicationNumberForm
import uk.gov.hmrc.claimvatenrolmentfrontend.models.VatApplicationNumber
import uk.gov.hmrc.claimvatenrolmentfrontend.repositories.JourneyDataRepository.SubmittedVatApplicationNumberKey
import uk.gov.hmrc.claimvatenrolmentfrontend.services.{LockService, StoreKnownFactService}
import uk.gov.hmrc.claimvatenrolmentfrontend.views.html
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.LoggingUtil

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CaptureVatApplicationNumberController @Inject() (mcc: MessagesControllerComponents,
                                                       view: html.capture_vat_application_number_page,
                                                       storeKnownFactService: StoreKnownFactService,
                                                       identify: AuthenticatedIdentifierAction,
                                                       getData: JourneyDataRetrievalAction,
                                                       journeyValidateService: LockService)(implicit val config: AppConfig, ec: ExecutionContext)
    extends FrontendController(mcc)
    with I18nSupport
    with LoggingUtil {

  def show(journeyId: String): Action[AnyContent] = (identify andThen getData).async { implicit request =>
    journeyValidateService.continueIfJourneyIsNotLocked(request.journeyData.vatNumber, request.userId)(
      Ok(view(CaptureVatApplicationNumberForm.form, journeyId))
    )
  }

  def submit(journeyId: String): Action[AnyContent] = identify.async { implicit request =>
    CaptureVatApplicationNumberForm.form
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(view(formWithErrors, journeyId))),
        vatApplicationNumber =>
          storeKnownFactService
            .storeKnownFact[VatApplicationNumber](journeyId, vatApplicationNumber, SubmittedVatApplicationNumberKey, request.userId) map {
            case true => Redirect(routes.CheckYourAnswersController.show(journeyId).url)
            case _ =>
              errorLog(s"[CaptureVatApplicationNumberController][submit] - The date of Vat registration could not be updated for journey $journeyId")
              throw new InternalServerException(s"The date of Vat registration could not be updated for journey $journeyId")
          }
      )
  }
}
