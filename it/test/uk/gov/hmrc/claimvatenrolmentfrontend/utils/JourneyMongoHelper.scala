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

import org.mongodb.scala.bson.collection.immutable.Document
import org.mongodb.scala.model.Filters
import org.mongodb.scala.result.InsertOneResult
import play.api.libs.json.{JsObject, Json}
import play.api.test.Helpers._
import uk.gov.hmrc.claimvatenrolmentfrontend.models.{JourneyConfig, Lock, VatKnownFacts}
import uk.gov.hmrc.claimvatenrolmentfrontend.repositories.JourneyDataRepository._
import uk.gov.hmrc.claimvatenrolmentfrontend.repositories.{JourneyConfigRepository, JourneyDataRepository, UserLockRepository}

import java.time.Instant
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait JourneyMongoHelper extends ComponentSpecHelper {

  override def beforeEach(): Unit = {
    super.beforeEach()
    await(dropConfigRepo)
    await(dropDataRepo)
    await(dropLockData)
  }

  lazy val journeyConfigRepository: JourneyConfigRepository = app.injector.instanceOf[JourneyConfigRepository]

  lazy val journeyDataRepository: JourneyDataRepository = app.injector.instanceOf[JourneyDataRepository]

  lazy val userLockRepository: UserLockRepository = app.injector.instanceOf[UserLockRepository]

  def find(identifier: String): Future[Option[Lock]] = {
    userLockRepository.collection
      .find(Filters.or(Filters.eq("identifier", identifier)))
      .headOption()
  }

  def find(vrn: String, userId: String): Future[Seq[Lock]] = {
    userLockRepository.collection
      .find(Filters.or(Filters.eq("identifier", vrn), Filters.equal("identifier", userId)))
      .toFuture()
  }

  // Journey configuration mongo repository methods
  def dropConfigRepo: Future[Unit] =
    journeyConfigRepository.collection.drop().toFuture().map(_ => ())

  def countConfigRepo: Future[Long] =
    journeyConfigRepository.collection.countDocuments().toFuture()

  def insertJourneyConfig(journeyId: String,
                          continueUrl: String,
                          authInternalId: String): Future[InsertOneResult] =
    journeyConfigRepository.insertJourneyConfig(
      journeyId, JourneyConfig(continueUrl), authInternalId
    )

  // Journey data mongo repository methods
  def dropDataRepo: Future[Unit] =
    journeyDataRepository.collection.drop().toFuture().map(_ => ())

  def countDataRepo: Future[Long] =
    journeyDataRepository.collection.countDocuments().toFuture()

  def retrieveJourneyDataAsJsObject(journeyId: String,
                                    authInternalId: String): Future[Option[JsObject]] = {
    journeyDataRepository.collection.find[JsObject](
      Filters.and(
        Filters.equal(JourneyIdKey, journeyId),
        Filters.equal(AuthInternalIdKey, authInternalId)
      )
    ).headOption()
  }

  def insertJourneyDataAsJsObject(journeyId: String,
                                  authInternalId: String,
                                  journeyData: JsObject): Future[String] =
    journeyDataRepository.collection.insertOne(
      Json.obj(
        JourneyIdKey -> journeyId,
        AuthInternalIdKey -> authInternalId,
        CreationTimestampKey -> Json.obj("$date" -> Instant.now.toEpochMilli)
      ) ++ journeyData
    ).toFuture().map(_ => journeyId)

  def insertVatKnownFactsData(journeyId: String,
                              authInternalId: String,
                              vatKnownFacts: VatKnownFacts): Future[String] =
    journeyDataRepository.collection.insertOne(
      Json.obj(
        JourneyIdKey -> journeyId,
        AuthInternalIdKey -> authInternalId,
        CreationTimestampKey -> Json.obj("$date" -> Instant.now().toEpochMilli)
      ) ++ Json.toJsObject(vatKnownFacts)
    ).toFuture().map(_ => journeyId)

  def insertLockData(vrn: String, userId: String, attempts: Int = 1): Future[String] = {
    def insert: Future[Map[String, Int]] = userLockRepository.updateAttempts(vrn, userId)

    for {
      a <- insert
      b <- if (a.values.forall(_ < attempts)) insert else Future.successful(a)
      _ <- if (b.values.forall(_ < attempts)) insert else Future.successful(b)
    } yield a.keys.head
  }

  // Journey data mongo repository methods
  def dropLockData: Future[Unit] =
    userLockRepository.collection.deleteMany(Document()).toFuture().map(_ => ())

}
