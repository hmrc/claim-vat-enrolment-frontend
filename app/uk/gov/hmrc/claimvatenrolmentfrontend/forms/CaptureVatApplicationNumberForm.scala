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

package uk.gov.hmrc.claimvatenrolmentfrontend.forms

import play.api.data.Form
import play.api.data.Forms._
import play.api.data.validation.Constraint
import uk.gov.hmrc.claimvatenrolmentfrontend.forms.utils.ConstraintUtil.ConstraintUtil
import uk.gov.hmrc.claimvatenrolmentfrontend.forms.utils.ValidationHelper.{validate, validateNot}

object CaptureVatApplicationNumberForm {

  private val vatApplicationNumber: String = "vatApplicationNumber"

  private val vatApplicationNumberEmpty: Constraint[String] = Constraint("vatApplicationNumber.not_entered")(vatApplicationNumber =>
    validate(
      constraint = vatApplicationNumber.isEmpty,
      errMsg = "capture-vat-application-number.error.message.nothing"
    ))

  private val vatApplicationNumberLength: Constraint[String] = Constraint("vatApplicationNumber.invalid_length")(vatApplicationNumber =>
    validateNot(
      constraint = vatApplicationNumber.length == 12,
      errMsg = "capture-vat-application-number.error.message.invalid_length"
    ))

  private val vatApplicationNumberFormat: Constraint[String] = Constraint("vatApplicationNumber.invalid_format")(vatApplicationNumber =>
    validateNot(
      constraint = vatApplicationNumber.forall(_.isDigit),
      errMsg = "capture-vat-application-number.error.message.invalid_format"
    ))

//  val form: Form[String] = Form(
//    single(
//      vatApplicationNumber -> text.verifying(
//        vatApplicationNumberEmpty andThen
//          vatApplicationNumberFormat andThen vatApplicationNumberLength)
//    )
//  )
  val form: Form[String] = Form(
    single(
      vatApplicationNumber -> text
        .transform[String](_.replaceAll("\\s", ""), identity)
        .verifying(
          vatApplicationNumberEmpty andThen
            vatApplicationNumberFormat andThen
            vatApplicationNumberLength
        )
    )
  )
}
