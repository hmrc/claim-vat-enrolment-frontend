/*
 * Copyright 2022 HM Revenue & Customs
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
import uk.gov.hmrc.claimvatenrolmentfrontend.featureswitch.core.config.{AllocateEnrolmentStub, FeatureSwitching}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import javax.inject.{Inject, Singleton}

@Singleton
class AppConfig @Inject()(config: Configuration, servicesConfig: ServicesConfig) extends FeatureSwitching {

  val welshLanguageSupportEnabled: Boolean = config.getOptional[Boolean]("features.welsh-language-support").getOrElse(false)

  val en: String = "en"
  val cy: String = "cy"
  val defaultLanguage: Lang = Lang(en)
  val timeToLiveSeconds: Int = servicesConfig.getInt("mongodb.timeToLiveSeconds")

  lazy val timeout: Int = servicesConfig.getInt("timeout.timeout")
  lazy val countdown: Int = servicesConfig.getInt("timeout.countdown")

  lazy val selfBaseUrl: String = servicesConfig.baseUrl("self")
  lazy val selfUrl: String = servicesConfig.getString("microservice.services.self.url")

  lazy val exitSurveyServiceIdentifier = "MTDfB-VAT-sign-up"
  lazy val feedbackFrontendUrl: String = servicesConfig.getString("microservice.services.feedback-frontend.url")
  lazy val feedbackUrl = s"$feedbackFrontendUrl/feedback/$exitSurveyServiceIdentifier"

  val contactFormServiceIdentifier = "MTDVAT"
  lazy val contactFrontendUrl: String = servicesConfig.getString("microservice.services.contact-frontend.url")
  lazy val reportAProblemNonJSUrl = s"$contactFrontendUrl/contact/problem_reports_nonjs?service=$contactFormServiceIdentifier"
  lazy val betaFeedbackUrl = s"$contactFrontendUrl/contact/beta-feedback?service=$contactFormServiceIdentifier"

  lazy val businessTaxAccountAddVatUrl = "/business-account/add-tax/vat/what-is-your-vat-number"

  lazy val btaBaseUrl: String = servicesConfig.getString("microservice.services.business-account.url") + "/business-account"

  lazy val businessTaxAccountUrl = "/tax-and-scheme-management/services"

  lazy val taxEnrolmentsUrl: String = servicesConfig.baseUrl("tax-enrolments") + "/tax-enrolments"

  def allocateEnrolmentUrl(groupId: String, enrolmentKey: String): String = {
    val baseUrl = if (isEnabled(AllocateEnrolmentStub)) s"$selfBaseUrl/claim-vat-enrolment/test-only" else taxEnrolmentsUrl
    baseUrl + s"/groups/$groupId/enrolments/$enrolmentKey"
  }

  lazy val enrolmentStoreProxyUrl: String = servicesConfig.baseUrl("enrolment-store-proxy") + "/enrolment-store-proxy/enrolment-store"

  def queryUserIdStub(vatNumber: String): String = {
    val baseUrl = if (isEnabled(AllocateEnrolmentStub)) s"$selfBaseUrl/claim-vat-enrolment/test-only" else enrolmentStoreProxyUrl
    baseUrl + s"/enrolments/HMRC-MTD-VAT~VRN~$vatNumber/users"
  }


}
