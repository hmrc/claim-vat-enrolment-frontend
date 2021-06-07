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

package uk.gov.hmrc.claimvatenrolmentfrontend.assets

import uk.gov.hmrc.auth.core.retrieve.Credentials
import uk.gov.hmrc.claimvatenrolmentfrontend.models.{JourneyConfig, Postcode, ReturnsInformation, VatKnownFacts}

import java.time.{LocalDate, Month}
import java.util.UUID


object TestConstants {

  val testVatNumber: String = "123456782"
  val testVatRegDate: LocalDate = LocalDate.now()
  val testPostcode: Postcode = Postcode("AA11AA")
  val testLastReturnMonth: Month = Month.JANUARY
  val testBoxFive: String = "1000.00"
  val testJourneyId: String = UUID.randomUUID().toString
  val testInternalId: String = UUID.randomUUID().toString
  val testContinueUrl: String = "/test-continue-url"
  val testGroupId: String = UUID.randomUUID().toString
  val testCredentialId: String = UUID.randomUUID().toString
  val testCredentials: Credentials = Credentials(testCredentialId, "GovernmentGateway")
  val testJourneyConfig: JourneyConfig = JourneyConfig(testContinueUrl)

  val testFullVatKnownFacts: VatKnownFacts =
    VatKnownFacts(
      vatNumber = testVatNumber,
      optPostcode = Some(testPostcode),
      vatRegistrationDate = testVatRegDate,
      optReturnsInformation =
        Some(ReturnsInformation(
          boxFive = testBoxFive,
          lastReturnMonth = testLastReturnMonth
        ))
    )

  val testVatKnownFactsNoReturns: VatKnownFacts =
    VatKnownFacts(
      vatNumber = testVatNumber,
      optPostcode = Some(testPostcode),
      vatRegistrationDate = testVatRegDate,
      optReturnsInformation = None
    )

  val testVatKnownFactsNoReturnsNoPostcode: VatKnownFacts =
    VatKnownFacts(
      vatNumber = testVatNumber,
      optPostcode = None,
      vatRegistrationDate = testVatRegDate,
      optReturnsInformation = None
    )

  val testVatKnownFactsNoPostcode: VatKnownFacts =
    VatKnownFacts(
      vatNumber = testVatNumber,
      optPostcode = None,
      vatRegistrationDate = testVatRegDate,
      optReturnsInformation = Some(ReturnsInformation(
        boxFive = testBoxFive,
        lastReturnMonth = testLastReturnMonth
      ))
    )

}
