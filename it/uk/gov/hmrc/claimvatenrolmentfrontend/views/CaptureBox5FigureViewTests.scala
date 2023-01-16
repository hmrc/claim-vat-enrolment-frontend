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

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.libs.ws.WSResponse
import uk.gov.hmrc.claimvatenrolmentfrontend.assets.MessageLookup.{Base, BetaBanner, Header, CaptureBox5Figure => messages}
import uk.gov.hmrc.claimvatenrolmentfrontend.utils.{ComponentSpecHelper, ViewSpecHelper}

trait CaptureBox5FigureViewTests extends ViewSpecHelper {
  this: ComponentSpecHelper =>

  def testCaptureBox5FigureViewTests(result: => WSResponse): Unit = {

    lazy val doc: Document = Jsoup.parse(result.body)

    "have a sign out link in the header" in {
      doc.getSignOutText mustBe Header.signOut
    }
    "have the correct beta banner" in {
      doc.getBanner.text mustBe BetaBanner.title
    }

    "have a view with the correct title" in {
      doc.title mustBe messages.title
    }

    "have the correct heading" in {
      doc.getH1Elements.first.text mustBe messages.heading
    }

    "have the correct text" in {
      doc.getParagraphs.get(2).text mustBe messages.line_1
    }

    "have the correct paragraph2" in {
      doc.getParagraphs.get(3).text mustBe messages.line_2
    }

    "have a continue button" in {
      doc.getSubmitButton.first.text mustBe Base.continue
    }
  }

  def testCaptureBox5MissingErrorViewTests(result: => WSResponse,
                                           authStub: => StubMapping): Unit = {

    lazy val doc: Document = {
      authStub
      Jsoup.parse(result.body)
    }

    "correctly display the error summary" in {
      doc.getErrorSummaryTitle.text mustBe Base.Error.title
      doc.getErrorSummaryBody.text mustBe messages.Error.noFigure
    }

    "correctly display the field error" in {
      doc.getFieldErrorMessage.text mustBe Base.Error.error + messages.Error.noFigure
    }
  }

  def testCaptureBox5InvalidLengthErrorViewTests(result: => WSResponse,
                                                 authStub: => StubMapping): Unit = {

    lazy val doc: Document = {
      authStub
      Jsoup.parse(result.body)
    }

    "correctly display the error summary" in {
      doc.getErrorSummaryTitle.text mustBe Base.Error.title
      doc.getErrorSummaryBody.text mustBe messages.Error.invalidLength
    }

    "correctly display the field error" in {
      doc.getFieldErrorMessage.text mustBe Base.Error.error + messages.Error.invalidLength
    }
  }

  def testCaptureBox5InvalidFormatErrorViewTests(result: => WSResponse,
                                                 authStub: => StubMapping): Unit = {

    lazy val doc: Document = {
      authStub
      Jsoup.parse(result.body)
    }

    "correctly display the error summary" in {
      doc.getErrorSummaryTitle.text mustBe Base.Error.title
      doc.getErrorSummaryBody.text mustBe messages.Error.invalidFormat
    }

    "correctly display the field error" in {
      doc.getFieldErrorMessage.text mustBe Base.Error.error + messages.Error.invalidFormat
    }
  }

}


