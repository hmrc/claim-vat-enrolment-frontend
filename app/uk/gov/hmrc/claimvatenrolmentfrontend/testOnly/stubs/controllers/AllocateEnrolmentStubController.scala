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

import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, ControllerComponents}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future

@Singleton
class AllocateEnrolmentStubController @Inject()(controllerComponents: ControllerComponents) extends BackendController(controllerComponents) {

  def stubMatch(groupId: String, enrolmentKey: String): Action[JsValue] = Action.async(parse.json) {
    implicit request =>
      enrolmentKey match {
        case "HMRC-MTD-VAT~VRN~444444444" => Future.successful(InternalServerError("Error on the Allocate Enrolment call"))
        case "HMRC-MTD-VAT~VRN~333333333" => Future.successful(InternalServerError("Error on the Allocate Enrolment call"))
        case "HMRC-MTD-VAT~VRN~222222222" => Future.successful(Conflict(Json.obj("code" -> "MULTIPLE_ENROLMENTS_INVALID")))
        case "HMRC-MTD-VAT~VRN~111111111" => Future.successful(BadRequest)
        case _ => Future.successful(Created)
      }
  }

}
