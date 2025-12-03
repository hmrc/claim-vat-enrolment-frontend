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

package uk.gov.hmrc.claimvatenrolmentfrontend.config

import play.api.Configuration
import play.api.i18n.Lang
import uk.gov.hmrc.claimvatenrolmentfrontend.featureswitch.core.config.{AllocateEnrolmentStub, FeatureSwitching, KnownFactsCheckFlag, QueryUserIdStub}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import javax.inject.{Inject, Singleton}

@Singleton
class AppConfig @Inject()(config: Configuration, servicesConfig: ServicesConfig) extends FeatureSwitching {

  private def loadConfig(key: String) = servicesConfig.getString(key)

  val welshLanguageSupportEnabled: Boolean = config.getOptional[Boolean]("features.welsh-language-support").getOrElse(false)

  val en: String = "en"
  val cy: String = "cy"
  val defaultLanguage: Lang = Lang(en)
  val timeToLiveSeconds: Int = servicesConfig.getInt("mongodb.timeToLiveSeconds")
  val ttlLockSeconds: Int = servicesConfig.getInt("mongodb.ttlLockSeconds")

  lazy val timeout: Int = servicesConfig.getInt("timeout.timeout")
  lazy val countdown: Int = servicesConfig.getInt("timeout.countdown")

  lazy val selfBaseUrl: String = servicesConfig.baseUrl("self")
  lazy val selfUrl: String = servicesConfig.getString("microservice.services.self.url")

  lazy val exitSurveyServiceIdentifier = "claim-vat-enrolment"
  lazy val feedbackFrontendUrl: String = servicesConfig.getString("microservice.services.feedback-frontend.url")
  lazy val feedbackUrl = s"$feedbackFrontendUrl/feedback/$exitSurveyServiceIdentifier"
  private lazy val basGatewayUrl: String = loadConfig(s"bas-gateway-frontend.host")
  private val signOutUri: String = loadConfig("sign-out.uri")
  lazy val signOutUrl: String = s"$basGatewayUrl$signOutUri"

  val contactFormServiceIdentifier = "cve"
  lazy val contactFrontendUrl: String = servicesConfig.getString("microservice.services.contact-frontend.url")
  lazy val betaFeedbackUrl = s"$contactFrontendUrl/contact/beta-feedback?service=$contactFormServiceIdentifier"

  lazy val businessTaxAccountAddVatUrl = "/business-account/add-tax/vat/what-is-your-vat-number"

  lazy val addVatUrl = "/business-account/add-tax/vat/interstitial"

  lazy val btaBaseUrl: String = servicesConfig.getString("microservice.services.business-account.url") + "/business-account"

  lazy val businessTaxAccountUrl = "/tax-and-scheme-management/services"
  lazy val accountRecoveryUrl = "https://www.access.service.gov.uk/account-recovery/forgot-userid-password/check-your-emails"

  lazy val taxEnrolmentsUrl: String = servicesConfig.baseUrl("tax-enrolments") + "/tax-enrolments"

  lazy val webchatUrl: String = servicesConfig.getString("digital-engagement-platform-frontend.host") + servicesConfig.getString("webchat.endpoint")
  lazy val webchatEnabled: Boolean = config.getOptional[Boolean]("feature-switch.webchat-enabled").getOrElse(false)

  lazy val knownFactsCheckFlag: Boolean = servicesConfig.getBoolean("feature-switch.knownFactsCheckFlag")

  def allocateEnrolmentUrl(groupId: String, enrolmentKey: String): String = {
    val baseUrl = if (isEnabled(AllocateEnrolmentStub)) s"$selfBaseUrl/claim-vat-enrolment/test-only" else taxEnrolmentsUrl
    baseUrl + s"/groups/$groupId/enrolments/$enrolmentKey"
  }

  lazy val enrolmentStoreProxyUrl: String = servicesConfig.baseUrl("enrolment-store-proxy") + "/enrolment-store-proxy/enrolment-store"

  def queryUsersUrl(vatNumber: String): String = {
    val baseUrl = if (isEnabled(QueryUserIdStub)) s"$selfBaseUrl/claim-vat-enrolment/test-only" else enrolmentStoreProxyUrl
    baseUrl + s"/enrolments/HMRC-MTD-VAT~VRN~$vatNumber/users"
  }

  lazy val isKnownFactsCheckEnabled: Boolean = isEnabled(KnownFactsCheckFlag)

}
