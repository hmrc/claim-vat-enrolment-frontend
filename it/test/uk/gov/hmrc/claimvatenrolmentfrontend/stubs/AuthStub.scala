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

package uk.gov.hmrc.claimvatenrolmentfrontend.stubs

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.libs.json.{JsObject, Json, Writes}
import uk.gov.hmrc.claimvatenrolmentfrontend.assets.TestConstants.testCredentialId
import uk.gov.hmrc.claimvatenrolmentfrontend.utils.WireMockMethods

trait AuthStub extends WireMockMethods {

  val authUrl = "/auth/authorise"

  def stubAuth[T](status: Int, body: T)(implicit writes: Writes[T]): StubMapping = {
    when(method = POST, uri = authUrl)
      .thenReturn(status = status, body = writes.writes(body))
  }

  def stubAuthFailure(): StubMapping = {
    when(method = POST, uri = authUrl)
      .thenReturn(status = 401, body = successfulAuthResponse(None))
  }

  def successfulAuthResponse(internalId: Option[String]): JsObject = Json.obj(
    "internalId" -> internalId
  )

  def successfulAuthResponse(groupId: Option[String],
                             internalId: Option[String],
                             credentialRole: Option[String] = Some("User")): JsObject = Json.obj(
    "optionalCredentials" -> Json.obj(
      "providerId" -> testCredentialId,
      "providerType" -> "GovernmentGateway"
    ),
    "groupIdentifier" -> groupId,
    "internalId" -> internalId,
    "credentialRole" -> credentialRole
  )
}
