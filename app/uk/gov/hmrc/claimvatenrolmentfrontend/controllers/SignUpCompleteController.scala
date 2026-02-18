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

import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.internalId
import uk.gov.hmrc.auth.core.{AuthConnector, AuthorisedFunctions}
import uk.gov.hmrc.claimvatenrolmentfrontend.config.AppConfig
import uk.gov.hmrc.claimvatenrolmentfrontend.views.html.sign_up_complete_page
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.LoggingUtil

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SignUpCompleteController @Inject()(mcc: MessagesControllerComponents,
                                         view: sign_up_complete_page,
                                         val authConnector: AuthConnector
                                        )(implicit val config: AppConfig, ec: ExecutionContext) extends FrontendController(mcc) with AuthorisedFunctions with LoggingUtil {


  def signUpComplete(journeyId: String): Action[AnyContent] = Action.async {
    implicit request =>
      authorised().retrieve(internalId) {
        case Some(_) =>
          infoLog(s"[SignUpCompleteController][signUpComplete] Sign up successful")
          Future.successful(Ok(view()))
        case None =>
          val message = "Internal ID could not be retrieved from Auth"
          errorLog(s"[SignUpCompleteController][signUpComplete] $message")
          throw new InternalServerException(message)
      }
  }
}