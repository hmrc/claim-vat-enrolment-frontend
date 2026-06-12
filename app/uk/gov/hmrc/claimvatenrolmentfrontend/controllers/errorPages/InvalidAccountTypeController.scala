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

package uk.gov.hmrc.claimvatenrolmentfrontend.controllers.errorPages

import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.claimvatenrolmentfrontend.config.AppConfig
import uk.gov.hmrc.claimvatenrolmentfrontend.views.html.errorPages.{invalid_account_type, user_is_an_agent_error_page}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future

@Singleton
class InvalidAccountTypeController @Inject() (mcc: MessagesControllerComponents,
                                              invalidAccountTypeView: invalid_account_type,
                                              userIsAnAgentView: user_is_an_agent_error_page)(implicit appConfig: AppConfig)
    extends FrontendController(mcc) {

  val showInvalidAccountTypeError: Action[AnyContent] = Action.async { implicit request =>
    Future.successful(Ok(invalidAccountTypeView()))
  }

  val showUserIsAnAgentError: Action[AnyContent] = Action.async { implicit request =>
    Future.successful(Ok(userIsAnAgentView()))
  }

}
