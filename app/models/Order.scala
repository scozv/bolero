package models

import base.beijingTime
import models.interop.{CanBeHierarchicInstance, CanBeHierarchicObject, CanBeJsonfied, CanBeMasked}
import play.api.libs.json._

trait CoreOrder extends CanBeMasked[CoreOrder] {
  val _id: String
  val userId: String
  val orderAmount: Double
  val orderItems: Seq[CoreCartItem]

  def asMasked: CoreOrder
  def isEmpty: Boolean = orderItems.isEmpty
}

object CoreOrder extends CanBeJsonfied[CoreOrder] with CanBeHierarchicObject {
  // will not override the rootFieldName

  import play.api.libs.functional.syntax._

  implicit val writes: OWrites[CoreOrder] = new OWrites[CoreOrder] {
    def writes(x: CoreOrder) = x match {
      case x: Order => coreWriter.writes(x)
      case x: TuanOrder => coreWriter.writes(x) ++ TuanOrder.writes.writes(x)
    }
  }

  val coreWriter = new OWrites[CoreOrder] {
    def writes(x: CoreOrder) = Json.obj (
      "_id" -> x._id,
      "userId" -> x.userId,
      "orderAmount" -> x.orderAmount,
      "orderItems" -> x.orderItems
    )
  }

  implicit val reads: Reads[CoreOrder] = (
    (__ \ "_id").read[String] and
      (__ \ "userId").read[String] and
      (__ \ "orderAmount").read[Double] and
      (__ \ "orderItems").read[Seq[CoreCartItem]] and
      (__ \ rootFieldName).readNullable[String]
    )(CoreOrder.apply _)

  def apply
  (_id: String,
   userId: String,
   orderAmount: Double,
   orderItems: Seq[CoreCartItem],
   tuanRootOrderId: Option[String]): CoreOrder =
    if (tuanRootOrderId.isDefined)
      TuanOrder(
        _id, userId,
        orderAmount,
        orderItems.map(_.asInstanceOf[TuanCartItem]),
        tuanRootOrderId.get)
    else Order(_id, userId, orderAmount, orderItems.map(_.asInstanceOf[CartItem]))
}

case class Order
(_id: String,
 userId: String,
 orderAmount: Double,
 orderItems: Seq[CoreCartItem]) extends CoreOrder {
  val isRoot = false

  def asMasked: Order =
    Order(_id, userId, orderAmount, orderItems.map(_.asMasked))
}

object Order extends CanBeJsonfied[Order] {
  implicit val reads: Reads[Order] = Json.reads[Order]
  val writes: OWrites[Order] = CoreOrder.writes
}


case class TuanOrder
(_id: String,
 userId: String,
 orderAmount: Double,
 orderItems: Seq[TuanCartItem],
 rootId: String) extends CoreOrder with CanBeHierarchicInstance {
  val isRoot = rootId.trim.isEmpty || rootId == _id


  def asMasked: TuanOrder =
    TuanOrder(_id, userId, orderAmount, orderItems.map(_.asMasked), rootId)
}

object TuanOrder extends CanBeJsonfied[TuanOrder] with CanBeHierarchicObject {
  implicit val reads: Reads[TuanOrder] = Json.reads[TuanOrder]
  val writes: OWrites[TuanOrder] = new OWrites[TuanOrder] {
    def writes(x: TuanOrder) = Json.obj(
      rootFieldName -> x.rootId
    )
  }
}




