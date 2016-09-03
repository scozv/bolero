package controllers

import biz._
import models.interop.{HTTPResponse, HTTPResponseError}
import org.joda.time.DateTime
import play.api.mvc._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json._
import scala.concurrent.Future
import play.modules.reactivemongo.MongoController

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

  def goodsType(keyword: String) = Action.async { request =>
    GoodsBiz.getAllTypies(keyword.split(Array(',', '-')))
      .map(ResponseOk)
      .map(corsGET)
  }

  def shippingRules() = Action.async {
    Future.successful {
      Json.obj(
        "freeShippingOn" -> base.ORDER_SHIPPING_FREE_THRESHOLD,
        "shippingFee" -> base.ORDER_PRICE_FOR_SHIPPING
      )
    }.map(ResponseOk).map(corsGET)
  }

  def create(id: String) = play.mvc.Results.TODO

  def get(id: String) = play.mvc.Results.TODO

  def list(tp: String) = play.mvc.Results.TODO

  def sum(id: String) = play.mvc.Results.TODO
}
