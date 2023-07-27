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
import uk.gov.hmrc.claimvatenrolmentfrontend.forms.CaptureBusinessPostcodeForm
import uk.gov.hmrc.claimvatenrolmentfrontend.services.{ClaimVatEnrolmentService, JourneyService, StoreBusinessPostcodeService}
import uk.gov.hmrc.claimvatenrolmentfrontend.views.html.capture_business_postcode_page
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.LoggingUtil

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CaptureBusinessPostcodeController @Inject()(mcc: MessagesControllerComponents,
                                                  view: capture_business_postcode_page,
                                                  auditConnector: AuditConnector,
                                                  cveService: ClaimVatEnrolmentService,
                                                  storeBusinessPostcodeService: StoreBusinessPostcodeService,
                                                  journeyService: JourneyService,
                                                  val authConnector: AuthConnector,
                                                  errorHandler: ErrorHandler
                                                 )(implicit val config: AppConfig,
                                                   ec: ExecutionContext) extends FrontendController(mcc) with AuthorisedFunctions with LoggingUtil{

  def show(journeyId: String): Action[AnyContent] = Action.async {
    implicit request =>
      authorised().retrieve(internalId) {
        case Some(authId) =>
          journeyService.retrieveJourneyConfig(journeyId, authId).map {
            case Some(value) => Ok(view(routes.CaptureBusinessPostcodeController.submit(journeyId), CaptureBusinessPostcodeForm.form, journeyId))
            case None =>
              errorLog(s"[CaptureBusinessPostcodeController][show] - Journey config could not be retrieved from the journeyConfigRepository for journey: $journeyId")
              Redirect(errorPages.routes.ServiceTimeoutController.show())
          }
        case None =>
          errorLog(s"[CaptureBusinessPostcodeController][show] - Internal ID could not be retrieved from Auth for journey: $journeyId")
          throw new InternalServerException(s"Internal ID could not be retrieved from Auth for journey: $journeyId")
      }
  }

  def submit(journeyId: String): Action[AnyContent] = Action.async {
    implicit request =>
      authorised().retrieve(internalId) {
        case Some(authId) =>
          CaptureBusinessPostcodeForm.form.bindFromRequest.fold(
            formWithErrors => {
              cveService.buildPostCodeFailureAuditEvent(formWithErrors)
            Future.successful(
              BadRequest(view(routes.CaptureBusinessPostcodeController.submit(journeyId), formWithErrors, journeyId))
            )},
            businessPostcode =>
              storeBusinessPostcodeService.storeBusinessPostcodeService(
                journeyId,
                businessPostcode,
                authId
              ).map {
                matched =>
                  if(matched) {
                    Redirect(routes.CaptureSubmittedVatReturnController.show(journeyId).url)
                  } else {
                    errorLog(s"[CaptureBusinessPostcodeController][submit] - The VAT registration postcode could not be updated for journey $journeyId")
                    throw new InternalServerException(s"The VAT registration postcode could not be updated for journey $journeyId")
                  }
              }
          )
        case None =>
          errorLog(s"[CaptureBusinessPostcodeController][submit] - Internal ID could not be retrieved from Auth for journey: $journeyId")
          throw new InternalServerException(s"Internal ID could not be retrieved from Auth for journey: $journeyId")
      }
  }

  def noPostcode(journeyId: String): Action[AnyContent] = Action.async {
    implicit request =>
      authorised().retrieve(internalId) {
        case Some(authId) =>
          journeyService.removePostcodeField(journeyId, authId).map {
            matched => if (matched) {
              Redirect(routes.CaptureSubmittedVatReturnController.show(journeyId))
            } else {
              errorLog(s"[CaptureBusinessPostcodeController][noPostcode] - The post code field could not be removed for journey $journeyId")
              throw new InternalServerException(s"The post code field could not be removed for journey $journeyId")
            }
          }
        case None =>
          errorLog(s"[CaptureBusinessPostcodeController][noPostcode] - Internal ID could not be retrieved from Auth for journey: $journeyId")
          throw new InternalServerException(s"Internal ID could not be retrieved from Auth for journey: $journeyId")
      }
  }

}
