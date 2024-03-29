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

package uk.gov.hmrc.claimvatenrolmentfrontend.forms.utils

import play.api.data.FormError
import play.api.data.format.Formatter

import java.lang.Integer.parseInt
import java.time.Month
import scala.util.{Failure, Success, Try}
object MonthMapping {
  def monthMapping(error: String): Formatter[Month] = new Formatter[Month] {
    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], Month] = {
      Try(Month.of(parseInt(data(key)))) match {
        case Success(month) => Right(month)
        case Failure(_) => Left(Seq(FormError(key, error)))
      }
    }

    override def unbind(key: String, month: Month): Map[String, String] = {
      Map(key -> month.getValue.toString)
    }
  }

}
