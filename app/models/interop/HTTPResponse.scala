package models.interop

import play.api.libs.json._
import play.api.mvc.{RequestHeader, WrappedRequest}

class HTTPResponse
(val data: JsValue,
 val ok: Boolean,
 val error: HTTPResponseError)

object HTTPResponse extends CanBeJsonfied[HTTPResponse] {
  import play.api.libs.functional.syntax._
  import play.api.libs.json._

  def apply(data: JsValue, ok: Boolean, error: HTTPResponseError): HTTPResponse =
    new HTTPResponse(data, ok, error)

  def apply(data: JsValue): HTTPResponse = new HTTPResponse(data, true, HTTPResponseError.OK)
  def apply(error: HTTPResponseError): HTTPResponse = new HTTPResponse(Json.obj(), false, error)
  def apply(data: String): HTTPResponse = HTTPResponse(JsString(data))

  implicit val writes = new OWrites[HTTPResponse] {
    def writes(x: HTTPResponse) =
      if (x.ok) Json.obj("ok" -> x.ok, "data" -> x.data, "error" -> "")
      else Json.obj("ok" -> x.ok, "error" -> x.error._id, "data" -> x.error.message)
  }

  implicit val reads: Reads[HTTPResponse] = (
    (__ \ "data").read[JsValue] and
    (__ \ "ok").read[Boolean] and
    (__ \ "error").read[String].map(HTTPResponseError.buildForm)
  )((x, y, z) => HTTPResponse.apply(x, y, z))
}

trait HTTPResponseError {
  val _id: String
  val message: String

  override def toString = s"${_id}: $message"
  def test(errorCode: String): Boolean = _id == errorCode
  def test(error: HTTPResponseError): Boolean = test(error._id)
}

object HTTPResponseError {
  def buildForm(errorCode: String): HTTPResponseError =
    allErrors.getOrElse(errorCode, UNDEFINED)

  // 001~699 see below
  case class e(_id: String, message: String)
    extends HTTPResponseError
  // 7?? : 3rd party interop error
  // 701 ~ 729: WeChat Error
  trait WECHAT_ERROR
    extends HTTPResponseError {
    val _id = "7??"
    val message = "WECHAT_ERROR (701~729)"
  }
  case class WECHAT_OPENID_ERROR (override val message: String = "WECHAT_OPENID_ERROR")
    extends WECHAT_ERROR {
    override val _id = "701"
  }
  case class WECHAT_ACCESSTOKEN_ERROR (override val message: String = "WECHAT_ACCESSTOKEN_ERROR")
    extends WECHAT_ERROR {
    override val _id = "703"
  }
  case class WECHAT_USERINFO_ERROR (override val message: String = "WECHAT_USERINFO_ERROR")
    extends WECHAT_ERROR {
    override val _id = "705"
  }

  // 731 ~ 749: Pingxx Error
  case class PINGXX_HOOK_ERROR (message: String = "") extends HTTPResponseError {
    val _id = "731"
  }


  val UNDEFINED = e("000", "error undefined")
  val OK = e("999", "everything's gonna be alright")
  // 0?? : data validation
  val DATA_NOT_MATCHED_ID = e("001", "payload data not matched the specific id")
  val DATA_NOT_MATCHED_HASH = e("003", "data not matched the hash string, like md5")
  // 1?? : Auth2
  val AUTH2_USER_NOT_AUTHENTICATED = e("101", "user cannot be authenticated")
  val AUTH2_USER_TOKEN_INVALID = e("103", "invalid authentication token")
  // 2?? : data manipulated failure
  val MONGO_SET_FAILED = e("203", "failed to set data in MongoDB")
  case class MONGO_NOT_FOUNT(request: Option[RequestHeader] = None) extends HTTPResponseError {
    val _id = "201"
    val message =
      if (request.isDefined) s"not found record in MongoDB from ${request.get.method} ${request.get.path}"
      else "not found record in MongoDB"
  }
  object MONGO_NOT_FOUNT {
    def apply(request: RequestHeader): MONGO_NOT_FOUNT = MONGO_NOT_FOUNT(Some(request))
  }
  // 55? : biz error - order
  val CART_EMPTY = e("551", "empty cart cannot generate the order")
  val ORDER_PRICE_NOT_MATCHED = e("552", "订单金额不匹配")
  val ONLY_VALID_FOR_FRESH_USER = e("553", "一元购活动只对新用户专享")
  val ORDER_PRICE_MUST_BE_FOR_FREE_SHIPPING = e("554", "订单价格不能小于".concat(base.ORDER_SHIPPING_FREE_THRESHOLD.toString))
  val ORDER_PRICE_MUST_GRATER_THAN_ZERO = e("555", "订单价格必须大于零")
  val ORDER_ITEM_REACH_LIMITED = e("556", "特价抢购单品达到了限购数量")

  val ORDER_FLOW_EMPTY = e("559", "订单流程为空")
  // 56?: biz order flow
  val NOT_FINISH_BEFORE_PAID = e("561", "未支付的订单无法确认")
  val NOT_FINISH_AFTER_PAID = e("562", "订单已经确认")
  val CHARGE_ON_PAID_ORDER = e("563", "订单票据已经生成")

  // 57?: tuan order validation
  val TUAN_ORDER_NOT_ONE_ITEM = e("571", "团购订单只能对应一件商品")
  val TUAN_ORDER_NOT_TUAN_GOODS = e("572", "团购订单只能对应团购商品")
  val TUAN_ORDER_FULFILLED = e("573", "本次团购已经满额")

  private val allErrors = List(
    OK, UNDEFINED,
    DATA_NOT_MATCHED_ID, DATA_NOT_MATCHED_HASH,
    AUTH2_USER_NOT_AUTHENTICATED, AUTH2_USER_TOKEN_INVALID,
    MONGO_SET_FAILED, MONGO_NOT_FOUNT(),
    CART_EMPTY, ORDER_PRICE_NOT_MATCHED, ONLY_VALID_FOR_FRESH_USER, ORDER_PRICE_MUST_BE_FOR_FREE_SHIPPING,
    ORDER_PRICE_MUST_GRATER_THAN_ZERO, ORDER_ITEM_REACH_LIMITED,
    NOT_FINISH_BEFORE_PAID, NOT_FINISH_AFTER_PAID,
    CHARGE_ON_PAID_ORDER,
    WECHAT_OPENID_ERROR(), WECHAT_ACCESSTOKEN_ERROR(), WECHAT_USERINFO_ERROR(),
    PINGXX_HOOK_ERROR(),

    TUAN_ORDER_NOT_ONE_ITEM, TUAN_ORDER_NOT_TUAN_GOODS, TUAN_ORDER_FULFILLED
  ).map(x=> x._id -> x).toMap
}