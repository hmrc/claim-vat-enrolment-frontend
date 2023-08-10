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

package uk.gov.hmrc.claimvatenrolmentfrontend.models

import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite

class PostcodeSpec extends PlaySpec with GuiceOneAppPerSuite with MockitoSugar {
  val testPostcode1: Postcode = Postcode("AA1 1AA")
  val testPostcode2: Postcode = Postcode("    AA1 1AA")
  val testPostcode3: Postcode = Postcode(" A A 1 1 A A ")
  val testPostcode4: Postcode = Postcode("AA1 1AA    ")

  "postcodes" should {
    "match given same postcode" in {
      testPostcode1.sanitisedPostcode mustBe "AA1 1AA"
      testPostcode2.sanitisedPostcode mustBe "AA1 1AA"
      testPostcode3.sanitisedPostcode mustBe "AA1 1AA"
      testPostcode4.sanitisedPostcode mustBe "AA1 1AA"
    }
  }



}
