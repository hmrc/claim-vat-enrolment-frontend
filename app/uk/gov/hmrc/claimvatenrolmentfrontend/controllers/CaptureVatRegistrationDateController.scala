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
import uk.gov.hmrc.claimvatenrolmentfrontend.forms.VatRegistrationDateForm.vatRegistrationDateForm
import uk.gov.hmrc.claimvatenrolmentfrontend.services.{LockService, StoreVatRegistrationDateService}
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
                                                     identify: AuthenticatedIdentifierAction,
                                                     getData: JourneyDataRetrievalAction,
                                                     journeyValidateService: LockService
                                                    )(implicit ec: ExecutionContext, appConfig: AppConfig)
  extends FrontendController(mcc) with I18nSupport with LoggingUtil{

  def show(journeyId: String): Action[AnyContent] = (identify andThen getData).async { implicit request =>
    journeyValidateService.continueIfJourneyIsNotLocked(request.journeyData.vatNumber, request.userId)(
      Ok(view(vatRegistrationDateForm, routes.CaptureVatRegistrationDateController.submit(journeyId)))
    )
  }

  def submit(journeyId: String): Action[AnyContent] = identify.async { implicit request =>
    vatRegistrationDateForm.bindFromRequest().fold(
      formWithErrors =>
        Future.successful(BadRequest(view(formWithErrors, routes.CaptureVatRegistrationDateController.submit(journeyId)))),
      vatRegistrationDate =>
        storeVatRegistrationDateService.storeVatRegistrationDate(
          journeyId,
          Some(vatRegistrationDate),
          request.userId
        ) map {
          case true => Redirect(routes.CaptureBusinessPostcodeController.show(journeyId).url)
          case _ =>
            errorLog(s"[CaptureVatRegistrationDateController][submit] - The date of Vat registration could not be updated for journey $journeyId")
            throw new InternalServerException(s"The date of Vat registration could not be updated for journey $journeyId")
        }
    )
  }
}
