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

package uk.gov.hmrc.claimvatenrolmentfrontend.utils

import org.mongodb.scala.model.Filters
import org.mongodb.scala.result.InsertOneResult
import play.api.libs.json.{JsObject, Json}
import play.api.test.Helpers._
import uk.gov.hmrc.claimvatenrolmentfrontend.models.{JourneyConfig, JourneySubmission, VatKnownFacts}
import uk.gov.hmrc.claimvatenrolmentfrontend.repositories.JourneyDataRepository.{AuthInternalIdKey, CreationTimestampKey, JourneyIdKey}
import uk.gov.hmrc.claimvatenrolmentfrontend.repositories.{JourneyConfigRepository, JourneyDataRepository, JourneySubmissionRepository}
import uk.gov.hmrc.claimvatenrolmentfrontend.repositories.JourneyDataRepository._
import uk.gov.hmrc.claimvatenrolmentfrontend.repositories.JourneySubmissionRepository.{AccountStatusKey, JourneySubmissionIdKey, SubmissionNumberKey, _}

import java.time.Instant
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

trait JourneyMongoHelper extends ComponentSpecHelper {

  override def beforeEach(): Unit = {
    await(dropConfigRepo)
    await(dropDataRepo)
    await(dropSubmissionDataRepo)
    super.beforeEach()
  }

  lazy val journeyConfigRepository: JourneyConfigRepository = app.injector.instanceOf[JourneyConfigRepository]

  lazy val journeyDataRepository: JourneyDataRepository = app.injector.instanceOf[JourneyDataRepository]

  lazy val journeySubmissionRepository: JourneySubmissionRepository = app.injector.instanceOf[JourneySubmissionRepository]

  // Journey configuration mongo repository methods
  def dropConfigRepo: Future[Unit] =
    journeyConfigRepository.collection.drop.toFuture().map(_ => ())

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
    journeyDataRepository.collection.drop.toFuture().map(_ => ())

  def countDataRepo: Future[Long] =
    journeyDataRepository.collection.countDocuments().toFuture()

  def retrieveJourneyDataAsJsObject(journeyId: String,
                                    authInternalId: String): Future[Option[JsObject]] = {
    journeyDataRepository.collection.find[JsObject](
      Filters.and(
        Filters.equal(JourneyIdKey, journeyId),
        Filters.equal(AuthInternalIdKey, authInternalId)
      )
    ).headOption
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

  // Journey submission data mongo repository methods
  def retrieveSubmissionData(journeyId: String, vrn: String): Future[Option[JsObject]] = {
    journeySubmissionRepository.collection.find[JsObject](
      Filters.and(
        Filters.equal(JourneySubmissionIdKey, journeyId),
        Filters.equal(SubmissionVrnKey, vrn)
      )
    ).headOption
  }

  def insertSubmissionData(journeyId: String, vrn: String, submissionNumber: Int, accountStatus: String,
                           submissionData: JourneySubmission): Future[String] = {
    journeySubmissionRepository.collection.insertOne(
      Json.obj(
        JourneySubmissionIdKey -> journeyId,
        SubmissionVrnKey -> vrn,
        SubmissionNumberKey -> submissionNumber,
        AccountStatusKey -> accountStatus,
        LastAttemptAtKey -> Json.obj( "$date" -> Instant.now.toEpochMilli)
      ) ++ Json.toJsObject(submissionData)
    ).toFuture().map(_ => journeyId)
  }

  // Journey data mongo repository methods
  def dropSubmissionDataRepo: Future[Unit] =
    journeySubmissionRepository.collection.drop.toFuture().map(_ => ())

}
