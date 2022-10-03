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

import play.api.libs.json.Json
import uk.gov.hmrc.claimvatenrolmentfrontend.repositories.JourneyDataRepository
import uk.gov.hmrc.claimvatenrolmentfrontend.services.StoreBox5FigureService.Box5FigureKey

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future

@Singleton
class StoreBox5FigureService @Inject()(journeyDataRepository: JourneyDataRepository
                                      ) {

  def storeBox5Figure(journeyId: String,
                      box5Figure: String,
                      authInternalId: String): Future[Boolean] =
    journeyDataRepository.updateJourneyData(
      journeyId = journeyId,
      dataKey = Box5FigureKey,
      data = Json.toJson(box5Figure),
      authInternalId = authInternalId
    )
}

object StoreBox5FigureService {
  val Box5FigureKey = "box5Figure"
}
