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

package uk.gov.hmrc.claimvatenrolmentfrontend.httpparsers

import play.api.http.Status._
import uk.gov.hmrc.http.{HttpReads, HttpResponse, InternalServerException}

object QueryUsersHttpParser {

  val principalUserIdKey = "principalUserIds"

  implicit object QueryUsersHttpReads extends HttpReads[QueryUsersSuccess] {
    override def read(method: String, url: String, response: HttpResponse): QueryUsersSuccess =
      response.status match {
        case OK => UsersFound
        case NO_CONTENT => NoUsersFound
        case status => throw new InternalServerException(s"getUserIds returned $status ${response.body}")
      }
  }

  sealed trait QueryUsersSuccess

  case object UsersFound extends QueryUsersSuccess {
    val message = "Enrolment could not be allocated as a user with the specified enrolment already applied to their credential has been found"
  }

  case object NoUsersFound extends QueryUsersSuccess {
    val message = "Enrolment could not be allocated but no other users have been found with the specified enrolment"
  }

}