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

package uk.gov.hmrc.claimvatenrolmentfrontend.views.helpers

import play.api.data.Form
import play.api.i18n.Messages

import java.time.LocalDate

object ViewUtils {

  def title(form: Form[_], titleStr: String, section: Option[String] = None, titleMessageArgs: Seq[String] = Seq())
           (implicit messages: Messages): String = {
      titleNoForm(s"${errorPrefix(form)} ${messages(titleStr, titleMessageArgs:_*)}", section)
  }

  def titleNoForm(title: String, section: Option[String] = None, titleMessageArgs: Seq[String] = Seq())(implicit messages: Messages): String =
    s"${messages(title, titleMessageArgs:_*)} - ${section.fold("")(messages(_) + " - ")}${messages("app.service_name")} - ${messages("app.govuk")}"

  def errorPrefix(form: Form[_])(implicit messages: Messages): String = {
    if (form.hasErrors || form.hasGlobalErrors) messages("app.title.error_prefix") else ""
  }

  def dateFormat(date: LocalDate)(implicit messages: Messages): String = {
    val day = date.getDayOfMonth
    val month = messages(s"capture-last-month-submitted.${date.getMonth}")
    val year = date.getYear

    s"$day $month $year"
  }

}
