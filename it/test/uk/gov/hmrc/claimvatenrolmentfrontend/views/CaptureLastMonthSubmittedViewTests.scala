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

package uk.gov.hmrc.claimvatenrolmentfrontend.views

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.libs.ws.WSResponse
import uk.gov.hmrc.claimvatenrolmentfrontend.assets.MessageLookup.{Base, Header, CaptureLastMonthSubmitted => messages}
import uk.gov.hmrc.claimvatenrolmentfrontend.utils.{ComponentSpecHelper, ViewSpecHelper}

import scala.jdk.CollectionConverters._

trait CaptureLastMonthSubmittedViewTests extends ViewSpecHelper {
  this: ComponentSpecHelper =>

  def testCaptureLastMonthSubmittedViewTests(result: => WSResponse): Unit = {
    lazy val doc: Document = {
      Jsoup.parse(result.body)
    }
    "have a sign out link in the header" in { doc.getSignOutText mustBe Header.signOut }
    "have a view with the correct title" in { doc.title mustBe messages.title }
    "have the correct heading" in { doc.getH1Elements.first.text mustBe messages.heading }
    "have the correct first line" in { doc.getElementsByClass("govuk-body").first.text mustBe messages.firstLine }
    "have the correct panel" in {
      val panel = doc.getInsetText.first
      panel.getParagraphs.first.text mustBe messages.panelFirstHeading
      panel.getParagraphs.get(1).text mustBe messages.panelFirstText
      panel.getParagraphs.get(2).text mustBe messages.panelSecondHeading
      panel.getParagraphs.last.text mustBe messages.panelSecondText
    }
    "have the correct hint" in { doc.getHintText.text mustBe messages.hint }
    "have the correct radio button labels" should {
      lazy val radioLabels = doc.getRadioLabels.iterator().asScala.toList
      "have 12 radio buttons" in { radioLabels.size mustBe 12 }
      "have a January label" in { radioLabels.head.text mustBe messages.januaryLabel }
      "have a February label" in { radioLabels(1).text mustBe messages.februaryLabel }
      "have a March label" in { radioLabels(2).text mustBe messages.marchLabel }
      "have a April label" in { radioLabels(3).text mustBe messages.aprilLabel }
      "have a May label" in { radioLabels(4).text mustBe messages.mayLabel }
      "have a June label" in { radioLabels(5).text mustBe messages.juneLabel }
      "have a July label" in { radioLabels(6).text mustBe messages.julyLabel }
      "have a August label" in { radioLabels(7).text mustBe messages.augustLabel }
      "have a September label" in { radioLabels(8).text mustBe messages.septemberLabel }
      "have a October label" in { radioLabels(9).text mustBe messages.octoberLabel }
      "have a November label" in { radioLabels(10).text mustBe messages.novemberLabel }
      "have a December label" in { radioLabels(11).text mustBe messages.decemberLabel }
    }
    "have a continue button" in { doc.getSubmitButton.first.text mustBe Base.continue }
  }

  def testCaptureLastMonthSubmittedErrorViewTests(result: => WSResponse,
                                                  authStub: => StubMapping): Unit = {
    lazy val doc: Document = {
      authStub
      Jsoup.parse(result.body)
    }
    "correctly display the error summary" in {
      doc.getErrorSummaryTitle.text mustBe Base.Error.title
      doc.getErrorSummaryBody.text mustBe messages.Error.noMonthSelected
    }

    "correctly display the field error" in {
      doc.getFieldErrorMessage.text mustBe Base.Error.error + messages.Error.noMonthSelected
    }
  }
}
