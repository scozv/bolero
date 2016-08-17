package controllers

import base.modelStatus
import biz.{OrderBiz, PaymentBiz}
import com.pingplusplus.model.Charge
import models.{OrderFlow, CoreOrder}
import models.interop.HTTPResponse
import models.interop.HTTPResponseError
import play.api.libs.json._
import play.api.mvc._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.modules.reactivemongo.MongoController
import collection.JavaConversions._

import scala.concurrent.Future
import javax.inject.Inject

import scala.concurrent.Future

import play.api.Logger
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json._

// Reactive Mongo imports
import reactivemongo.api.Cursor

import play.modules.reactivemongo.{ // ReactiveMongo Play2 plugin
  MongoController,
  ReactiveMongoApi,
  ReactiveMongoComponents
}

class PaymentController @Inject() (val reactiveMongoApi: ReactiveMongoApi)
  extends Controller
  with MongoController with ReactiveMongoComponents
  with CanAuthenticate
  with CanResponse
  with CanCrossOrigin {

  def applyPayment = UserAction.async(parse.json) { request =>
    ???
  }

  def pingxx = Action.async(parse.json) { request =>
    request.body.validate[JsValue]
      .map { json =>
        val webhookType = (json \ "type").asOpt[String]

        if (webhookType.isDefined && webhookType.get == "charge.succeeded") {

          ((json \ "data" \ "object" \ "paid").asOpt[Boolean],
            (json \ "data" \ "object" \ "order_no").asOpt[String]) match {
            case (Some(true), Some(orderId)) => OrderBiz.any(db, orderId).flatMap {
              case true => PaymentBiz.receivePaid(db, orderId).map(_ => ResponseOk(s"$orderId: payment received"))
              case _ => Future.successful(ResponseError(HTTPResponseError.MONGO_NOT_FOUND(request)))
            }
            case _ => Future.successful(BadRequest("no payment"))
          }
        } else Future.successful(BadRequest("not charge object"))
      }
      .recoverTotal { e => Future.successful(BadRequest(JsError.toJson(e))) }
    // .map (corsPOST)
  }
}
