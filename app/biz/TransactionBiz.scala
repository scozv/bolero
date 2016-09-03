package biz

import base._
import models._
import models.interop.HTTPResponseError
import play.api.libs.json.{JsValue, Json}
import play.modules.reactivemongo.json._
import reactivemongo.api.DB

import scala.concurrent.{ExecutionContext, Future}

object TransactionBiz extends CanConnectDB2[Transaction] {
  override val collectionName: Symbol = 'transactions
}
