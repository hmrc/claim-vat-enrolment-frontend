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

import play.api.mvc.Request
import uk.gov.hmrc.claimvatenrolmentfrontend.models.{JourneyConfig, VatKnownFacts}
import uk.gov.hmrc.claimvatenrolmentfrontend.repositories.JourneyDataRepository.{Box5FigureKey, LastMonthSubmittedKey, PostcodeKey}
import uk.gov.hmrc.claimvatenrolmentfrontend.repositories.{JourneyConfigRepository, JourneyDataRepository}
import utils.LoggingUtil

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class JourneyService @Inject()(journeyConfigRepository: JourneyConfigRepository,
                               journeyDataRepository: JourneyDataRepository,
                               journeyIdGenerationService: JourneyIdGenerationService
                              )(implicit ec: ExecutionContext) extends LoggingUtil{

  def createJourney(journeyConfig: JourneyConfig, vatNumber: String, authInternalId: String): Future[String] = {
    val id = journeyIdGenerationService.generateJourneyId()
    for {
      _ <- journeyConfigRepository.insertJourneyConfig(id, journeyConfig, authInternalId)
      _ <- journeyDataRepository.insertJourneyVatNumber(id, authInternalId, vatNumber)
    } yield id
  }

  def retrieveJourneyConfig(journeyId: String, authInternalId: String)(implicit request: Request[_]): Future[Option[JourneyConfig]] =
    journeyConfigRepository.retrieveJourneyConfig(journeyId, authInternalId).map {
      case Some(journeyConfig) =>
        infoLog(s"[JourneyService][retrieveJourneyConfig] - successfully retrieved the journey config for journey ID $journeyId")
        Some(journeyConfig)
      case None =>
        errorLog(s"[JourneyService][retrieveJourneyConfig] - Journey data was not found for journey ID $journeyId")
        None
    }

  def retrieveJourneyData(journeyId: String, authInternalId: String)(implicit request: Request[_]): Future[Option[VatKnownFacts]] = {
      journeyDataRepository.getJourneyData(journeyId, authInternalId).map {
        case Some(journeyData) =>
          infoLog(s"[JourneyService][retrieveJourneyData] - successfully retrieved the journey data for journey ID $journeyId")
          Some(journeyData)
        case None =>
          errorLog(s"[JourneyService][retrieveJourneyData] - Journey data was not found for journey ID $journeyId")
          None
      }
  }

  def removePostcodeField(journeyId: String, authInternalId: String): Future[Boolean] = {
    journeyDataRepository.removeJourneyDataFields(journeyId, authInternalId, Seq(PostcodeKey))
  }

  def removeAdditionalVatReturnFields(journeyId: String, authInternalId: String): Future[Boolean] = {
    journeyDataRepository.removeJourneyDataFields(journeyId, authInternalId, Seq(Box5FigureKey, LastMonthSubmittedKey))
  }

}
