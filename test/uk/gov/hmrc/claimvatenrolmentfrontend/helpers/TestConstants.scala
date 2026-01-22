/*
 * Copyright 2023 HM Revenue & Customs
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

import play.api.mvc.{Result, Results}
import uk.gov.hmrc.claimvatenrolmentfrontend.models.{JourneyConfig, Postcode, ReturnsInformation, VatKnownFacts}

import java.time.{LocalDate, Month}
import java.util.UUID

object TestConstants {

  val testJourneyId: String = UUID.randomUUID().toString
  val testCredentialId: String = UUID.randomUUID().toString
  val testGroupId: String = UUID.randomUUID().toString
  val testContinueUrl: String = "/test"
  val testVatRegDate: Some[LocalDate] = Some(LocalDate.parse("2021-01-01"))
  val testVatReturn: Boolean = true
  val testInternalId: String = UUID.randomUUID().toString
  val continueResult: Result = Results.Ok("next action executed")
  val testVatNumber: String = UUID.randomUUID().toString
  val testBoxFive: Some[String] = Some("1000.00")
  val testFormBundleReference: String = "123456789101"
  val testLastMonthSubmitted: Some[Month] = Some(Month.MARCH)
  val testPostcode: Postcode = Postcode("AA11AA")
  val testLastReturnMonth: Some[Month] = Some(Month.JANUARY)
  val testFullVatKnownFacts: Option[VatKnownFacts] = {
    Some(VatKnownFacts(
      vatNumber = testVatNumber,
      optPostcode = Some(testPostcode),
      vatRegistrationDate = testVatRegDate,
      optReturnsInformation =
        Some(ReturnsInformation(
          boxFive = testBoxFive,
          lastReturnMonth = testLastReturnMonth
        )),
      formBundleReference = Some(testFormBundleReference)
    ))
  }

  val testSubmissionUpdateTrueStatus: Boolean = true
  val testSubmissionUpdateFalseStatus: Boolean = false
  val testSubmissionNumber1: Int = 1
  val testSubmissionNumber2: Int = 2
  val testSubmissionNumber3: Int = 3
  val testAccountStatusUnLocked: String = "UnLocked"
  val testAccountStatusLocked: String = "Locked"

  def testVrnLock(attempts: Int = 1): Map[String, Int] = Map(testVatNumber -> attempts)

  val testJourneyConfig: Option[JourneyConfig] = Some(JourneyConfig(testContinueUrl))

}