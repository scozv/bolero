package base

import play.modules.reactivemongo.json.JSONSerializationPack
import play.modules.reactivemongo.json.collection.JSONCollection
import reactivemongo.api.DB

import scala.concurrent.{Future, ExecutionContext}

/**
  * TODO should be moved into /conf/application.conf
  */

object mongo {
  object collectionName {
    val PRODUCTS = 'products
    val GOODS = 'goods
    val IMAGE = 'images
    val USERS = 'users
    val ADDRESS = 'addresses
    val ORDERS = 'orders
    val AUTHS = 'authentications
  }

  object generalFields {
    val _id = "id"
    val hierarchicId = "rootId"
  }

  object userFields {
    val CART = "shoppingCart"

    object IdentityType extends Enumeration {
      type IdentityType = Value
      val WeChatOpenId, WeChatUniqueId, QQ, UserId = Value
    }
  }

  object identitySize {
    // yyMMddHHmmssSSS
    val ORDER_TIME_STRING = 15
    // random 000~999
    val ORDER_SUFFIX = 3
    val ORDER_IDENTITY = ORDER_TIME_STRING + ORDER_SUFFIX

    val AUTH_TOKEN = 64
  }

  /**
    * 获取指定的collection对象
    * @param db MongoDB数据库
    * @param collectionName 文档（collection）对象的名称
    * @return 返回collection实例
    */
  def ctx(db: DB, collectionName: Symbol): JSONCollection = ctx(db, collectionName.name)

  private def ctx(db: DB, collectionName: String): JSONCollection =
    db.collection[JSONCollection](collectionName)

  def LastOk = reactivemongo.core.commands.LastError(true, None, None, None, None, 0, false)
  def LastError(error: String) = reactivemongo.core.commands.LastError(false, None, None, Some(error), None, 0, false)

  def bulkInsert[T]
  (ctx: JSONCollection, lst: Seq[T])
  (implicit writer: JSONSerializationPack.Writer[T], ec: ExecutionContext): Future[Int] = {
    lst.foldLeft(Future.successful(0)) { (acc: Future[Int], x: T) =>
      for {
        n <- acc
        error <- ctx.insert(x)
        i = if (error.ok) 1 else 0
      } yield n + 1
    }
  }
}
