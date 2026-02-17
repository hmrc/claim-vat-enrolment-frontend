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

package uk.gov.hmrc.claimvatenrolmentfrontend.repositories

import play.api.libs.json.{Format, JsObject, Json}
import org.mongodb.scala.model.Indexes.ascending
import org.mongodb.scala.model.{Filters, IndexModel, IndexOptions}
import org.mongodb.scala.result.InsertOneResult
import uk.gov.hmrc.claimvatenrolmentfrontend.config.AppConfig
import uk.gov.hmrc.claimvatenrolmentfrontend.models.JourneyConfig
import uk.gov.hmrc.claimvatenrolmentfrontend.models.JourneyConfig.format
import uk.gov.hmrc.claimvatenrolmentfrontend.repositories.JourneyConfigRepository._
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.{Codecs, PlayMongoRepository}

import java.time.Instant
import java.util.concurrent.TimeUnit
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class JourneyConfigRepository @Inject()(mongoComponent: MongoComponent,
                                        appConfig: AppConfig
                                       )(implicit ec: ExecutionContext) extends PlayMongoRepository[JsObject](
  collectionName = "claim-vat-enrolment-frontend-journey-config",
  mongoComponent = mongoComponent,
  domainFormat = implicitly[Format[JsObject]],
  indexes = Seq(timeToLiveIndex(appConfig.timeToLiveSeconds)),
  extraCodecs = Seq(Codecs.playFormatCodec(format))
) {

  def insertJourneyConfig(journeyId: String, journeyConfig: JourneyConfig, authInternalId: String): Future[InsertOneResult] = {

    val document: JsObject = Json.obj(
      JourneyIdKey -> journeyId,
      AuthInternalIdKey -> authInternalId,
      CreationTimestampKey -> Json.obj( "$date" -> Instant.now.toEpochMilli)
    ) ++ Json.toJsObject(journeyConfig)

    collection.insertOne(document).toFuture()
  }

  def retrieveJourneyConfig(journeyId: String, authInternalId: String): Future[Option[JourneyConfig]] =

    collection.find[JourneyConfig](
      Filters.and(
        Filters.equal(JourneyIdKey, journeyId),
        Filters.equal(AuthInternalIdKey, authInternalId)
      )
    ).headOption()

}

object JourneyConfigRepository {
  val JourneyIdKey = "_id"
  val AuthInternalIdKey = "authInternalId"
  val CreationTimestampKey = "creationTimestamp"

  def timeToLiveIndex(timeToLiveDuration: Long): IndexModel =
    IndexModel(
      keys = ascending(CreationTimestampKey),
      indexOptions = IndexOptions()
        .name("ClaimVatEnrolmentDataExpires")
        .expireAfter(timeToLiveDuration, TimeUnit.SECONDS)
    )
}
