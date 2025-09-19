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

package uk.gov.hmrc.claimvatenrolmentfrontend.repositories

import play.api.libs.json._
import org.mongodb.scala.bson.conversions.Bson
import org.mongodb.scala.model.Indexes.ascending
import org.mongodb.scala.model.{Filters, IndexModel, IndexOptions, Updates, UpdateOptions}
import org.mongodb.scala.model.Updates.{combine, unset}
import uk.gov.hmrc.claimvatenrolmentfrontend.config.AppConfig
import uk.gov.hmrc.claimvatenrolmentfrontend.models.{VatKnownFacts, Postcode, ReturnsInformation}
import uk.gov.hmrc.claimvatenrolmentfrontend.repositories.JourneyDataRepository._
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.{Codecs, PlayMongoRepository}

import java.time.{Instant, LocalDate, Month}
import java.util.concurrent.TimeUnit
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class JourneyDataRepository @Inject()(mongoComponent: MongoComponent,
                                      appConfig: AppConfig
                                     )(implicit ec: ExecutionContext) extends PlayMongoRepository[JsObject](
  collectionName = "claim-vat-enrolment-frontend-data",
  mongoComponent = mongoComponent,
  domainFormat = implicitly[Format[JsObject]],
  indexes = Seq(timeToLiveIndex(appConfig.timeToLiveSeconds))
) {

  def insertJourneyVatNumber(journeyId: String, authInternalId: String, vatNumber: String): Future[String] =
    collection.insertOne(
      Json.obj(
        JourneyIdKey -> journeyId,
        AuthInternalIdKey -> authInternalId,
        VatNumberKey -> vatNumber,
        CreationTimestampKey -> Json.obj( "$date" -> Instant.now.toEpochMilli)
      )
    ).toFuture().map(_ => journeyId)

   def getJourneyData(journeyId: String, authInternalId: String): Future[Option[VatKnownFacts]] = {

     collection.find[JsObject](
       Filters.and(
         Filters.equal(JourneyIdKey, journeyId),
         Filters.equal(AuthInternalIdKey, authInternalId)
       )
     ).headOption().map {
       case Some(doc) => Some(doc.as[VatKnownFacts])
       case _ => None
     }

   }

  def updateJourneyData(journeyId: String, dataKey: String, data: JsValue, authInternalId: String): Future[Boolean] =
    collection.updateOne(
      filterJourneyData(journeyId, authInternalId),
      Updates.set(dataKey, Codecs.toBson(data)),
      UpdateOptions().upsert(false)
    ).toFuture().map{
      _.getMatchedCount == 1
    }

  def removeJourneyDataFields(journeyId: String, authInternalId: String, dataKeySeq: Seq[String]): Future[Boolean] = {

    val unsetDataKeySeq: Seq[Bson] = dataKeySeq.map(key => unset(key))

    collection.updateOne(
      filterJourneyData(journeyId, authInternalId),
      combine(unsetDataKeySeq:_*),
      UpdateOptions().upsert(false)
    ).toFuture().map{
      _.getMatchedCount == 1
    }

  }

  private def filterJourneyData(journeyId: String, authInternalId: String): Bson =
    Filters.and(
      Filters.equal(JourneyIdKey, journeyId),
      Filters.equal(AuthInternalIdKey, authInternalId)
    )

}

object JourneyDataRepository {
  val JourneyIdKey: String = "_id"
  val AuthInternalIdKey: String = "authInternalId"
  val CreationTimestampKey = "creationTimestamp"
  val VatNumberKey: String = "vatNumber"
  val PostcodeKey: String = "vatRegPostcode"
  val VatRegistrationDateKey: String = "vatRegistrationDate"
  val SubmittedVatReturnKey: String = "submittedVatReturn"
  val Box5FigureKey: String = "box5Figure"
  val LastMonthSubmittedKey: String = "lastMonthSubmitted"

  implicit lazy val vatKnownFactsReads: Reads[VatKnownFacts] =
    (json: JsValue) => for {
      vatNumber <- (json \ VatNumberKey).validate[String]
      optPostcode <- (json \ PostcodeKey).validateOpt[String].map {
        optPostcodeString => optPostcodeString.map { stringValue => Postcode(stringValue) } // may be a cleaner way to do this
      }
      vatRegistrationDate <- (json \ VatRegistrationDateKey).validate[LocalDate]
      submittedVatReturn <- (json \ SubmittedVatReturnKey).validate[Boolean]
      optReturnsInformation <- if (submittedVatReturn) {
        for {
          boxFiveFigure <- (json \ Box5FigureKey).validate[String]
          lastReturnMonth <- (json \ LastMonthSubmittedKey).validate[Int].map(Month.of)
        } yield Some(ReturnsInformation(boxFiveFigure, lastReturnMonth))
      } else {
        JsSuccess(None)
      }
    } yield VatKnownFacts(vatNumber, optPostcode, vatRegistrationDate, optReturnsInformation)

  implicit lazy val vatKnownFactsWrites: OWrites[VatKnownFacts] =
    (vatKnownFacts: VatKnownFacts) => Json.obj(
      VatNumberKey -> vatKnownFacts.vatNumber,
      VatRegistrationDateKey -> vatKnownFacts.vatRegistrationDate,
      PostcodeKey -> vatKnownFacts.optPostcode.map(_.stringValue)
    ) ++ {
      if (vatKnownFacts.optReturnsInformation.isDefined) {
        Json.obj(
          SubmittedVatReturnKey -> true,
          Box5FigureKey -> vatKnownFacts.optReturnsInformation.map(_.boxFive),
          LastMonthSubmittedKey -> vatKnownFacts.optReturnsInformation.map(_.lastReturnMonth.getValue)
        )
      } else {
        Json.obj(SubmittedVatReturnKey -> false)
      }
    }

  def timeToLiveIndex(timeToLiveDuration: Long): IndexModel =
    IndexModel(
      keys = ascending(CreationTimestampKey),
      indexOptions = IndexOptions()
        .name("ClaimVatEnrolmentDataExpires")
        .expireAfter(timeToLiveDuration, TimeUnit.SECONDS)
    )

}



