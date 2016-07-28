package biz

import play.api.libs.json._
import play.modules.reactivemongo.json.collection.JSONCollection
import reactivemongo.api._

import scala.concurrent.{Future, ExecutionContext}

/**
 * 可被扩展用于链接数据库
 */
trait CanConnectDB {
  def ctx(db: DB): JSONCollection

  val defaultIdentityField = "_id"
  def identityQuery(value: String, identityField: String = defaultIdentityField): JsObject =
    Json.obj(identityField -> JsString(value))
  def fieldsProjection(fields: String*): JsObject = {
    val project = fields.filter(! _.trim.isEmpty).map(_ -> JsBoolean(true))
    JsObject(project)
  }

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

// TODO
//
//trait CanConnectDB2 {
//import reactivemongo.api.collections.GenericQueryBuilder
//import reactivemongo.api.commands._
//import reactivemongo.api._
//import play.modules.reactivemongo.json.JSONSerializationPack
//import scala.concurrent.{Future, ExecutionContext}
//  val pack = JSONSerializationPack
//  type Self <: GenericQueryBuilder[pack.type]
//
//  val collectionName: Symbol
//
//  def ctx(db: DB) = utils.MongoDB.ctx(db, collectionName)
//  def idQuery(id: String): JsObject = Json.obj("_id" -> JsString(id))
//  def fieldsProjection(fields: String*): JsObject = JsObject(fields.map (_ -> JsBoolean(true)))
//  def fieldsProjection(fields: Seq[String]): JsObject = fieldsProjection(fields: _*)
//
//  def one[T](db: DB, id: String)(implicit swriter: pack.Writer[JsObject], reader: pack.Reader[T], ec: ExecutionContext) =
//    ctx(db).find(idQuery(id)).one[T]
//
//  def field[T](db: DB, id: String, fieldName: String)(implicit swriter: pack.Writer[JsObject], reader: pack.Reader[T], ec: ExecutionContext) =
//    ctx(db).find(idQuery(id), fieldsProjection(fieldName)).one[JsObject].map { feature =>
//      feature.map ( _ \ fieldName)
//    }
//
//  def sequence[T](db: DB, selector: JsObject, fieldName: String)(implicit write: pack.Writer[JsObject], reader: pack.Reader[T], ec: ExecutionContext) =
//    ctx(db).find(selector, fieldsProjection(fieldName)).one[JsValue].map { feature =>
//      feature.map ( _ \\ fieldName)
//    }
//
//  def insert[T](db: DB, document: T)(implicit writer: pack.Writer[T], ec: ExecutionContext): Future[WriteResult] =
//    ctx(db).insert(document)
//
//  def update[T](db: DB, selector: JsObject, update: T)(implicit selectorWriter: pack.Writer[JsObject], updateWriter: pack.Writer[T], ec: ExecutionContext): Future[UpdateWriteResult] =
//    ctx(db).update(selector, update, upsert = false, multi = true)
//
//  def edit[T](db: DB, id: String, update: T)(implicit selectorWriter: pack.Writer[JsObject], updateWriter: pack.Writer[T], ec: ExecutionContext): Future[UpdateWriteResult] =
//    ctx(db).update(idQuery(id), update, upsert = false, multi = false)
//}