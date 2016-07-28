package controllers

import javax.inject.Inject

import base.modelStatus
import biz._
import models._
import modelStatus._
import models.interop.{HTTPResponse, HTTPResponseError}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json._
import play.api.mvc.{Action, Controller}

import scala.concurrent.{ExecutionContext, Future}

// Reactive Mongo imports
import play.modules.reactivemongo.{MongoController, ReactiveMongoApi, ReactiveMongoComponents}

class OrderController @Inject() (val reactiveMongoApi: ReactiveMongoApi)
  extends Controller
  with MongoController with ReactiveMongoComponents
  with CanAuthenticate
  with CanResponse
  with CanCrossOrigin {

  def orders = UserAction.async { request =>
    val userId = request.userId
    val status = request
      .getQueryString("status")
      .flatMap(x => scala.util.Try(x.toInt).toOption)

    OrderBiz.getOrders(db, userId, status)
      .map(x => ResponseOk(Json.toJson(x.map(_.asMasked))))
      .map(corsGET)
  }

  def getOrder(id: String) = UserAction.async { request =>
    val userId = request.userId

    OrderBiz.getOrder(db, userId, id).map {
      case Some(order) => ResponseOk(Json.toJson(order.asMasked))
      case _ => ResponseError(HTTPResponseError.MONGO_NOT_FOUNT(request))
    }.map(corsGET)
  }

  def getOrderFlow(id: String) = ???

  def getLatestOrderFlow(id: String) = ???

  def finishOrder(id: String) = UserAction.async(parse.json) { request =>
    val userId = request.userId

    ???
  }
}
