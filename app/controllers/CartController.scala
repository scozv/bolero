package controllers

import javax.inject.Inject

import biz._
import models._
import models.interop.{HTTPResponse, HTTPResponseError}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.{JsError, Json}
import play.api.mvc.Controller

import scala.concurrent.Future

// Reactive Mongo imports
import play.modules.reactivemongo.{MongoController, ReactiveMongoApi, ReactiveMongoComponents}

// BSON-JSON conversions/collection

class CartController  @Inject() (val reactiveMongoApi: ReactiveMongoApi)
  extends Controller
  with MongoController with ReactiveMongoComponents
  with CanAuthenticate
  with CanResponse
  with CanCrossOrigin {

  def cart = UserAction.async { request =>
    val userId = request.userId
    CartBiz.getUserCart(db, userId)
      .map(x => ResponseOk(Json.toJson(x.map(_.asMasked))))
      .map(corsGET)
  }

  def fruitData(id: String) = play.mvc.Results.TODO
  def fruitPicture(id: String) = play.mvc.Results.TODO

  def edit = UserAction.async(parse.json) { request =>
    request.body.validate[Seq[CoreCartItem]]
      .map { payload =>
        val userId = request.userId

        for {
          ok <- CartBiz.setUserCart(db, userId, payload, drop = false)
          cart <- CartBiz.getUserCart(db, userId)
        } yield Ok(Json.toJson(HTTPResponse(Json.toJson(cart.map(_.asMasked)), ok, HTTPResponseError.MONGO_SET_FAILED)))
      }
      .recoverTotal { e => Future.successful(BadRequest(JsError.toJson(e))) }
      .map (corsPOST)
  }

  def checkout = UserAction.async(parse.json) { request =>
    request.body.validate[CoreOrder]
      .map { payload =>
        val rs = OrderBiz.validate(db, payload)
          .flatMap {
            case Left(order) => OrderBiz.createOrder(db, order, request.userId).map(x => ResponseOk(Json.toJson(x.asMasked)))
            case Right(error) => Future.successful(ResponseError(error))
          }
        rs
      }
      .recoverTotal { e => Future.successful(BadRequest(JsError.toJson(e))) }
      .map (corsPOST)
    // .getOrElse(Future.successful(BadRequest("invalid json")))
  }
}
