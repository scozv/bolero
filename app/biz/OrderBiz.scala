package biz

import base._
import models.{TuanOrder, Order, OrderFlow, CoreOrder}
import models.interop.HTTPResponseError
import play.api.libs.json.{JsValue, Json}
import play.modules.reactivemongo.json._
import reactivemongo.api.DB

import scala.concurrent.{ExecutionContext, Future}

object OrderBiz extends CanConnectDB {
  def ctx(db: DB) =
    mongo.ctx(db, base.mongo.collectionName.ORDERS)

  def validate
  (db: DB, order: CoreOrder)
  (implicit ec: ExecutionContext): Future[Either[CoreOrder, HTTPResponseError]] =  order match {
    case x : Order => biz.rules.OrderRules.pipeRules(x, db, biz.rules.OrderRules.allRules)
    case x : TuanOrder => biz.rules.TuanRules.pipeRules(x, db, biz.rules.TuanRules.allRules)
  }

  def getOrder(db: DB, userId: String, orderId: String)(implicit ec: ExecutionContext): Future[Option[CoreOrder]] = ???
  def getOrders
  (db: DB, userId: String, status: Option[Int] = None)
  (implicit ec: ExecutionContext): Future[Seq[CoreOrder]] = ???

  def createOrder(db: DB, payload: CoreOrder, userId: String)(implicit ec: ExecutionContext): Future[CoreOrder] = ???

  def nextOrderId
  (db: DB)(implicit ec: ExecutionContext): Future[String] = ???

  def closeOrder(db: DB, userId: String, orderId: String)(implicit ec: ExecutionContext): Future[CoreOrder] = ???
}
