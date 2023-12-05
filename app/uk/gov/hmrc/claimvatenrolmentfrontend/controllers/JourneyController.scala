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
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.{credentialRole, internalId}
import uk.gov.hmrc.auth.core.retrieve.~
import uk.gov.hmrc.auth.core.{AuthConnector, AuthorisedFunctions, User}
import uk.gov.hmrc.claimvatenrolmentfrontend.controllers.errorPages.{routes => errorRoutes}
import uk.gov.hmrc.claimvatenrolmentfrontend.models.JourneyConfig
import uk.gov.hmrc.claimvatenrolmentfrontend.services.JourneyService
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.LoggingUtil

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class JourneyController @Inject()(journeyService: JourneyService,
                                  mcc: MessagesControllerComponents,
                                  val authConnector: AuthConnector
                                 )(implicit ec: ExecutionContext) extends FrontendController(mcc) with AuthorisedFunctions with LoggingUtil {

  def createJourney(vatNumber: String, continueUrl: String): Action[AnyContent] = Action.async {
    implicit request =>
      authorised().retrieve(internalId and credentialRole) {
        case Some(authId) ~ Some(User) =>
          infoLog(s"[JourneyController][createJourney] Creating journey")
          journeyService.createJourney(JourneyConfig(continueUrl), vatNumber, authId).map {
            journeyId => Redirect(routes.CaptureVatRegistrationDateController.show(journeyId).url)
          }
        case Some(_) ~ _ =>
          warnLog("[JourneyController][createJourney] Invalid account type controller")
          Future.successful(Redirect(errorRoutes.InvalidAccountTypeController.show().url))
        case None ~ _ =>
          val message = "Internal ID could not be retrieved from Auth"
          errorLog(s"[JourneyController][createJourney] $message")
          throw new InternalServerException(message)
      }

  }

}
