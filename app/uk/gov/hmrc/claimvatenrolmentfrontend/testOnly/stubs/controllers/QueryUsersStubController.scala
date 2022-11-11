/*
 * Copyright 2022 HM Revenue & Customs
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

package uk.gov.hmrc.claimvatenrolmentfrontend.testOnly.stubs.controllers

import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.{Inject, Singleton}

@Singleton
class QueryUsersStubController @Inject()(controllerComponents: ControllerComponents) extends BackendController(controllerComponents) {

  def getUserIds(vatNumber: String): Action[AnyContent] = {
    Action {
      vatNumber match {
        case "333333333" => Ok
        case "444444444" => NoContent
        case _ => InternalServerError("Error in the QueryUsersStubController")
      }
    }
  }

}
