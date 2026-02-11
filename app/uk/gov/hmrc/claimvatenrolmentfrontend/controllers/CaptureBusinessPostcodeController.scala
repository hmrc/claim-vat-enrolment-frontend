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
import uk.gov.hmrc.claimvatenrolmentfrontend.forms.CaptureBusinessPostcodeForm
import uk.gov.hmrc.claimvatenrolmentfrontend.services.{ClaimVatEnrolmentService, JourneyService, LockService, StoreBusinessPostcodeService}
import uk.gov.hmrc.claimvatenrolmentfrontend.views.html.capture_business_postcode_page
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.LoggingUtil

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CaptureBusinessPostcodeController @Inject()(mcc: MessagesControllerComponents,
                                                  view: capture_business_postcode_page,
                                                  cveService: ClaimVatEnrolmentService,
                                                  storeBusinessPostcodeService: StoreBusinessPostcodeService,
                                                  journeyService: JourneyService,
                                                  identify: AuthenticatedIdentifierAction,
                                                  getData: JourneyDataRetrievalAction,
                                                  lockService: LockService
                                                 )(implicit val config: AppConfig, ec: ExecutionContext)
  extends FrontendController(mcc) with I18nSupport with LoggingUtil{

  def show(journeyId: String): Action[AnyContent] = (identify andThen getData).async { implicit request =>
    lockService.continueIfJourneyIsNotLocked(request.journeyData.vatNumber, request.userId)(
      Ok(view(routes.CaptureBusinessPostcodeController.submit(journeyId), CaptureBusinessPostcodeForm.form, journeyId))
    )
  }

  def submit(journeyId: String): Action[AnyContent] = identify.async { implicit request =>
    CaptureBusinessPostcodeForm.form.bindFromRequest().fold(
      formWithErrors => {
        cveService.buildPostCodeFailureAuditEvent(formWithErrors)
        Future.successful(
          BadRequest(view(routes.CaptureBusinessPostcodeController.submit(journeyId), formWithErrors, journeyId))
        )},
      businessPostcode =>
        storeBusinessPostcodeService.storeBusinessPostcodeService(
          journeyId,
          businessPostcode,
          request.userId
        ).map {
          case true => Redirect(routes.CaptureSubmittedVatReturnController.show(journeyId).url)
          case _ =>
            errorLog(s"[CaptureBusinessPostcodeController][submit] - The VAT registration postcode could not be updated for journey $journeyId")
            throw new InternalServerException(s"The VAT registration postcode could not be updated for journey $journeyId")
        }
    )
  }

  def noPostcode(journeyId: String): Action[AnyContent] = identify.async { implicit request =>
    journeyService.removePostcodeField(journeyId, request.userId) map {
      case true => Redirect(routes.CaptureSubmittedVatReturnController.show(journeyId))
      case _ =>
        errorLog(s"[CaptureBusinessPostcodeController][noPostcode] - The post code field could not be removed for journey $journeyId")
        throw new InternalServerException(s"The post code field could not be removed for journey $journeyId")
    }
  }

}
