package controllers

import biz._
import models._
import models.interop.{HTTPResponse, HTTPResponseError}
import org.joda.time.DateTime
import play.api.mvc._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json._
import scala.concurrent.Future
import play.modules.reactivemongo.MongoController

import models.interop.HTTPResponseError
import play.api.libs.json.{JsValue, Json}
import play.modules.reactivemongo.json._
import reactivemongo.api.DB

import scala.concurrent.{ExecutionContext, Future}

import javax.inject.Inject

import scala.concurrent.Future

import play.api.Logger
import play.api.mvc.{ Action, Controller }
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json._

// Reactive Mongo imports
import reactivemongo.api.{DB, Cursor}

import play.modules.reactivemongo.{ // ReactiveMongo Play2 plugin
MongoController,
ReactiveMongoApi,
ReactiveMongoComponents
}

class TransactionController @Inject() (val reactiveMongoApi: ReactiveMongoApi)
  extends Controller
    with MongoController with ReactiveMongoComponents
    with CanResponse
    with CanCrossOrigin {

  def create(id: String) = Action.async(parse.json) { request =>
    request.body.validate[Transaction]
      .map { payload =>
        TransactionBiz.one(db, id).flatMap {
          case None => TransactionBiz.insert(db, payload.withId(id), id).map( tx => ResponseOk(Json.toJson(tx)))
          case Some(_) => base.fs(ResponseError(HTTPResponseError.MONGO_ID_DUPLICATED))
        }
      }
      .recoverTotal { e => Future.successful(BadRequest(JsError.toJson(e))) }
      .map (corsPOST)
  }

  def get(id: String) = play.mvc.Results.TODO

  def list(tp: String) = play.mvc.Results.TODO

  def sum(id: String) = play.mvc.Results.TODO
}
