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

package uk.gov.hmrc.claimvatenrolmentfrontend.repositories

import play.api.libs.json.{Format, JsObject, JsValue, Json}
import play.modules.reactivemongo.ReactiveMongoComponent
import reactivemongo.api.commands.UpdateWriteResult
import reactivemongo.api.indexes.{Index, IndexType}
import reactivemongo.bson.BSONDocument
import reactivemongo.play.json._
import uk.gov.hmrc.claimvatenrolmentfrontend.config.AppConfig
import uk.gov.hmrc.claimvatenrolmentfrontend.models.JourneyDataModel
import uk.gov.hmrc.claimvatenrolmentfrontend.repositories.JourneyDataRepository._
import uk.gov.hmrc.mongo.ReactiveRepository

import java.time.Instant
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class JourneyDataRepository @Inject()(reactiveMongoComponent: ReactiveMongoComponent,
                                      appConfig: AppConfig
                                     )(implicit ec: ExecutionContext) extends ReactiveRepository[JourneyDataModel, String](
  collectionName = "claim-vat-enrolment-frontend-data",
  mongo = reactiveMongoComponent.mongoConnector.db,
  domainFormat = JourneyDataModel.MongoFormat,
  idFormat = implicitly[Format[String]]
) {

  def insertJourneyData(journeyId: String, authInternalId: String, vatNumber: String): Future[String] =
    collection.insert(true).one(
      Json.obj(
        journeyIdKey -> journeyId,
        authInternalIdKey -> authInternalId,
        vatNumberKey -> vatNumber,
        "creationTimestamp" -> Json.obj("$date" -> Instant.now.toEpochMilli)
      )
    ).map(_ => journeyId)

  def getJourneyData(journeyId: String, authInternalId: String): Future[Option[JsObject]] =
    collection.find(
      Json.obj(
        journeyIdKey -> journeyId,
        authInternalIdKey -> authInternalId
      ),
      Some(Json.obj(
        journeyIdKey -> 0
      ))
    ).one[JsObject]

  def updateJourneyData(journeyId: String, dataKey: String, data: JsValue, authInternalId: String): Future[UpdateWriteResult] =
    collection.update(true).one(
      Json.obj(
        journeyIdKey -> journeyId,
        authInternalIdKey -> authInternalId
      ),
      Json.obj(
        "$set" -> Json.obj(dataKey -> data)
      ),
      upsert = false,
      multi = false
    ).filter(_.n == 1)

  private val ttlIndexName = "ClaimVatEnrolmentDataExpires"

  private lazy val ttlIndex = Index(
    Seq(("creationTimestamp", IndexType.Ascending)),
    name = Some(ttlIndexName),
    options = BSONDocument("expireAfterSeconds" -> appConfig.timeToLiveSeconds)
  )

  private def setIndex(): Unit = {
    collection.indexesManager.drop(ttlIndexName) onComplete {
      _ => collection.indexesManager.ensure(ttlIndex)
    }
  }

  setIndex()

  override def drop(implicit ec: ExecutionContext): Future[Boolean] =
    collection.drop(failIfNotFound = false).map { r =>
      setIndex()
      r
    }
}

object JourneyDataRepository {
  val journeyIdKey: String = "_id"
  val authInternalIdKey: String = "authInternalId"
  val vatNumberKey: String = "vatNumber"
}

