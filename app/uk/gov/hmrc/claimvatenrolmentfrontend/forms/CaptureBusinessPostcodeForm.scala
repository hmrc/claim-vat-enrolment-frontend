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

package uk.gov.hmrc.claimvatenrolmentfrontend.forms

import play.api.data.Form
import play.api.data.Forms._
import play.api.data.validation.Constraint
import uk.gov.hmrc.claimvatenrolmentfrontend.forms.utils.ConstraintUtil.ConstraintUtil
import uk.gov.hmrc.claimvatenrolmentfrontend.forms.utils.ValidationHelper.{validate, validateNot}
import uk.gov.hmrc.claimvatenrolmentfrontend.models.Postcode

object CaptureBusinessPostcodeForm {

  val businessPostcode: String = "business_postcode"
  val postCodeRegex = "^[A-Z]{1,2}[0-9][0-9A-Z]?[ ]?[0-9][A-Z]{2}$"

  val businessPostcodeEmpty: Constraint[String] = Constraint("business_postcode.not_entered")(
    businessPostcode => validate(
      constraint = businessPostcode.isEmpty,
      errMsg = "capture-business-postcode.error.emptyPostcode"
    )
  )

  val businessPostcodeFormat: Constraint[String] = Constraint("business_postcode.invalid_format")(
    businessPostcode => validateNot(
      constraint = businessPostcode.toUpperCase.replaceAll(" ", "") matches postCodeRegex,
      errMsg = "capture-business-postcode.error.emptyPostcode"
    )
  )

  val form: Form[Postcode] = Form(
    mapping(
      businessPostcode -> text.verifying(businessPostcodeEmpty andThen businessPostcodeFormat)
    )(Postcode.apply)(Postcode.unapply)
  )

}
