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

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.libs.json.{JsString, Json, Writes}
import play.api.test.Helpers._
import uk.gov.hmrc.claimvatenrolmentfrontend.assets.TestConstants._
import uk.gov.hmrc.claimvatenrolmentfrontend.models.VatKnownFacts
import uk.gov.hmrc.claimvatenrolmentfrontend.repositories.JourneyDataRepository._
import uk.gov.hmrc.claimvatenrolmentfrontend.stubs.AuthStub
import uk.gov.hmrc.claimvatenrolmentfrontend.utils.JourneyMongoHelper
import uk.gov.hmrc.claimvatenrolmentfrontend.views.CaptureSubmittedVatReturnViewTests

import java.time.{Instant, Month}

class CaptureSubmittedVatReturnControllerISpec extends JourneyMongoHelper with CaptureSubmittedVatReturnViewTests with AuthStub {

  "GET /:testJourneyId/submitted-vat-return" should {
    "show the CaptureSubmittedVatReturn page with correct content in an Ok response" when {
      "the view is rendered with a form without errors" must {
        lazy val result = {
          await(insertVatKnownFactsData(testJourneyId, testInternalId, baseVatKnownFacts))
          stubAuth(OK, successfulAuthResponse(Some(testGroupId), Some(testInternalId)))
          get(s"/$testJourneyId/submitted-vat-return")
        }

        checkOkResponseAndCorrectContent(result)
      }
    }

    "redirect to the ServiceTimeout error page" when {
      "there is no Journey Config" in {
        stubSuccessfulAuth
        lazy val result = get(s"/$testJourneyId/submitted-vat-return")

        result must have(
          httpStatus(SEE_OTHER),
          redirectUri(errorPages.routes.ServiceTimeoutController.show().url)
        )
      }
      "the internal IDs do not match" in {
        stubSuccessfulAuth
        await(insertJourneyConfig(testJourneyId, testContinueUrl, "testInternalId"))

        lazy val result = get(s"/$testJourneyId/submitted-vat-return")

        result must have(
          httpStatus(SEE_OTHER),
          redirectUri(errorPages.routes.ServiceTimeoutController.show().url)
        )
      }
      "the journey ID has no internal ID stored" in {
        stubSuccessfulAuth
        await(
          journeyConfigRepository.collection
            .insertOne(
              Json.obj(
                "_id"               -> testJourneyId,
                "creationTimestamp" -> Json.obj("$date" -> Instant.now.toEpochMilli)
              ) ++ Json.toJsObject(testJourneyConfig)
            )
            .toFuture())

        lazy val result = get(s"/$testJourneyId/submitted-vat-return")

        result must have(
          httpStatus(SEE_OTHER),
          redirectUri(errorPages.routes.ServiceTimeoutController.show().url)
        )
      }
    }

    "return an INTERNAL_SERVER_ERROR" when {
      "there is no auth ID" in {
        await(insertVatKnownFactsData(testJourneyId, testInternalId, baseVatKnownFacts))
        stubAuth(OK, successfulAuthResponse(None))
        lazy val result = get(s"/$testJourneyId/submitted-vat-return")

        result.status mustBe INTERNAL_SERVER_ERROR
      }
    }
  }

  "POST /:testJourneyId/submitted-vat-return" should {
    "redirect to CaptureBox5Figure page and save the user answer when the user selects 'yes'" when {
      "there is no prior journey data for the 'no' journey" in {
        stubSuccessfulAuth
        createDataWithVatNumber

        lazy val result = post(s"/$testJourneyId/submitted-vat-return")("vat_return" -> "yes")

        result must have(
          httpStatus(SEE_OTHER),
          redirectUri(routes.CaptureBox5FigureController.show(testJourneyId).url)
        )
      }
      "there is prior journey data for the 'no' journey which is then deleted" in {
        stubSuccessfulAuth
        createDataWithVatNumber
        addJourneyDataField(SubmittedVatApplicationNumberKey, testFormBundleReference)

        lazy val result = post(s"/$testJourneyId/submitted-vat-return")("vat_return" -> "yes")

        result must have(
          httpStatus(SEE_OTHER),
          redirectUri(routes.CaptureBox5FigureController.show(testJourneyId).url)
        )

        val journeyData: Option[VatKnownFacts] = checkJourneyData()
        journeyData.flatMap(_.formBundleReference) mustBe None
      }
    }

    "redirect to CaptureVatApplicationNumber page and save the user answer the user selects 'no'" when {
      "there is no prior journey data for the 'yes' journey" in {
        stubSuccessfulAuth
        createDataWithVatNumber

        lazy val result = post(s"/$testJourneyId/submitted-vat-return")("vat_return" -> "no")

        result must have(
          httpStatus(SEE_OTHER),
          redirectUri(routes.CaptureVatApplicationNumberController.show(testJourneyId).url)
        )
      }
      "there is prior journey data for the 'yes' journey which is then deleted" in {
        stubSuccessfulAuth
        createDataWithVatNumber
        addJourneyDataField(Box5FigureKey, testBoxFive)
        addJourneyDataField(LastMonthSubmittedKey, testLastReturnMonth)

        lazy val result = post(s"/$testJourneyId/submitted-vat-return")("vat_return" -> "no")

        result must have(
          httpStatus(SEE_OTHER),
          redirectUri(routes.CaptureVatApplicationNumberController.show(testJourneyId).url)
        )

        val journeyData: Option[VatKnownFacts] = checkJourneyData()
        journeyData.flatMap(_.optReturnsInformation) mustBe None
      }
    }

    "render the page with error content in a BadRequest response" when {
      "the user tries to submit empty data" must {
        lazy val result = {
          stubSuccessfulAuth
          post(s"/$testJourneyId/submitted-vat-return")()
        }

        checkBadRequestResponseAndCorrectErrorContent(result)
      }
    }

    "return an INTERNAL_SERVER_ERROR" when {
      "there is no journey data" in {
        stubSuccessfulAuth

        lazy val result = post(s"/$testJourneyId/submitted-vat-return")("vat_return" -> "yes")

        result.status mustBe INTERNAL_SERVER_ERROR
      }

      "there is no auth ID" in {
        stubAuth(OK, successfulAuthResponse(None))

        lazy val result = post(s"/$testJourneyId/submitted-vat-return")("vat_return" -> "yes")

        result.status mustBe INTERNAL_SERVER_ERROR
      }
    }
  }

  implicit val monthWrites: Writes[Month] =
    Writes(month => JsString(month.toString))

  private def stubSuccessfulAuth: StubMapping = stubAuth(OK, successfulAuthResponse(Some(testGroupId), Some(testInternalId)))

  private def checkJourneyData(): Option[VatKnownFacts] = await(journeyDataRepository.getJourneyData(testJourneyId, testInternalId))

  private def createDataWithVatNumber: String =
    await(journeyDataRepository.insertJourneyVatNumber(testJourneyId, testInternalId, testVatNumber))

  private def addJourneyDataField[T](key: String, value: T)(implicit writes: Writes[T]): Boolean =
    await(journeyDataRepository.updateJourneyData(testJourneyId, key, Json.toJson(value), testInternalId))

}
