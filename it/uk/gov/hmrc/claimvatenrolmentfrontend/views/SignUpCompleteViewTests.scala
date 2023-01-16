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

package uk.gov.hmrc.claimvatenrolmentfrontend.views

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.libs.ws.WSResponse
import uk.gov.hmrc.claimvatenrolmentfrontend.assets.MessageLookup.{Base, BetaBanner, Header, SignUpCompleteClient => messages}
import uk.gov.hmrc.claimvatenrolmentfrontend.config.AppConfig
import uk.gov.hmrc.claimvatenrolmentfrontend.utils.{ComponentSpecHelper, ViewSpecHelper}

trait SignUpCompleteViewTests extends ViewSpecHelper {
  this: ComponentSpecHelper =>

  def testSignUpCompleteViewTests(result: => WSResponse): Unit = {

    lazy val config = app.injector.instanceOf[AppConfig]

    lazy val doc: Document = {
      Jsoup.parse(result.body)
    }

    "have a view with the correct title" in {
      doc.title mustBe messages.title
    }

    "have a sign out link in the header" in {
      doc.getSignOutText mustBe Header.signOut
    }

    "have the correct beta banner" in {
      doc.getBanner.text mustBe BetaBanner.title
    }

    "have the correct heading" in {
      doc.getH1Elements.first.text mustBe messages.heading
    }

    "have the correct text" in {
      doc.getParagraphs.get(2).text mustBe messages.line_1
    }

    "have the correct first bullet point" in {
      doc.getBulletPoints.get(2).text mustBe messages.bullet1
    }

    "have the correct second bullet point" in {
      doc.getBulletPoints.get(3).text mustBe messages.bullet2
    }

    "have the correct third bullet point" in {
      doc.getBulletPoints.get(4).text mustBe messages.bullet3
    }

    "have the correct fourth bullet point" in {
      doc.getBulletPoints.get(5).text mustBe messages.bullet4
    }

    "have the correct button" in {
      doc.getSubmitButton.first.text mustBe Base.continue
      doc.getElementById("Continue").attr("href") mustBe config.btaBaseUrl
    }

  }
}
