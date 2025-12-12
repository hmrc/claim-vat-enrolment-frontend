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

package uk.gov.hmrc.claimvatenrolmentfrontend.services

import play.api.data.Form
import play.api.mvc.Request
import uk.gov.hmrc.claimvatenrolmentfrontend.config.AppConfig
import uk.gov.hmrc.claimvatenrolmentfrontend.connectors.AllocateEnrolmentConnector.etmpDateFormat
import uk.gov.hmrc.claimvatenrolmentfrontend.featureswitch.core.config.{FeatureSwitching, KnownFactsCheckFlag}
import uk.gov.hmrc.claimvatenrolmentfrontend.httpparsers.QueryUsersHttpParser.{NoUsersFound, UsersFound}
import uk.gov.hmrc.claimvatenrolmentfrontend.models._
import uk.gov.hmrc.claimvatenrolmentfrontend.repositories.JourneySubmissionRepository
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
                                         config: AppConfig,
                                         journeyService: JourneyService,
                                         submissionRepo: JourneySubmissionRepository) extends FeatureSwitching {

  def claimVatEnrolment(credentialId: String,
                        groupId: String,
                        internalId: String,
                        journeyId: String
                       )(implicit hc: HeaderCarrier,
                         request: Request[_],
                         ec: ExecutionContext): Future[ClaimVatEnrolmentResponse] = {
    journeyService.retrieveJourneyData(journeyId, internalId).flatMap {
      case Some(journeyData) =>
        allocateEnrolmentService.allocateEnrolment(journeyData, credentialId, groupId).flatMap {
          case EnrolmentSuccess =>
            sendAuditEvent(journeyData, isSuccessful = true)
            journeyService.retrieveJourneyConfig(journeyId, internalId).map {
              case Some(journeyConfig) => Right(journeyConfig.continueUrl)
              case None => Left(JourneyConfigFailure)
            }
          case MultipleEnrolmentsInvalid =>
            sendAuditEvent(journeyData, isSuccessful = false, Some(MultipleEnrolmentsInvalid.message))
            Future.successful(Left(CannotAssignMultipleMtdvatEnrolments))
          case InvalidKnownFacts =>
            callEnrolmentStoreProxy(journeyId, journeyData)
          case EnrolmentFailure(_) =>
            callEnrolmentStoreProxy(journeyId, journeyData, enrolmentFailure = true)
        }
      case None => Future.successful(Left(JourneyDataFailure))
    }
  }

  private def callEnrolmentStoreProxy(journeyId: String,
                                      journeyData: VatKnownFacts,
                                      enrolmentFailure: Boolean = false
                                     )(implicit hc: HeaderCarrier,
                                       request: Request[_],
                                       ec: ExecutionContext): Future[ClaimVatEnrolmentResponse] = {

    allocateEnrolmentService.getUserIds(journeyData.vatNumber).flatMap {
      case NoUsersFound if enrolmentFailure =>
        sendAuditEvent(journeyData, isSuccessful = false, Some(NoUsersFound.message))
        throw new InternalServerException(NoUsersFound.message)
      case NoUsersFound =>
        if (isEnabled(KnownFactsCheckFlag)) {
          callKnownFactsMismatchLogic(journeyId, journeyData)
        } else {
          sendAuditEvent(journeyData, isSuccessful = false, Some(InvalidKnownFacts.message))
          Future.successful(Left(KnownFactsMismatchLevel1))
        }
      case UsersFound =>
        sendAuditEvent(journeyData, isSuccessful = false, Some(UsersFound.message))
        Future.successful(Left(EnrolmentAlreadyAllocated))
    }
  }

  private def callKnownFactsMismatchLogic(journeyId: String,
                                          journeyData: VatKnownFacts)(implicit hc: HeaderCarrier,
                                                                      request: Request[_],
                                                                      ec: ExecutionContext): Future[ClaimVatEnrolmentResponse] = {

    val accountStatusUnLocked = "UnLocked"
    val accountStatusLocked = "Locked"
    submissionRepo.findSubmissionData(journeyId, journeyData.vatNumber).flatMap {
      case Some(data) if data.submissionNumber == 1 =>
        submissionRepo.updateSubmissionData(journeyId, journeyData.vatNumber, data.submissionNumber + 1, accountStatusUnLocked)
        sendAuditEventKnownFactsCheck(journeyData, data.submissionNumber + 1, accountStatusUnLocked, Some(InvalidKnownFacts.message))
        Future.successful(Left(KnownFactsMismatchLevel1))
      case Some(data) if data.submissionNumber == 2 =>
        submissionRepo.updateSubmissionData(journeyId, journeyData.vatNumber, data.submissionNumber + 1, accountStatusLocked)
        sendAuditEventKnownFactsCheck(journeyData, data.submissionNumber + 1, accountStatusLocked, Some(InvalidKnownFacts.message))
        Future.successful(Left(KnownFactsMismatchLevel2))
      case _ => submissionRepo.insertSubmissionData(journeyId, journeyData.vatNumber, 1, accountStatusUnLocked)
        sendAuditEventKnownFactsCheck(journeyData, 1, accountStatusUnLocked, Some(InvalidKnownFacts.message))
        Future.successful(Left(KnownFactsMismatchLevel1))
    }
  }

  private def sendAuditEventKnownFactsCheck(vatKnownFacts: VatKnownFacts,
                                            submissionNumber: Int,
                                            accountStatus: String,
                                            optFailureMessage: Option[String] = None
                            )(implicit hc: HeaderCarrier,
                              request: Request[_],
                              ec: ExecutionContext): Future[AuditResult] =
    auditConnector.sendEvent(buildClaimVatEnrolmentAuditEvent(vatKnownFacts, isSuccessful = false, optFailureMessage, Some(submissionNumber), Some(accountStatus)))

  private def sendAuditEvent(vatKnownFacts: VatKnownFacts,
                             isSuccessful: Boolean,
                             optFailureMessage: Option[String] = None
                            )(implicit hc: HeaderCarrier,
                              request: Request[_],
                              ec: ExecutionContext): Future[AuditResult] =
    auditConnector.sendEvent(buildClaimVatEnrolmentAuditEvent(vatKnownFacts, isSuccessful, optFailureMessage))

  private def buildClaimVatEnrolmentAuditEvent(vatKnownFacts: VatKnownFacts,
                                               isSuccessful: Boolean,
                                               optFailureMessage: Option[String],
                                               submissionNumber: Option[Int] = None,
                                               accountStatus: Option[String] = Some("")
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
      "latestMonthReturn" -> vatKnownFacts.optReturnsInformation.map(x => formatString(x.lastReturnMonth.getValue)).getOrElse(""),
      "vatSubscriptionClaimSuccessful" -> isSuccessful.toString,
      "enrolmentAndClientDatabaseFailureReason" -> optFailureMessage.getOrElse("")
    ) ++
      ( if (config.isKnownFactsCheckEnabled) {
          Map("submissionNumber"-> submissionNumber.getOrElse(0).toString,
               "accountStatus"-> accountStatus.getOrElse(""))
      } else {Map.empty})

    val updatedDetail: Map[String, String] = detail.filter {case(_, value) => value.nonEmpty}

    DataEvent(
      auditSource = auditSource,
      auditType = auditType,
      tags = AuditExtensions.auditHeaderCarrier(hc).toAuditTags(transactionName, request.path),
      detail = AuditExtensions.auditHeaderCarrier(hc).toAuditDetails(updatedDetail.toSeq: _*)
    )
  }

  private def formatString(value: Int): String = {
    "%02d".format(value)
  }

  def buildPostCodeFailureAuditEvent(form: Form[Postcode])
                                    (implicit hc: HeaderCarrier, request: Request[_]): DataEvent = {

    val auditSource = "claim-vat-enrolment"
    val transactionName: String = "MTDVATPostCodeFail"
    val auditType: String = "MTDVATPostCodeFail"

    val postcodeString = form.value match {
      case Some(value) => value.stringValue
      case _ => ""
    }

    val detail: Map[String, String] = Map(
      "postCodeEntered" -> postcodeString
    ).filter { case (_, value) => value.nonEmpty }

    DataEvent(
      auditSource = auditSource,
      auditType = auditType,
      tags = AuditExtensions.auditHeaderCarrier(hc).toAuditTags(transactionName, request.path),
      detail = AuditExtensions.auditHeaderCarrier(hc).toAuditDetails(detail.toSeq: _*)
    )

  }
}

object ClaimVatEnrolmentService {

  private type ClaimVatEnrolmentResponse = Either[ClaimVatEnrolmentFailure, String]

  sealed trait ClaimVatEnrolmentFailure

  case object EnrolmentAlreadyAllocated extends ClaimVatEnrolmentFailure

  case object CannotAssignMultipleMtdvatEnrolments extends ClaimVatEnrolmentFailure

  case object KnownFactsMismatchLevel1 extends ClaimVatEnrolmentFailure

  case object KnownFactsMismatchLevel2 extends ClaimVatEnrolmentFailure

  case object JourneyConfigFailure extends ClaimVatEnrolmentFailure

  case object JourneyDataFailure extends ClaimVatEnrolmentFailure

}
