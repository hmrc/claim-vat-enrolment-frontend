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

import play.api.libs.json._
import play.modules.reactivemongo.ReactiveMongoComponent
import reactivemongo.api.commands.UpdateWriteResult
import reactivemongo.api.indexes.{Index, IndexType}
import reactivemongo.bson.BSONDocument
import reactivemongo.play.json._
import uk.gov.hmrc.claimvatenrolmentfrontend.config.AppConfig
import uk.gov.hmrc.claimvatenrolmentfrontend.models.{VatKnownFacts, JourneyData, Postcode, ReturnsInformation}
import uk.gov.hmrc.claimvatenrolmentfrontend.repositories.JourneyDataRepository._
import uk.gov.hmrc.mongo.ReactiveRepository

import java.time.{Instant, LocalDate, Month}
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class JourneyDataRepository @Inject()(reactiveMongoComponent: ReactiveMongoComponent,
                                      appConfig: AppConfig
                                     )(implicit ec: ExecutionContext) extends ReactiveRepository[JourneyData, String](
  collectionName = "claim-vat-enrolment-frontend-data",
  mongo = reactiveMongoComponent.mongoConnector.db,
  domainFormat = JourneyData.MongoFormat,
  idFormat = implicitly[Format[String]]
) {

  def insertJourneyVatNumber(journeyId: String, authInternalId: String, vatNumber: String): Future[String] =
    collection.insert(true).one(
      Json.obj(
        JourneyIdKey -> journeyId,
        AuthInternalIdKey -> authInternalId,
        VatNumberKey -> vatNumber,
        "creationTimestamp" -> Json.obj("$date" -> Instant.now.toEpochMilli)
      )
    ).map(_ => journeyId)

  def getJourneyData(journeyId: String, authInternalId: String): Future[Option[VatKnownFacts]] =
    collection.find(
      Json.obj(
        JourneyIdKey -> journeyId,
        AuthInternalIdKey -> authInternalId
      ),
      Some(Json.obj(
        JourneyIdKey -> 0,
        AuthInternalIdKey -> 0
      ))
    ).one[VatKnownFacts]

  def updateJourneyData(journeyId: String, dataKey: String, data: JsValue, authInternalId: String): Future[UpdateWriteResult] =
    collection.update(true).one(
      Json.obj(
        JourneyIdKey -> journeyId,
        AuthInternalIdKey -> authInternalId
      ),
      Json.obj(
        "$set" -> Json.obj(dataKey -> data)
      ),
      upsert = false,
      multi = false
    ).filter(_.n == 1)

  def removeJourneyDataFields(journeyId: String, authInternalId: String, dataKeySeq: Seq[String]): Future[UpdateWriteResult] =
    collection.update(true).one(
      Json.obj(
        JourneyIdKey -> journeyId,
        AuthInternalIdKey -> authInternalId
      ),
      Json.obj(
        "$unset" ->
          Json.toJsObject(dataKeySeq.map { dataKey => dataKey -> 1 }.toMap)
      ),
      upsert = false,
      multi = false
    ).filter(_.n == 1)

  private val TtlIndexName = "ClaimVatEnrolmentDataExpires"

  private lazy val ttlIndex = Index(
    Seq(("creationTimestamp", IndexType.Ascending)),
    name = Some(TtlIndexName),
    options = BSONDocument("expireAfterSeconds" -> appConfig.timeToLiveSeconds)
  )

  private def setIndex(): Unit = {
    collection.indexesManager.drop(TtlIndexName) onComplete {
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
  val JourneyIdKey: String = "_id"
  val AuthInternalIdKey: String = "authInternalId"
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

}



