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

import javax.inject.{Inject, Singleton}
import play.api.mvc.{Action, AnyContent, BaseController, ControllerComponents}
import uk.gov.hmrc.auth.core.{AuthConnector, AuthorisedFunctions}
import uk.gov.hmrc.claimvatenrolmentfrontend.config.AppConfig

import scala.concurrent.Future

@Singleton
class SignInOutController @Inject()(val controllerComponents: ControllerComponents,
                                    val authConnector: AuthConnector,
                                    appConfig: AppConfig)
  extends BaseController with AuthorisedFunctions{

  def signOut: Action[AnyContent] = Action.async {
    _ => Future.successful(Redirect(s"${appConfig.signOutUrl}?continue=${appConfig.feedbackUrl}").withNewSession)
  }

  def keepAlive: Action[AnyContent] = Action.async { implicit request =>
    Future.successful(Ok.withSession(request.session))
  }

}
