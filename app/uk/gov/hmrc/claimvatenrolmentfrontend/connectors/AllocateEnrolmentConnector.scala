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

package uk.gov.hmrc.claimvatenrolmentfrontend.connectors

import play.api.libs.json.{JsObject, Json, Writes}
import play.api.mvc.Request
import uk.gov.hmrc.claimvatenrolmentfrontend.config.AppConfig
import uk.gov.hmrc.claimvatenrolmentfrontend.connectors.AllocateEnrolmentConnector._
import uk.gov.hmrc.claimvatenrolmentfrontend.models.AllocateEnrolmentResponseHttpParser.AllocateEnrolmentResponseReads
import uk.gov.hmrc.claimvatenrolmentfrontend.models.{AllocateEnrolmentResponse, VatKnownFacts}
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import utils.LoggingUtil

import java.time.format.DateTimeFormatter
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AllocateEnrolmentConnector @Inject()(http: HttpClient,
                                           appConfig: AppConfig
                                          )(implicit ec: ExecutionContext) extends LoggingUtil {

  def allocateEnrolment(vatKnownFacts: VatKnownFacts, credentialId: String, groupId: String)(implicit hc: HeaderCarrier, request: Request[_]): Future[AllocateEnrolmentResponse] = {
    infoLog(s"[AllocateEnrolmentConnector][allocateEnrolment] Allocating enrolment")
    val enrolmentKey = s"HMRC-MTD-VAT~VRN~${vatKnownFacts.vatNumber}"

    val requestBody = Json.obj(
      "userId" -> credentialId,
      "friendlyName" -> "Making Tax Digital - VAT",
      "type" -> "principal",
      "verifiers" -> Json.arr(
        Json.obj(
          "key" -> "VATRegistrationDate",
          "value" -> vatKnownFacts.vatRegistrationDate.format(etmpDateFormat)
        ),
        Json.obj(
          "key" -> "Postcode",
          "value" -> (vatKnownFacts.optPostcode match {
            case Some(postcode) => postcode.sanitisedPostcode
            case None => NullValue
          })
        ),
        Json.obj(
          "key" -> "BoxFiveValue",
          "value" -> (vatKnownFacts.optReturnsInformation match {
            case Some(returnsInformation) => returnsInformation.boxFive
            case None => NullValue
          })
        ),
        Json.obj(
          "key" -> "LastMonthLatestStagger",
          "value" -> (vatKnownFacts.optReturnsInformation match {
            case Some(returnsInformation) => returnsInformation.lastReturnMonth.getValue.formatted("%02d")
            case None => NullValue
          })
        )
      )
    )

    http.POST[JsObject, AllocateEnrolmentResponse](
      url = appConfig.allocateEnrolmentUrl(groupId, enrolmentKey),
      body = requestBody
    )(implicitly[Writes[JsObject]],
      AllocateEnrolmentResponseReads,
      hc,
      ec)
  }
}

object AllocateEnrolmentConnector {
  val NullValue: String = "NULL"

  val etmpDateFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
}
