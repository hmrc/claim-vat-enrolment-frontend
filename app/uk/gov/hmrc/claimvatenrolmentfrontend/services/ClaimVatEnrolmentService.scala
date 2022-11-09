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

package uk.gov.hmrc.claimvatenrolmentfrontend.services

import play.api.mvc.Request
import uk.gov.hmrc.claimvatenrolmentfrontend.connectors.AllocateEnrolmentConnector.etmpDateFormat
import uk.gov.hmrc.claimvatenrolmentfrontend.httpparsers.QueryUsersHttpParser.{NoUsersFound, UsersFound}
import uk.gov.hmrc.claimvatenrolmentfrontend.models._
import uk.gov.hmrc.claimvatenrolmentfrontend.services.ClaimVatEnrolmentService._
import uk.gov.hmrc.http.{HeaderCarrier, InternalServerException}
import uk.gov.hmrc.play.audit.AuditExtensions
import uk.gov.hmrc.play.audit.http.connector.{AuditConnector, AuditResult}
import uk.gov.hmrc.play.audit.model.DataEvent

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ClaimVatEnrolmentService @Inject()(auditConnector: AuditConnector,
                                         allocateEnrolmentService: AllocateEnrolmentService,
                                         journeyService: JourneyService) {

  private def buildClaimVatEnrolmentAuditEvent(vatKnownFacts: VatKnownFacts,
                                               isSuccessful: Boolean,
                                               optFailureMessage: Option[String]
                                              )(implicit hc: HeaderCarrier,
                                                request: Request[_]): DataEvent = {

    val auditSource = "claim-vat-enrolment"
    val transactionName: String = "MTDVATClaimSubscriptionRequest"
    val auditType: String = "MTDVatClaimSubscription"

    val detail: Map[String, String] = Map(
      "vatNumber" -> vatKnownFacts.vatNumber,
      "businessPostcode" -> vatKnownFacts.optPostcode.map(_.sanitisedPostcode).getOrElse(""),
      "vatRegistrationDate" -> vatKnownFacts.vatRegistrationDate.format(etmpDateFormat),
      "boxFiveAmount" -> vatKnownFacts.optReturnsInformation.map(_.boxFive).getOrElse(""),
      "latestMonthReturn" -> vatKnownFacts.optReturnsInformation.map(_.lastReturnMonth.getValue.formatted("%02d")).getOrElse(""),
      "vatSubscriptionClaimSuccessful" -> isSuccessful.toString,
      "enrolmentAndClientDatabaseFailureReason" -> optFailureMessage.getOrElse("")
    ).filter { case (_, value) => value.nonEmpty }

    DataEvent(
      auditSource = auditSource,
      auditType = auditType,
      tags = AuditExtensions.auditHeaderCarrier(hc).toAuditTags(transactionName, request.path),
      detail = AuditExtensions.auditHeaderCarrier(hc).toAuditDetails(detail.toSeq: _*)
    )

  }

  private def sendAuditEvent(vatKnownFacts: VatKnownFacts,
                             isSuccessful: Boolean,
                             optFailureMessage: Option[String] = None
                            )(implicit hc: HeaderCarrier,
                              request: Request[_],
                              ec: ExecutionContext): Future[AuditResult] =
    auditConnector.sendEvent(buildClaimVatEnrolmentAuditEvent(vatKnownFacts, isSuccessful, optFailureMessage))

  def claimVatEnrolment(credentialId: String,
                        groupId: String,
                        internalId: String,
                        journeyId: String
                       )(implicit hc: HeaderCarrier,
                         request: Request[_],
                         ec: ExecutionContext): Future[ClaimVatEnrolmentResponse] = {
    journeyService.retrieveJourneyData(journeyId, internalId).flatMap {
      journeyData =>
        allocateEnrolmentService.allocateEnrolment(journeyData, credentialId, groupId).flatMap {
          case EnrolmentSuccess =>
            sendAuditEvent(journeyData, isSuccessful = true)
            journeyService.retrieveJourneyConfig(journeyId, internalId).map {
              journeyConfig =>
                Right(journeyConfig.continueUrl)
            }
          case MultipleEnrolmentsInvalid =>
            sendAuditEvent(journeyData, isSuccessful = false, Some(MultipleEnrolmentsInvalid.message))
            Future.successful(Left(CannotAssignMultipleMtdvatEnrolments))
          case InvalidKnownFacts =>
            sendAuditEvent(journeyData, isSuccessful = false, Some(InvalidKnownFacts.message))
            Future.successful(Left(KnownFactsMismatch))
          case EnrolmentFailure(_) =>
            allocateEnrolmentService.getUserIds(journeyData.vatNumber).map {
              case UsersFound =>
                sendAuditEvent(journeyData, isSuccessful = false, Some(UsersFound.message))
                Left(EnrolmentAlreadyAllocated)
              case NoUsersFound =>
                sendAuditEvent(journeyData, isSuccessful = false, Some(NoUsersFound.message))
                throw new InternalServerException(NoUsersFound.message)
            }
        }
    }
  }

}

object ClaimVatEnrolmentService {

  sealed trait ClaimVatEnrolmentFailure

  case object EnrolmentAlreadyAllocated extends ClaimVatEnrolmentFailure

  case object CannotAssignMultipleMtdvatEnrolments extends ClaimVatEnrolmentFailure

  case object KnownFactsMismatch extends ClaimVatEnrolmentFailure

  type ClaimVatEnrolmentResponse = Either[ClaimVatEnrolmentFailure, String]

}
