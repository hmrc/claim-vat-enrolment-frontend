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

package uk.gov.hmrc.claimvatenrolmentfrontend.testOnly.stubs.controllers

import play.api.libs.json.{JsError, JsValue, Json, OFormat}
import play.api.mvc.{Action, ControllerComponents}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future

case class Verifier(key: String, value: String)
object Verifier {implicit val format: OFormat[Verifier] = Json.format[Verifier]}

case class AllocateEnrolmentRequest(userId: String, friendlyName: String, `type`: String, verifiers: Seq[Verifier])
object AllocateEnrolmentRequest {
  implicit val format: OFormat[AllocateEnrolmentRequest] = Json.format[AllocateEnrolmentRequest]
}

@Singleton
class AllocateEnrolmentStubController @Inject()(controllerComponents: ControllerComponents) extends BackendController(controllerComponents) {

  private val expectedFacts: Map[String, Map[String, String]] = Map(
    "123456789" -> Map(
      "VATRegistrationDate" -> "2025-01-01",
      "Postcode"            -> "AA1 1AA",
      "BoxFiveValue" -> "123.45",
      "LastMonthLatestStagger" -> "01"
    ),
    "968501689" -> Map(
      "VATRegistrationDate" -> "2025-12-12",
      "Postcode"            -> "BA1 1AB",
      //"FB_NUM"              -> "099123456789"  --> to be used only for VAN
    )
  )

def stubMatch(groupId: String, enrolmentKey: String): Action[JsValue] = Action.async(parse.json) { implicit request =>
    enrolmentKey match {
      case "HMRC-MTD-VAT~VRN~555555555" => Future.successful(BadRequest)
      case "HMRC-MTD-VAT~VRN~444444444" => Future.successful(InternalServerError("Error on the Allocate Enrolment call"))
      case "HMRC-MTD-VAT~VRN~333333333" => Future.successful(InternalServerError("Error on the Allocate Enrolment call"))
      case "HMRC-MTD-VAT~VRN~222222222" => Future.successful(Conflict(Json.obj("code" -> "MULTIPLE_ENROLMENTS_INVALID")))
      case _ => val vrn = enrolmentKey.split("~").lastOption.getOrElse("")
        request.body.validate[AllocateEnrolmentRequest].fold(
          errors =>
            Future.successful(BadRequest(Json.obj(
                  "code"    -> "INVALID_JSON",
                  "details" -> JsError.toJson(errors)
                )
              )
            ),
          parsedReq => {
            val verifiersMap: Map[String, String] =
              parsedReq.verifiers.map(v => v.key -> v.value).toMap
            expectedFacts.get(vrn) match {
              case Some(expectedForVrn) =>
                val matchesAll = expectedForVrn.forall {
                  case (key, expectedValue) =>
                    verifiersMap.get(key).contains(expectedValue)
                }
                if (matchesAll) {
                  Future.successful(Created)
                } else {
                  Future.successful(BadRequest)
                }
              case None =>
                Future.successful(Created)
            }
          }
        )
    }
  }
}
