package biz

import play.api.libs.json._
import play.modules.reactivemongo.json.collection.JSONCollection
import reactivemongo.api._

import scala.concurrent.{Future, ExecutionContext}

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

object QueryBuilder {
  val defaultIdentityField = "_id"

  val universal = Json.obj()
  def withId(id: String, identityField: String = defaultIdentityField): JsObject =
    Json.obj(identityField -> JsString(id))
  def fieldsProjection(fields: String*): JsObject = JsObject(fields.map (_ -> JsBoolean(true)))
}

/**
  * 可被扩展用于链接数据库（第2个版本）
  */
trait CanConnectDB2[T] {
  import reactivemongo.api.collections.GenericQueryBuilder
  import reactivemongo.api.commands._
  import reactivemongo.api._
  import play.modules.reactivemongo.json.JSONSerializationPack
  import scala.concurrent.{Future, ExecutionContext}

  val pack = JSONSerializationPack
  type Self <: GenericQueryBuilder[pack.type]

  val collectionName: Symbol

  private def ctx(db: DB) = base.mongo.ctx(db, collectionName)

  /**
    * Gets the list of data T
    */
  def list(db: DB)(implicit swriter: pack.Writer[JsObject], reader: pack.Reader[T], ec: ExecutionContext): Future[Seq[T]] =
    ctx(db).find(QueryBuilder.universal).cursor[T]().collect[Seq]()

  /**
    * Gets one data T or None
    */
  def one(db: DB, id: String)(implicit swriter: pack.Writer[JsObject], reader: pack.Reader[T], ec: ExecutionContext): Future[Option[T]] =
    ctx(db).find(QueryBuilder.withId(id)).one[T]

  /**
    * Gets the value of specific field
    */
  def field(db: DB, id: String, fieldName: String)(implicit swriter: pack.Writer[JsObject], reader: pack.Reader[T], ec: ExecutionContext) =
    ctx(db).find(QueryBuilder.withId(id), QueryBuilder.fieldsProjection(fieldName)).one[JsObject].map { feature =>
      feature.map ( _ \ fieldName)
    }

  /**
    * Gets the list of specific field value
    */
  def sequence(db: DB, selector: JsObject, fieldName: String)(implicit write: pack.Writer[JsObject], reader: pack.Reader[T], ec: ExecutionContext) =
    ctx(db).find(selector, QueryBuilder.fieldsProjection(fieldName)).one[JsValue].map { feature =>
      feature.map ( _ \\ fieldName)
    }

  /**
    * Inserts a new data T
    */
  def insert(db: DB, document: T)(implicit writer: pack.Writer[T], ec: ExecutionContext): Future[WriteResult] =
    ctx(db).insert(document)

  /**
    * Sets the specific fields
    */
  def update(db: DB, selector: JsObject, update: T)(implicit selectorWriter: pack.Writer[JsObject], updateWriter: pack.Writer[T], ec: ExecutionContext): Future[UpdateWriteResult] =
    ctx(db).update(selector, update, upsert = false, multi = true)

  /**
    * Edit a specific data
    */
  def edit(db: DB, id: String, update: T)(implicit selectorWriter: pack.Writer[JsObject], updateWriter: pack.Writer[T], ec: ExecutionContext): Future[UpdateWriteResult] =
    ctx(db).update(QueryBuilder.fieldsProjection(id), update, upsert = false, multi = false)
}