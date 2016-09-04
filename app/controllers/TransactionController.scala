package controllers

import javax.inject.Inject

import biz._
import biz.interop.QueryBuilder
import models._
import models.interop.HTTPResponseError
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.{Json, _}
import play.api.mvc.{Action, Controller}
import play.modules.reactivemongo.json._

import scala.concurrent.Future

// Reactive Mongo imports
import play.modules.reactivemongo.{MongoController, ReactiveMongoApi, ReactiveMongoComponents}

class TransactionController @Inject() (val reactiveMongoApi: ReactiveMongoApi)
  extends Controller
    with MongoController with ReactiveMongoComponents
    with CanResponse
    with CanCrossOrigin {

  def create(id: String) = Action.async(parse.json) { request =>
    request.body.validate[Transaction]
      .map { payload =>
        TransactionBiz.one(db, id).flatMap {
          case None => TransactionBiz.insert(db, payload.withId(id), id).map {
            case None => ResponseError(HTTPResponseError.MONGO_SET_FAILED)
            case Some(tx) => ResponseOk(tx)
          }
          case Some(_) => base.fs(ResponseError(HTTPResponseError.MONGO_ID_DUPLICATED))
        }
      }
      .recoverTotal { e => Future.successful(BadRequest(JsError.toJson(e))) }
      .map (corsPOST)
  }

  def get(id: String) = Action.async { request =>
    TransactionBiz.one(db, id).map {
      case None => ResponseError(HTTPResponseError.MONGO_NOT_FOUND(request))
      case Some(tx) => ResponseOk(tx)
    }.map (corsGET)
  }

  def list(tp: String) = Action.async { request =>
    TransactionBiz.sequence[String](db, Json.obj("type" -> tp), "_id")
      .map(ResponseOk)
      .map(corsGET)
  }

  def sum(id: String) = Action.async { request =>
    TransactionBiz.sequence[Double](db, QueryBuilder.or(QueryBuilder.withId(id), Json.obj("parent_id" -> id)), "amount")
      .map(lst => lst.sum)
      .map(ResponseOk)
      .map(corsGET)
  }
}
