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
import uk.gov.hmrc.claimvatenrolmentfrontend.forms.CaptureLastMonthSubmittedForm
import uk.gov.hmrc.claimvatenrolmentfrontend.services.{LockService, StoreLastMonthSubmittedService}
import uk.gov.hmrc.claimvatenrolmentfrontend.views.html.capture_last_month_submitted_page
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.LoggingUtil

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CaptureLastMonthSubmittedController @Inject()(mcc: MessagesControllerComponents,
                                                    view: capture_last_month_submitted_page,
                                                    storeLastMonthSubmittedService: StoreLastMonthSubmittedService,
                                                    identify: AuthenticatedIdentifierAction,
                                                    getData: JourneyDataRetrievalAction,
                                                    journeyValidateService: LockService
                                                   )(implicit val config: AppConfig, ec: ExecutionContext)
  extends FrontendController(mcc) with I18nSupport with LoggingUtil {

  def show(journeyId: String): Action[AnyContent] = (identify andThen getData).async { implicit request =>
    journeyValidateService.continueIfJourneyIsNotLocked(request.journeyData.vatNumber, request.userId)(
      Ok(view(routes.CaptureLastMonthSubmittedController.submit(journeyId), CaptureLastMonthSubmittedForm.form))
    )
  }

  def submit(journeyId: String): Action[AnyContent] = identify.async { implicit request =>
    CaptureLastMonthSubmittedForm.form.bindFromRequest().fold(
      formWithErrors => Future.successful(
        BadRequest(view(routes.CaptureLastMonthSubmittedController.submit(journeyId), formWithErrors))
      ),
      lastMonthSubmitted =>
        storeLastMonthSubmittedService.storeLastMonthSubmitted(journeyId, lastMonthSubmitted, request.userId) map {
          case true => Redirect(routes.CheckYourAnswersController.show(journeyId).url)
          case _ =>
            errorLog(s"The last month a Vat return was submitted could not be updated for journey $journeyId")
            throw new InternalServerException(s"The last month a Vat return was submitted could not be updated for journey $journeyId")
        }
    )
  }
}
