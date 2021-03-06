package controllers

import javax.inject.Inject

import biz._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json._
import play.api.mvc.{Action, Controller}

import scala.concurrent.Future

// Reactive Mongo imports
import play.modules.reactivemongo.{MongoController, ReactiveMongoApi, ReactiveMongoComponents}

class StaticRulesController @Inject() (val reactiveMongoApi: ReactiveMongoApi)
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
}
