/*
 * Copyright 2025 HM Revenue & Customs
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

package uk.gov.hmrc.claimvatenrolmentfrontend.models

import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.libs.json.{JsValue, Json}

class JourneySubmissionSpec extends PlaySpec with GuiceOneAppPerSuite with MockitoSugar {


  "JourneySubmission JSON format" should {

    "serialize a JourneySubmission to JSON" in {
      val attempt = JourneySubmission(
        journeyId = "abc-123",
        vrn = "123456789",
        submissionNumber = 1,
        accountStatus = "UnLocked")

      val json = Json.toJson(attempt)

      (json \ "journeyId").as[String] mustBe "abc-123"
      (json \ "vrn").as[String] mustBe "123456789"
      (json \ "submissionNumber").as[Int] mustBe 1
      (json \ "accountStatus").as[String] mustBe "UnLocked"
    }

    "deserialize JSON into a JourneySubmission" in {
      val json = Json.obj(
        "journeyId" -> "abc-123",
        "vrn" -> "123456789",
        "submissionNumber" -> 1,
        "accountStatus" -> "UnLocked"
      )

      val result = json.as[JourneySubmission]

      result.journeyId mustBe "abc-123"
      result.vrn mustBe "123456789"
      result.submissionNumber mustBe 1
      result.accountStatus mustBe "UnLocked"
    }

    "round-trip JSON serialization/deserialization" in {
      val attempt = JourneySubmission(
        journeyId = "abc-123",
        vrn = "123456789",
        submissionNumber = 1,
        accountStatus = "UnLocked")


      val json: JsValue = Json.toJson(attempt)
      val roundTripped: JourneySubmission = json.as[JourneySubmission]

      roundTripped mustBe attempt
    }
  }

}
