package controllers

import javax.inject.Inject

import biz.{OrderBiz, PaymentBiz}
import models.interop.HTTPResponseError
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json._
import play.api.mvc._

import scala.concurrent.Future

// Reactive Mongo imports
import play.modules.reactivemongo.{MongoController, ReactiveMongoApi, ReactiveMongoComponents}

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
