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

package uk.gov.hmrc.claimvatenrolmentfrontend.services

import reactivemongo.api.commands.UpdateWriteResult
import uk.gov.hmrc.claimvatenrolmentfrontend.models.{JourneyConfig, VatKnownFacts}
import uk.gov.hmrc.claimvatenrolmentfrontend.repositories.JourneyDataRepository.{Box5FigureKey, LastMonthSubmittedKey, PostcodeKey}
import uk.gov.hmrc.claimvatenrolmentfrontend.repositories.{JourneyConfigRepository, JourneyDataRepository}
import uk.gov.hmrc.http.NotFoundException

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class JourneyService @Inject()(journeyConfigRepository: JourneyConfigRepository,
                               journeyDataRepository: JourneyDataRepository,
                               journeyIdGenerationService: JourneyIdGenerationService
                              )(implicit ec: ExecutionContext) {

  def createJourney(journeyConfig: JourneyConfig, vatNumber: String, authInternalId: String): Future[String] = {
    val id = journeyIdGenerationService.generateJourneyId()
    for {
      _ <- journeyConfigRepository.insertJourneyConfig(id, journeyConfig, authInternalId)
      _ <- journeyDataRepository.insertJourneyVatNumber(id, authInternalId, vatNumber)
    } yield id
  }

  def retrieveJourneyConfig(journeyId: String, authInternalId: String): Future[JourneyConfig] =
    journeyConfigRepository.retrieveJourneyConfig(journeyId, authInternalId).map {
      case Some(journeyConfig) =>
        journeyConfig
      case None =>
        throw new NotFoundException(s"Journey data was not found for journey ID $journeyId")
    }

  def retrieveJourneyData(journeyId: String, authInternalId: String): Future[VatKnownFacts] =
    journeyDataRepository.getJourneyData(journeyId, authInternalId).map {
      case Some(journeyData) =>
        journeyData
      case None =>
        throw new NotFoundException(s"Journey data was not found for journey ID $journeyId")
    }

  def removePostcodeField(journeyId: String, authInternalId: String): Future[UpdateWriteResult] = {
    journeyDataRepository.removeJourneyDataFields(journeyId, authInternalId, Seq(PostcodeKey))
  }

  def removeAdditionalVatReturnFields(journeyId: String, authInternalId: String): Future[UpdateWriteResult] = {
    journeyDataRepository.removeJourneyDataFields(journeyId, authInternalId, Seq(Box5FigureKey, LastMonthSubmittedKey))
  }

}
