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

package uk.gov.hmrc.claimvatenrolmentfrontend.connectors


import play.api.mvc.Request
import uk.gov.hmrc.claimvatenrolmentfrontend.config.AppConfig
import uk.gov.hmrc.claimvatenrolmentfrontend.connectors.EnrolmentStoreProxyConnector._
import uk.gov.hmrc.claimvatenrolmentfrontend.httpparsers.QueryUsersHttpParser.QueryUsersSuccess
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import utils.LoggingUtil

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class EnrolmentStoreProxyConnector @Inject()(http: HttpClient,
                                             appConfig: AppConfig)(implicit ec: ExecutionContext) extends LoggingUtil {


  def getUserIds(vatNumber: String)(implicit hc: HeaderCarrier, request: Request[_]): Future[QueryUsersSuccess] = {
    infoLog(s"[EnrolmentStoreProxyConnector][getUserIds] Getting user IDs for VAT number $vatNumber")
    http.GET[QueryUsersSuccess](
      url = appConfig.queryUsersUrl(vatNumber),
      queryParams = Seq(principalQueryParameter))
  }
}

object EnrolmentStoreProxyConnector {
  val principalQueryParameter: (String, String) = "type" -> "principal"

}

