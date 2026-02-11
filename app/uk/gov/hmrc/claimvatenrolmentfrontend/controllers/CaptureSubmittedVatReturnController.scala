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
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import uk.gov.hmrc.claimvatenrolmentfrontend.auth.{AuthenticatedIdentifierAction, IdentifierRequest, JourneyDataRetrievalAction}
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
class CaptureSubmittedVatReturnController @Inject() (mcc: MessagesControllerComponents,
                                                     view: capture_submitted_vat_return_page,
                                                     storeSubmittedVatService: StoreSubmittedVatReturnService,
                                                     journeyService: JourneyService,
                                                     identify: AuthenticatedIdentifierAction,
                                                     getData: JourneyDataRetrievalAction,
                                                     journeyValidateService: LockService)(implicit val config: AppConfig, ec: ExecutionContext)
    extends FrontendController(mcc)
    with I18nSupport
    with LoggingUtil {

  def show(journeyId: String): Action[AnyContent] = (identify andThen getData).async { implicit request =>
    journeyValidateService.continueIfJourneyIsNotLocked(request.journeyData.vatNumber, request.userId)(
      Ok(view(journeyId, CaptureSubmittedVatReturnForm.form))
    )
  }

  def submit(journeyId: String): Action[AnyContent] = identify.async { implicit request =>
    def handleSuccess(userAnswerIsYes: Boolean): Future[Result] =
      for {
        storeUserAnswerIsSuccessful <- storeSubmittedVatService.storeStoreSubmittedVat(journeyId, userAnswerIsYes, request.userId)
        clearOtherDataIsSuccessful  <- journeyService.removeOppositePagesDataForGatewayQuestion(userAnswerIsYes, journeyId, request.userId)
        nextPage =
          if (userAnswerIsYes) routes.CaptureBox5FigureController.show(journeyId) else routes.CaptureVatApplicationNumberController.show(journeyId)
      } yield (storeUserAnswerIsSuccessful, clearOtherDataIsSuccessful) match {
        case (true, true) => Redirect(nextPage.url)
        case (false, _)   => throwError(s"Unable to store user's answer ($userAnswerIsYes) for CaptureSubmittedVatReturn page.", journeyId)
        case (_, false)   => throwError("Unable to clear data for alternative journey pages.", journeyId)
      }

    CaptureSubmittedVatReturnForm.form
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(view(journeyId, formWithErrors))),
        userAnswer => handleSuccess(userAnswer)
      )
  }

  private def throwError(errorMessage: String, journeyId: String)(implicit request: IdentifierRequest[_]): Nothing = {
    errorLog(s"[CaptureSubmittedVatReturnController][submit] - $errorMessage JourneyID: $journeyId")
    throw new InternalServerException(s"$errorMessage JourneyID: $journeyId")
  }

}
