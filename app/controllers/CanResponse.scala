package controllers

import models.interop.{HTTPResponseError, HTTPResponse}
import play.api.libs.json._
import play.api.mvc._

trait CanResponse {
  self: Controller =>

  private def r0(data: JsValue, ok: Boolean, error: HTTPResponseError): Result =
    Ok(Json.toJson(HTTPResponse(data, ok = ok, error = error)))

  def ResponseOk(data: JsValue): Result =
    r0(data, ok = true, error = HTTPResponseError.OK)

  // val ResponseOk: Result = ResponseOk(Json.obj())

  def ResponseOk(data: Option[JsValue]): Result =
    ResponseOk(data.getOrElse(Json.obj()))

  def ResponseOk(data: Map[String, String]): Result =
    ResponseOk(Json.toJson(data))

  def ResponseOk(data: Seq[String]): Result =
    ResponseOk(Json.toJson(data))

  def ResponseOk(data: String): Result =
    ResponseOk(JsString(data))

  def ResponseError(error: HTTPResponseError, data: JsValue = Json.obj()): Result =
    r0(data, ok = false, error = error)
}