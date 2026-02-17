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

package uk.gov.hmrc.claimvatenrolmentfrontend.views.errorPages

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.libs.ws.WSResponse
import uk.gov.hmrc.claimvatenrolmentfrontend.assets.MessageLookup.{Header, ThirdAttemptLockout => messages}
import uk.gov.hmrc.claimvatenrolmentfrontend.utils.{ComponentSpecHelper, ViewSpecHelper}

trait ThirdAttemptLockoutViewTests extends ViewSpecHelper {
  this: ComponentSpecHelper =>

  def testThirdAttemptLockoutView(result: => WSResponse): Unit = {

    lazy val doc: Document = {
      Jsoup.parse(result.body)
    }

    "have a view with the correct title" in {
      doc.title mustBe messages.title
    }

    "have a sign out link in the header" in {
      doc.getSignOutText mustBe Header.signOut
    }

    "have the correct heading" in {
      doc.getH1Elements.first.text mustBe messages.heading
    }

    "have the correct text" in {
      doc.getParagraphs.get(1).text mustBe messages.line_1
      doc.getParagraphs.get(2).text mustBe messages.line_2
      doc.getParagraphs.get(3).text mustBe messages.line_3
    }

    "have a correct button" in {
      doc.getSubmitButton.first.text mustBe messages.button_text
    }

    "have the correct link" in {
      doc.getLink(id = "back-to-bta").attr("href") mustBe messages.link
    }
  }
}
