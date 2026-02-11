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

package uk.gov.hmrc.claimvatenrolmentfrontend.services

import play.api.mvc.Request

import javax.inject.{Inject, Singleton}
import uk.gov.hmrc.claimvatenrolmentfrontend.connectors.{AllocateEnrolmentConnector, EnrolmentStoreProxyConnector}
import uk.gov.hmrc.claimvatenrolmentfrontend.httpparsers.QueryUsersHttpParser.QueryUsersSuccess
import uk.gov.hmrc.claimvatenrolmentfrontend.models.{AllocateEnrolmentResponse, VatKnownFacts}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

@Singleton
class AllocateEnrolmentService @Inject()(allocateEnrolmentConnector: AllocateEnrolmentConnector,
                                         enrolmentStoreProxyConnector: EnrolmentStoreProxyConnector) {

  def allocateEnrolment(vatKnownFacts: VatKnownFacts,
                        credentialId: String,
                        groupId: String
                       )(implicit hc: HeaderCarrier, request: Request[_]): Future[AllocateEnrolmentResponse] =
    allocateEnrolmentConnector.allocateEnrolment(vatKnownFacts, credentialId, groupId)

  def getUserIds(vatNumber: String)(implicit hc: HeaderCarrier, request: Request[_]): Future[QueryUsersSuccess] =
    enrolmentStoreProxyConnector.getUserIds(vatNumber)

}
