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

package uk.gov.hmrc.claimvatenrolmentfrontend.controllers

import play.api.http.HeaderNames
import play.api.test.Helpers._
import uk.gov.hmrc.claimvatenrolmentfrontend.assets.TestConstants.testJourneyId
import uk.gov.hmrc.claimvatenrolmentfrontend.stubs.AuthStub
import uk.gov.hmrc.claimvatenrolmentfrontend.utils.ComponentSpecHelper

class SignInOutControllerISpec extends ComponentSpecHelper with AuthStub {

  val host: String = s"http://localhost:$port"
  val signOutUrl: String = s"$host$baseUrl/sign-out"
  val keepAliveUrl: String = s"$host$baseUrl/keep-alive"
  val refererUrl: String = s"$host$baseUrl/somePage"

  s"GET /sign-out" should {
    "return 303, and redirect to the feedback page" in {
      lazy val result = await(ws.url(signOutUrl)
        .withHttpHeaders((HeaderNames.REFERER, refererUrl))
        .withFollowRedirects(false).get())

      result.status mustBe SEE_OTHER

      result.header(HeaderNames.LOCATION) mustBe Some("http://localhost:9553/bas-gateway/sign-out-without-state?continue=http://localhost:9514/feedback/claim-vat-enrolment")
    }
  }

  s"GET /keep-alive" should {
    "return 200" in {

      lazy val result = await(ws.url(keepAliveUrl)
        .withHttpHeaders((HeaderNames.REFERER, refererUrl))
        .withFollowRedirects(false).get())

      result.status mustBe OK
    }
  }

  s"AuthExceptions should redirect a user to GG Login" should {
    "return 200" in {
      stubAuthFailure()

      lazy val result = get(s"/$testJourneyId/vat-application-number")

      result.status mustBe SEE_OTHER
      result.header(HeaderNames.LOCATION).get must include("http://localhost:9553/bas-gateway/sign-in?continue_url=")
    }
  }
}