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

package uk.gov.hmrc.claimvatenrolmentfrontend.assets

import play.api.libs.json.{JsObject, Json}
import uk.gov.hmrc.auth.core.retrieve.Credentials
import uk.gov.hmrc.claimvatenrolmentfrontend.models.{JourneyConfig, JourneySubmission, Postcode, ReturnsInformation, VatKnownFacts}
import uk.gov.hmrc.claimvatenrolmentfrontend.repositories.JourneyDataRepository.{AuthInternalIdKey, JourneyIdKey}

import java.time.{LocalDate, Month}
import java.util.UUID


object TestConstants {

  val testVatNumber: String = "123456782"
  val differentTestVatNumber: String = "123456783"
  val testVatRegDate: Some[LocalDate] = Some(LocalDate.now())
  val testPostcode: Postcode = Postcode("AA11AA")
  val testLastReturnMonth: Some[Month] = Some(Month.JANUARY)
  val testBoxFive: Some[String] = Some("1000.00")
  val testFormBundleReference: String = "123456789101"
  val testJourneyId: String = UUID.randomUUID().toString
  val testInternalId: String = UUID.randomUUID().toString
  val testContinueUrl: String = "/test-continue-url"
  val testGroupId: String = UUID.randomUUID().toString
  val testCredentialId: String = UUID.randomUUID().toString
  val testCredentials: Credentials = Credentials(testCredentialId, "GovernmentGateway")
  val testJourneyConfig: JourneyConfig = JourneyConfig(testContinueUrl)

  val testSubmissionUpdateStatusTrue: Boolean = true
  val testSubmissionUpdateStatusFalse: Boolean = false

  val testKey = "testKey"
  val testData = "test"
  val testSecondKey = "secondKey"
  val testSecondData = "secondTest"
  val testThirdKey = "thirdKey"
  val testThirdData = "thirdTest"
  val testFourthKey = "fourthKey"
  val testFourthData = "fourthTest"
  val testFifthKey = "fifthKey"
  val testFifthData = "fifthTest"

  val updatedData = "updated"

  val testSubmissionNumber1: Int = 1
  val testSubmissionNumber2: Int = 2
  val testSubmissionNumber3: Int = 3
  val testAccountStatusUnLocked: String= "UnLocked"
  val testAccountStatusLocked: String= "Locked"

  val accountStatusKey: String = "accountStatus"

  val testSubmissionDataAttempt1: JourneySubmission =
      JourneySubmission(
        journeyId = testJourneyId,
        vrn = testVatNumber,
        submissionNumber = testSubmissionNumber1,
        accountStatus = testAccountStatusUnLocked
      )

  val testSubmissionDataAttempt2: JourneySubmission =
    JourneySubmission(
      journeyId = testJourneyId,
      vrn = testVatNumber,
      submissionNumber = testSubmissionNumber2,
      accountStatus = testAccountStatusUnLocked
    )

  val testSubmissionDataAttempt3: JourneySubmission =
    JourneySubmission(
      journeyId = testJourneyId,
      vrn = testVatNumber,
      submissionNumber = testSubmissionNumber3,
      accountStatus = testAccountStatusLocked
    )

  val testFullVatKnownFacts: VatKnownFacts =
    VatKnownFacts(
      vatNumber = testVatNumber,
      optPostcode = Some(testPostcode),
      vatRegistrationDate = testVatRegDate,
      optReturnsInformation =
        Some(ReturnsInformation(
          boxFive = testBoxFive,
          lastReturnMonth = testLastReturnMonth
        )),
      formBundleReference = Some(testFormBundleReference)
    )

  val testVatKnownFactsDefault: VatKnownFacts =
    VatKnownFacts(
      vatNumber = testVatNumber,
      optPostcode = None,
      vatRegistrationDate = testVatRegDate,
      optReturnsInformation = None,
      formBundleReference = None
    )

  val testVatKnownFactsNoReturns: VatKnownFacts =
    VatKnownFacts(
      vatNumber = testVatNumber,
      optPostcode = Some(testPostcode),
      vatRegistrationDate = testVatRegDate,
      optReturnsInformation = None,
      formBundleReference = Some(testFormBundleReference)
    )

  val testVatKnownFactsNoReturnsNoPostcode: VatKnownFacts =
    VatKnownFacts(
      vatNumber = testVatNumber,
      optPostcode = None,
      vatRegistrationDate = testVatRegDate,
      optReturnsInformation = None,
      formBundleReference = Some(testFormBundleReference)
    )

  val testVatKnownFactsNoPostcode: VatKnownFacts =
    VatKnownFacts(
      vatNumber = testVatNumber,
      optPostcode = None,
      vatRegistrationDate = testVatRegDate,
      optReturnsInformation = Some(ReturnsInformation(
        boxFive = testBoxFive,
        lastReturnMonth = testLastReturnMonth
      )),
      formBundleReference = Some(testFormBundleReference)
    )

  val testVatKnownFactsNoPostcodeNoRetInfo: VatKnownFacts =
    VatKnownFacts(
      vatNumber = testVatNumber,
      optPostcode = None,
      vatRegistrationDate = testVatRegDate,
      optReturnsInformation = Some(ReturnsInformation(
        boxFive = testBoxFive,
        lastReturnMonth = None
      )),
      formBundleReference = None
    )

  val testVatKnownFactsNoFormBundleReference: VatKnownFacts =
    VatKnownFacts(
      vatNumber = testVatNumber,
      optPostcode = Some(Postcode("AA11AA")),
      vatRegistrationDate = testVatRegDate,
      optReturnsInformation = None,
      formBundleReference = None
    )

  val emptyJourneyDataJson: JsObject = Json.obj(
    JourneyIdKey -> testJourneyId,
    AuthInternalIdKey -> testInternalId
  )


}
