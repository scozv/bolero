package biz.interop

import play.api.libs.json.{JsBoolean, JsObject, JsString, Json}

object QueryBuilder {
  val defaultIdentityField = "_id"

  val universal = Json.obj()
  def withId(id: String, identityField: String = defaultIdentityField): JsObject =
    Json.obj(identityField -> JsString(id))
  def fieldsProjection(fields: String*): JsObject = JsObject(fields.map (_ -> JsBoolean(true)))
  def or(selector: JsObject*): JsObject = Json.obj("$or" -> Json.toJson(selector))
}
