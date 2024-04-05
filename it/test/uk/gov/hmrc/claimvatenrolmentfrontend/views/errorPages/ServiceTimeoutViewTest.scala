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

package uk.gov.hmrc.claimvatenrolmentfrontend.views.errorPages

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.libs.ws.WSResponse
import uk.gov.hmrc.claimvatenrolmentfrontend.assets.MessageLookup.{ServiceTimeoutError => messages}
import uk.gov.hmrc.claimvatenrolmentfrontend.utils.{ComponentSpecHelper, ViewSpecHelper}

trait ServiceTimeoutViewTest extends ViewSpecHelper {
  this: ComponentSpecHelper =>

  def testServiceTimeoutViewTest(result: => WSResponse): Unit = {

    lazy val doc: Document = {
      Jsoup.parse(result.body)
    }

    "have a view with the correct title" in {
      doc.title mustBe messages.title
    }

    "have the correct heading" in {
      doc.getH1Elements.first.text mustBe messages.heading
    }

    "have the correct text" in {
      doc.getParagraphs.get(1).text mustBe messages.line_1
      doc.getParagraphs.get(2).text mustBe messages.line_2
      doc.getParagraphs.get(3).text mustBe messages.line_3
      doc.getParagraphs.get(4).text mustBe messages.line_4
    }

    "have the correct link" in {
      doc.getLink(id = "businessTaxAccount").text mustBe messages.link
    }

  }
}
