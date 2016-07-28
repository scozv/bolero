package biz

import base.{mongo}
import models._
import play.api.libs.json.Json
import reactivemongo.api.DB

import scala.concurrent.{Future, ExecutionContext}

import play.modules.reactivemongo.json._

/**
 * 购物车业务逻辑，注意购物车信息存放在用户模型中
 */
object CartBiz extends CanConnectDB {
  def ctx(db: DB) =
    mongo.ctx(db, base.mongo.collectionName.USERS)

  /**
   * 获取用户购物车信息
 *
   * @return 如果没有购物车，则列表为空
   */
  def getUserCart
    (db: DB, userId: String)
    (implicit ec: ExecutionContext): Future[Seq[CoreCartItem]] = ???

  /**
   * 更新购物车内容
 *
   * @param userId TODO 如果获取缓存中的当前登录信息，保证只能登录之后，查看各自的购物车
   * @param drop 默认drop掉之前的购物车信息
   */
  def setUserCart
    (db: DB, userId: String, cart: Seq[CoreCartItem], drop: Boolean = true)
    (implicit ec: ExecutionContext): Future[Boolean] = ???

  def setUserCart
    (db: DB, userId: String, originItem: CoreCartItem, offsetAmount: Int)
    (implicit ec: ExecutionContext): Future[Boolean] = ???
}
