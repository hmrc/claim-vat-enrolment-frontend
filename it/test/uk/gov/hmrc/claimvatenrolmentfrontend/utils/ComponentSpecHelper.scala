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

package uk.gov.hmrc.claimvatenrolmentfrontend.utils

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsValue, Writes}
import play.api.libs.ws.{DefaultWSCookie, WSClient, WSCookie, WSRequest, WSResponse}
import play.api.mvc.{Cookie, Session, SessionCookieBaker}
import play.api.test.Helpers._
import play.api.test.Injecting
import uk.gov.hmrc.claimvatenrolmentfrontend.featureswitch.core.config.{FeatureSwitching, FeatureSwitchingModule}
import uk.gov.hmrc.claimvatenrolmentfrontend.featureswitch.core.models.FeatureSwitch
import uk.gov.hmrc.crypto.PlainText
import uk.gov.hmrc.http.SessionKeys
import uk.gov.hmrc.play.bootstrap.frontend.filters.crypto.SessionCookieCrypto

trait ComponentSpecHelper
    extends AnyWordSpec
    with Matchers
    with CustomMatchers
    with WiremockHelper
    with BeforeAndAfterAll
    with BeforeAndAfterEach
    with GuiceOneServerPerSuite
    with FeatureSwitching
    with Injecting {

  val mockHost: String = WiremockHelper.wiremockHost
  val mockPort: Int = WiremockHelper.wiremockPort
  val mockUrl: String  = s"http://$mockHost:$mockPort"

  def additionalConfig: Map[String, String] = Map.empty

  val config = Map(
    "auditing.enabled"                                  -> "false",
    "play.http.router"                                  -> "testOnlyDoNotUseInAppConf.Routes",
    "play.filters.csrf.header.bypassHeaders.Csrf-Token" -> "nocheck",
    "microservice.services.auth.host"                   -> mockHost,
    "microservice.services.auth.port"                   -> mockPort,
    "microservice.services.base.host"                   -> mockHost,
    "microservice.services.base.port"                   -> mockPort,
    "microservice.services.self.host"                   -> mockHost,
    "microservice.services.self.port"                   -> mockPort,
    "microservice.services.self.url"                    -> mockUrl,
    "microservice.services.tax-enrolments.host"         -> mockHost,
    "microservice.services.tax-enrolments.port"         -> mockPort,
    "microservice.services.enrolment-store-proxy.host"  -> mockHost,
    "microservice.services.enrolment-store-proxy.port"  -> mockPort,
    "microservice.services.business-account.host"       -> mockHost,
    "microservice.services.business-account.port"       -> mockPort,
    "feature-switch.knownFactsCheckFlag"                -> "true",
    "feature-switch.knownFactsCheckWithVanFlag"         -> "true"
  )

  override lazy val app: Application = new GuiceApplicationBuilder()
    .configure(config ++ additionalConfig)
    .build()

  implicit val ws: WSClient                    = app.injector.instanceOf[WSClient]
  lazy val featureSwitches: Seq[FeatureSwitch] = app.injector.instanceOf[FeatureSwitchingModule].switches

  override def beforeAll(): Unit = {
    startWiremock()
    super.beforeAll()
  }

  override def afterAll(): Unit = {
    stopWiremock()
    super.afterAll()
  }

  override def beforeEach(): Unit = {
    resetWiremock()
    featureSwitches.foreach(disable)
    super.beforeEach()
  }

  val cyLangCookie: WSCookie = DefaultWSCookie("PLAY_LANG", "cy")

  val enLangCookie: WSCookie = DefaultWSCookie("PLAY_LANG", "en")

  def get(uri: String, cookie: WSCookie = enLangCookie): WSResponse =
    await(
      buildClient(uri)
        .withCookies(cookie, mockSessionCookie)
        .get()
    )

  def post(uri: String, cookie: WSCookie = enLangCookie)(form: (String, String)*): WSResponse = {
    val formBody = (form map { case (k, v) => (k, Seq(v)) }).toMap
    await(
      buildClient(uri)
        .withHttpHeaders("Csrf-Token" -> "nocheck")
        .withCookies(cookie, mockSessionCookie)
        .post(formBody)
    )
  }

  def post(uri: String, json: JsValue): WSResponse =
    await(
      buildClient(uri)
        .withHttpHeaders("Content-Type" -> "application/json")
        .withCookies(mockSessionCookie)
        .post(json.toString())
    )

  def put[T](uri: String)(body: T)(implicit writes: Writes[T]): WSResponse =
    await(
      buildClient(uri)
        .withHttpHeaders("Content-Type" -> "application/json")
        .withCookies(mockSessionCookie)
        .put(writes.writes(body).toString())
    )

  val baseUrl: String = "/claim-vat-enrolment"

  private def buildClient(path: String): WSRequest =
    ws.url(s"http://localhost:$port$baseUrl$path").withFollowRedirects(false)

  def mockSessionCookie: WSCookie = {

    def makeSessionCookie(session: Session): Cookie = {
      val cookieCrypto   = inject[SessionCookieCrypto]
      val cookieBaker    = inject[SessionCookieBaker]
      val sessionCookie  = cookieBaker.encodeAsCookie(session)
      val encryptedValue = cookieCrypto.crypto.encrypt(PlainText(sessionCookie.value))
      sessionCookie.copy(value = encryptedValue.value)
    }

    val mockSession = Session(
      Map(
        SessionKeys.lastRequestTimestamp -> System.currentTimeMillis().toString,
        SessionKeys.authToken            -> "mock-bearer-token",
        SessionKeys.sessionId            -> "mock-sessionid"
      ))

    val cookie = makeSessionCookie(mockSession)

    new WSCookie() {
      override def name: String = cookie.name

      override def value: String = cookie.value

      override def domain: Option[String] = cookie.domain

      override def path: Option[String] = Some(cookie.path)

      override def maxAge: Option[Long] = cookie.maxAge.map(_.toLong)

      override def secure: Boolean = cookie.secure

      override def httpOnly: Boolean = cookie.httpOnly
    }
  }

}
