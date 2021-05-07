/*
 * Copyright 2021 HM Revenue & Customs
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

package uk.gov.hmrc.claimvatenrolmentfrontend.stubs

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.libs.json.{JsObject, Json}
import uk.gov.hmrc.claimvatenrolmentfrontend.connectors.AllocateEnrolmentConnector._
import uk.gov.hmrc.claimvatenrolmentfrontend.models.ClaimVatEnrolmentModel
import uk.gov.hmrc.claimvatenrolmentfrontend.utils.WireMockMethods


trait AllocationEnrolmentStub extends WireMockMethods {

  private def allocateEnrolmentUrl(groupId: String, enrolmentKey: String): String = s"/tax-enrolments/groups/$groupId/enrolments/$enrolmentKey"

  def stubAllocateEnrolment(claimVatEnrolmentInfo: ClaimVatEnrolmentModel,
                            credentialId: String,
                            groupId: String)(status: Int, jsonBody: JsObject): StubMapping = {
    val enrolmentKey = s"HMRC-MTD-VAT~VRN~${claimVatEnrolmentInfo.vatNumber}"

    val allocateEnrolmentJsonBody = Json.obj(
      "userId" -> credentialId,
      "friendlyName" -> "Making Tax Digital - VAT",
      "type" -> "principal",
      "verifiers" -> Json.arr(
        Json.obj(
          "key" -> "VATRegistrationDate",
          "value" ->  claimVatEnrolmentInfo.vatRegistrationDate.format(etmpDateFormat)
        ),
        Json.obj(
          "key" -> "Postcode",
          "value" -> (claimVatEnrolmentInfo.optPostcode match {
            case Some(postcode) => postcode.sanitisedPostcode
            case None => NullValue
          })
        ),
        Json.obj(
          "key" -> "BoxFiveValue",
          "value" -> (claimVatEnrolmentInfo.optReturnsInformation match {
            case Some(returnsInformation) => returnsInformation.boxFive
            case None => NullValue
          })
        ),
        Json.obj(
          "key" -> "LastMonthLatestStagger",
          "value" -> (claimVatEnrolmentInfo.optReturnsInformation match {
            case Some(returnsInformation) => returnsInformation.lastReturnMonth
            case None => NullValue
          })
        )
      )
    )

    when(
      method = POST,
      uri = allocateEnrolmentUrl(
        groupId = groupId,
        enrolmentKey = enrolmentKey
      ),
      body = allocateEnrolmentJsonBody
    ).thenReturn(status, jsonBody)
  }
}
