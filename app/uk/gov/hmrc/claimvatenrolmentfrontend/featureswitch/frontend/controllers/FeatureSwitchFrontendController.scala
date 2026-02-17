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

package uk.gov.hmrc.claimvatenrolmentfrontend.featureswitch.frontend.controllers

import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import uk.gov.hmrc.claimvatenrolmentfrontend.config.AppConfig
import uk.gov.hmrc.claimvatenrolmentfrontend.featureswitch.core.config.FeatureSwitching
import uk.gov.hmrc.claimvatenrolmentfrontend.featureswitch.frontend.services.FeatureSwitchRetrievalService
import uk.gov.hmrc.claimvatenrolmentfrontend.featureswitch.frontend.views.html.feature_switch

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class FeatureSwitchFrontendController @Inject()(featureSwitchService: FeatureSwitchRetrievalService,
                                                featureSwitchView: feature_switch,
                                                mcc: MessagesControllerComponents
                                               )(implicit ec: ExecutionContext,
                                                 appConfig: AppConfig) extends FrontendController(mcc) with FeatureSwitching with I18nSupport {


  def show: Action[AnyContent] = Action.async {
    implicit req =>
      featureSwitchService.retrieveFeatureSwitches().map {
        featureSwitches =>
          Ok(featureSwitchView(featureSwitches, routes.FeatureSwitchFrontendController.submit))
      }
  }

  def submit: Action[Map[String, Seq[String]]] = Action.async(parse.formUrlEncoded) {
    implicit req =>
      featureSwitchService.updateFeatureSwitches(req.body.keys).map {
        featureSwitches =>
          Ok(featureSwitchView(featureSwitches, routes.FeatureSwitchFrontendController.submit))
      }
  }
}
