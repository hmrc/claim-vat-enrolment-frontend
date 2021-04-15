/*
 * Copyright 2021 HM Revenue & Customs
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


import javax.inject.{Inject, Singleton}
import uk.gov.hmrc.claimvatenrolmentfrontend.config.AppConfig
import uk.gov.hmrc.claimvatenrolmentfrontend.connectors.EnrolmentStoreProxyConnector._
import uk.gov.hmrc.claimvatenrolmentfrontend.httpparsers.QueryUsersHttpParser.QueryUsersSuccess
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class EnrolmentStoreProxyConnector @Inject()(http: HttpClient,
                                             appConfig: AppConfig)(implicit ec: ExecutionContext) {


  def getUserIds(vatNumber: String)(implicit hc: HeaderCarrier): Future[QueryUsersSuccess] = {
    http.GET[QueryUsersSuccess](
      url = appConfig.queryUsersUrl(vatNumber),
      queryParams = Seq(principalQueryParameter))
  }
}

object EnrolmentStoreProxyConnector {
  val principalQueryParameter: (String, String) = "type" -> "principal"

}
