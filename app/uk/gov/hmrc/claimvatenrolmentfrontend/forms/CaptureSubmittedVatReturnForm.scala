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

package uk.gov.hmrc.claimvatenrolmentfrontend.forms

import play.api.data.Forms.of
import play.api.data.format.Formatter
import play.api.data.{Form, FormError}

object CaptureSubmittedVatReturnForm {

  def booleanFormatter(errorKey: String): Formatter[Boolean] =
    new Formatter[Boolean] {

      override def bind(key: String, data: Map[String, String]) =
        data.get(key) match {
          case None | Some("") => Left(Seq(FormError(key, errorKey)))
          case Some("yes") => Right(true)
          case Some("no") => Right(false)
          case _ => Left(Seq(FormError(key, errorKey)))
        }

      def unbind(key: String, value: Boolean) = Map(key -> value.toString)
    }

  val form: Form[Boolean] =
    Form(
      "vat_return" -> of(booleanFormatter("capture-submitted-vat-return.error.message"))
    )

}
