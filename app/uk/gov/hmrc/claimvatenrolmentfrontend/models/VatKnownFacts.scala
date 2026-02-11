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

package uk.gov.hmrc.claimvatenrolmentfrontend.models

import java.time.{LocalDate, Month}

case class VatKnownFacts(vatNumber: String,
                         optPostcode: Option[Postcode],
                         vatRegistrationDate: Option[LocalDate],
                         optReturnsInformation: Option[ReturnsInformation],
                         formBundleReference: Option[String]) {
  def hasCompleteJourneyData: Boolean = vatRegistrationDate.isDefined &&
    (optReturnsInformation.exists(_.hasCompleteData) || formBundleReference.isDefined)
}

case class ReturnsInformation(boxFive: Option[String], lastReturnMonth: Option[Month]) {
  def hasCompleteData: Boolean = boxFive.isDefined && lastReturnMonth.isDefined
}


