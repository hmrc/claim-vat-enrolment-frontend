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

object CaptureBox5FigureForm {

  val box5Figure: String = "box5_figure"
  val box5FigureRegex = "^-?([0-9]*)([.])([0-9]{2})*$"
  val defaultMaxLength = 14
  val negativeValueMaxLength = 15

  val box5FigureEmpty: Constraint[String] = Constraint("box5_figure.empty")(
    box5Figure => validate(
      constraint = box5Figure.isEmpty,
      errMsg = "capture_box5_figure.empty.error"
    )
  )

  val box5FigureLength: Constraint[String] = Constraint("box5_figure.invalid_length")(
    box5Figure => validateNot(
      constraint = if (box5Figure.contains("-")) box5Figure.length <= negativeValueMaxLength else box5Figure.length <= defaultMaxLength,
      errMsg = "capture_box5_figure.invalid_length.error"
    )
  )

  val box5FigureFormat: Constraint[String] = Constraint("box5_figure.invalid_format")(
    box5Figure => validateNot(
      constraint = box5Figure matches box5FigureRegex,
      errMsg = "capture_box5_figure.invalid_format.error"
    )
  )

  val form: Form[String] = Form(
    single(
      box5Figure -> text
        .transform[String](_.replaceAll(",",""), identity)
        .verifying(box5FigureEmpty andThen box5FigureLength andThen box5FigureFormat)
    )
  )
}

