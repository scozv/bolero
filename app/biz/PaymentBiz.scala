package biz

import java.util
import base.{mongo, beijingTime}
import com.pingplusplus.Pingpp
import com.pingplusplus.model.Charge
import models.{OrderFlow, Order}
import play.api.libs.json.Json
import reactivemongo.api.DB

import play.modules.reactivemongo.json._

import scala.concurrent.{Future, ExecutionContext}

object PaymentBiz {

  private val TITLE_LENGTH = 29
  private val BODY_LENGTH = 121
  private val CONTENT_PREFIX = "BOLERO"


  // def conf(key: String) = utils.string.conf(key)
  Pingpp.apiKey = base.runtime.conf("bolero.inject.pingpp.secret")

  def createPayment(userId: String, wechatOpenId: String, order: models.CoreOrder) = {
    val par = new util.HashMap[String, Object]()
    par.put("order_no", order._id)
    // TODO see issue #33 系统和数据库是否都使用“分”作为单位，使用整数计算？
    par.put("amount", (order.orderAmount * 100).toInt.asInstanceOf[Object])
    par.put("channel", "wx_pub")
    par.put("client_ip", "127.0.0.1")
    par.put("currency", "cny")
    // 商品的标题，该参数最长为 32 个 Unicode 字符，银联全渠道（upacp/upacp_wap）限制在 32 个字节。
    par.put("subject", paymentTitle(order))
    par.put("body", paymentBody(order))

    val app = new util.HashMap[String, Object]()
    app.put("id", base.runtime.conf("shanlin.inject.pingpp.appid"))
    par.put("app", app)

    val extra = new util.HashMap[String, Object]()
    extra.put("open_id", wechatOpenId)
    par.put("extra", extra)

    Charge.create(par)
  }

  private def paymentTitle(order: models.CoreOrder): String = {
    val title = order.orderItems.map(_.goods.title)
      .mkString(CONTENT_PREFIX, "", "")

    title.substring(0, Math.min(title.length, TITLE_LENGTH))
  }

  private def paymentBody(order: models.CoreOrder): String = ???

  def receivePaid(db: DB, orderId: String)(implicit ec: ExecutionContext): Future[Boolean] = {
    for {
      updated <- mongo.ctx(db, base.mongo.collectionName.ORDERS)
        .update(Json.obj("_id" -> orderId), Json.obj(
        "$push" -> Json.obj("orderFlow" -> OrderFlow(base.modelStatus.order.PAID, beijingTime.nowAtSeconds))))
    } yield updated.ok && updated.nModified == 1
  }
}
