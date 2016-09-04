package biz.interop

import play.api.libs.json.{JsBoolean, JsObject, JsString, Json}
import play.modules.reactivemongo.json.collection.JSONCollection
import reactivemongo.api.DB
import scala.concurrent.{ExecutionContext, Future}

trait CanConnectDB {
  def ctx(db: DB): JSONCollection

  val defaultIdentityField = "_id"
  def identityQuery(value: String, identityField: String = defaultIdentityField): JsObject =
    Json.obj(identityField -> JsString(value))
  def fieldsProjection(fields: String*): JsObject = {
    val project = fields.filter(! _.trim.isEmpty).map(_ -> JsBoolean(true))
    JsObject(project)
  }

  val allQuery = Json.obj()

  def count
  (db: DB, value: String, identityField: String = defaultIdentityField)
  (implicit ec: ExecutionContext): Future[Int] = {
    ctx(db).count(Some(identityQuery(value, identityField)))
  }

  def any
  (db: DB, value: String, identityField: String = defaultIdentityField)
  (implicit ec: ExecutionContext): Future[Boolean] = {
    count(db, value, identityField).map(_ > 0)
  }
}
