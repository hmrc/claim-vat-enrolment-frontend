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

package uk.gov.hmrc.claimvatenrolmentfrontend.controllers

import play.api.i18n.MessagesApi
import play.api.test.Helpers._
import play.api.http.HeaderNames
import play.api.libs.ws.WSCookie
import uk.gov.hmrc.claimvatenrolmentfrontend.utils.ComponentSpecHelper

class LanguageSwitchControllerISpec extends ComponentSpecHelper {

  val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]

  val playCookieName: String = messagesApi.langCookieName

  val host: String = s"http://localhost:$port"

  val getEnUrl: String = s"$host$baseUrl/language/en"
  val getCyUrl: String = s"$host$baseUrl/language/cy"

  val refererUrl: String = s"$host$baseUrl/somePage"

  val defaultFallBackUrl: String = "https://www.gov.uk/government/organisations/hm-revenue-customs"

  def confirmLangCookie(cookie: Option[WSCookie], lang: String): Unit =
    cookie match {
      case Some(cookie) => cookie.value mustBe lang
      case None => fail(s"Missing $playCookieName cookie")
    }

  s"GET /language/cy" should {

    "return 303, set the language to welsh and redirect to the specified referer" in {

      lazy val result = await(ws.url(getCyUrl)
        .withHttpHeaders((HeaderNames.REFERER, refererUrl))
        .withFollowRedirects(false).get())

      result.status mustBe SEE_OTHER

      confirmLangCookie(result.cookie(playCookieName), "cy")

      result.header(HeaderNames.LOCATION) mustBe Some(s"$baseUrl/somePage")
    }

    "return 303, set the language to welsh and redirect to the fallback url" in {

      lazy val result = await(ws.url(getCyUrl)
        .withFollowRedirects(false).get())

      result.status mustBe SEE_OTHER

      confirmLangCookie(result.cookie(playCookieName), "cy")

      result.header(HeaderNames.LOCATION) mustBe Some(defaultFallBackUrl)
    }

  }

  s"GET /language/en" should {

    "return 303, set the language to english and redirect to the specified referer" in {

      lazy val result = await(ws.url(getEnUrl)
        .withHttpHeaders((HeaderNames.REFERER, refererUrl))
        .withFollowRedirects(false).get())

      result.status mustBe SEE_OTHER

      confirmLangCookie(result.cookie(playCookieName), "en")

      result.header(HeaderNames.LOCATION) mustBe Some(s"$baseUrl/somePage")
    }

    "return 303, set the language to english and redirect to the fallback url" in {

      lazy val result = await(ws.url(getEnUrl)
        .withFollowRedirects(false).get())

      result.status mustBe SEE_OTHER

      confirmLangCookie(result.cookie(playCookieName), "en")

      result.header(HeaderNames.LOCATION) mustBe Some(defaultFallBackUrl)
    }

  }

}