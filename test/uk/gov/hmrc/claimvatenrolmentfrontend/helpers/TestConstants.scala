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

package uk.gov.hmrc.claimvatenrolmentfrontend.helpers

import uk.gov.hmrc.claimvatenrolmentfrontend.models.{ClaimVatEnrolmentModel, Postcode, ReturnsInformationModel}

import java.time.{LocalDate, Month}
import java.util.UUID

object TestConstants {

  val testJourneyId: String = UUID.randomUUID().toString
  val testContinueUrl: String = "/test"
  val testVatRegDate: LocalDate = LocalDate.parse("2021-01-01")
  val testVatReturn: Boolean = true
  val testInternalId: String = UUID.randomUUID().toString
  val testVatNumber: String = UUID.randomUUID().toString
  val testBoxFive: String = "1000.00"
  val testLastMonthSubmitted: Month = Month.MARCH
  val testPostcode: Postcode = Postcode("AA11AA")
  val testLastReturnMonth: Month = Month.JANUARY
  val testFullClaimVatEnrolmentModel: ClaimVatEnrolmentModel =
    ClaimVatEnrolmentModel(
      vatNumber = testVatNumber,
      optPostcode = Some(testPostcode),
      vatRegistrationDate = testVatRegDate,
      optReturnsInformation =
        Some(ReturnsInformationModel(
          boxFive = testBoxFive,
          lastReturnMonth = testLastReturnMonth
        ))
    )

}