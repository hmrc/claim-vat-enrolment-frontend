/*
 * Copyright 2025 HM Revenue & Customs
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

import org.mongodb.scala.bson.conversions.Bson
import org.mongodb.scala.model.Indexes.ascending
import org.mongodb.scala.model.Updates.combine
import org.mongodb.scala.model._
import play.api.libs.json._
import uk.gov.hmrc.claimvatenrolmentfrontend.config.AppConfig
import uk.gov.hmrc.claimvatenrolmentfrontend.models.JourneySubmission
import uk.gov.hmrc.claimvatenrolmentfrontend.repositories.JourneySubmissionRepository._
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository

import java.time.Instant
import java.util.concurrent.TimeUnit
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class JourneySubmissionRepository @Inject()(mongoComponent: MongoComponent,
                                      appConfig: AppConfig
                                     )(implicit ec: ExecutionContext) extends PlayMongoRepository[JsObject](
  collectionName = "cve-frontend-invalid-submission-data",
  mongoComponent = mongoComponent,
  domainFormat = implicitly[Format[JsObject]],
  indexes = Seq(IndexModel(
                  keys = ascending(LastAttemptAtKey),
                  indexOptions = IndexOptions()
                    .name("CVEInvalidDataLockExpires")
                    .expireAfter(appConfig.ttlLockSeconds, TimeUnit.SECONDS)),

                IndexModel(
                  keys = ascending(JourneySubmissionIdKey, SubmissionVrnKey),
                  indexOptions = IndexOptions()
                    .name("JourneyIdAndVrnIndex")
                    .unique(true))
              )
) {

  def findSubmissionData(vrn: String): Future[Option[JourneySubmission]] = {
    collection.find[JsObject](
      Filters.equal(SubmissionVrnKey, vrn)
    ).headOption().map {
      case Some(doc) => Some(doc.as[JourneySubmission])
      case _ => None
    }
  }

  def insertSubmissionData(journeyId: String, vrn: String, submissionNumber: Int, accountStatus: String): Future[String] = {
    collection.insertOne(
      Json.obj(
        JourneySubmissionIdKey -> journeyId,
        SubmissionVrnKey -> vrn,
        SubmissionNumberKey -> submissionNumber,
        AccountStatusKey -> accountStatus,
        LastAttemptAtKey -> Json.obj( "$date" -> Instant.now.toEpochMilli)
      )
    ).toFuture().map(_ => journeyId)
  }

  def updateSubmissionData(vrn: String, submissionNumber: Int, accountStatus: String): Future[Boolean] =
    collection.updateOne(
      filterSubmissionData(vrn),
      combine(Updates.set(SubmissionNumberKey, submissionNumber),
          Updates.set(AccountStatusKey, accountStatus),
          Updates.set(LastAttemptAtKey, Json.obj("$date" -> Instant.now.toEpochMilli))),
      UpdateOptions().upsert(false)
    ).toFuture().map(_.getMatchedCount == 1 )

  private def filterSubmissionData(vrn: String): Bson =
    Filters.equal(SubmissionVrnKey, vrn)

 def isVrnLocked(vrn: String): Future[Boolean] = {
   collection.find[JsObject](
     Filters.and(
       Filters.equal(SubmissionVrnKey, vrn),
       Filters.equal(AccountStatusKey, "Locked")
     )
   ).headOption().map(_.isDefined)
 }
}

object JourneySubmissionRepository {
  val JourneySubmissionIdKey: String = "journeyId"
  val SubmissionVrnKey: String = "vrn"
  val SubmissionNumberKey: String = "submissionNumber"
  val AccountStatusKey: String = "accountStatus"
  val LastAttemptAtKey = "lastAttemptAt"
  val UniqueId = "_id"

  implicit lazy val reads: Reads[JourneySubmission] =
    (json: JsValue) => for {
      journeyId <- (json \ JourneySubmissionIdKey).validate[String]
      vrn <- (json \ SubmissionVrnKey).validate[String]
      submissionNumber <- (json \ SubmissionNumberKey).validate[Int]
      accountStatus <- (json \ AccountStatusKey).validate[String]
    } yield JourneySubmission(journeyId, vrn, submissionNumber, accountStatus)

  implicit lazy val writes: OWrites[JourneySubmission] =
    (journeySubmission: JourneySubmission) => Json.obj(
      JourneySubmissionIdKey -> journeySubmission.journeyId,
      SubmissionVrnKey -> journeySubmission.vrn,
      SubmissionNumberKey -> journeySubmission.submissionNumber,
      AccountStatusKey -> journeySubmission.accountStatus)
}



