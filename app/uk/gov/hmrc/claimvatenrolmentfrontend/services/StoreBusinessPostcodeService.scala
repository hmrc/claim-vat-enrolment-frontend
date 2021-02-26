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

import javax.inject.{Inject, Singleton}
import play.api.libs.json.Json
import uk.gov.hmrc.claimvatenrolmentfrontend.repositories.JourneyDataRepository
import uk.gov.hmrc.claimvatenrolmentfrontend.services.StoreBusinessPostcodeService.vatRegPostcodeKey

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class StoreBusinessPostcodeService @Inject()(journeyDataRepository: JourneyDataRepository
                                            )(implicit executionContext: ExecutionContext) {

  def storeBusinessPostcodeService(journeyId: String,
                                   vatRegPostcode: String,
                                   authInternalId: String): Future[Unit] =
    journeyDataRepository.updateJourneyData(
      journeyId = journeyId,
      dataKey = vatRegPostcodeKey,
      data = Json.toJson(vatRegPostcode),
      authInternalId = authInternalId
    ).map {
      _ => Unit
    }

}

object StoreBusinessPostcodeService {
  val vatRegPostcodeKey = "vatRegPostcode"
}

