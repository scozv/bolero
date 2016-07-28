package controllers

import models.interop.HTTPResponse
import play.api._
import play.api.http.Status._
import play.api.libs.json
import play.api.mvc._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json._
import scala.concurrent.Future

// Reactive Mongo imports
import reactivemongo.api._

// Reactive Mongo plugin, including the JSON-specialized collection
import play.modules.reactivemongo.MongoController
import play.modules.reactivemongo.json.collection.JSONCollection

trait CanCrossOrigin {
  self: Controller =>

  private val SPECIFIC_ORIGIN =
    base.runtime.conf("bolero.http.crossorigin")
      .split(",").headOption.map(_.trim)

  private val key = "Access-Control-Allow-Origin"

  private val WILDCARD_ALLOW = key -> "*"
  private val SPECIFIC_ALLOW = key -> SPECIFIC_ORIGIN.getOrElse("*")

  private val SPECIFIC_HEADER = base.runtime.HTTP_AUTH2_HEADER_KEY

  private def withAllowOrigin(result: Result) = SPECIFIC_ORIGIN match {
    case None => result.withHeaders(WILDCARD_ALLOW)
    case Some(origin) => result.withHeaders(SPECIFIC_ALLOW)
  }

  def corsGET(result: Result): Result =
    withAllowOrigin(result)

  def corsGET(content: HTTPResponse): Result =
    corsGET(Ok(Json.toJson(content)))

  def corsPOST(result: Result): Result =
    withAllowOrigin(result).withHeaders(
      "Access-Control-Allow-Headers" -> s"Content-Type, $SPECIFIC_HEADER",
      "Access-Control-Allow-Methods" -> "POST, OPTIONS",
      "Access-Control-Max-Age" -> "600")

  private def corsPOST(content: HTTPResponse): Result =
    corsPOST(Ok(Json.toJson(content)))

  def corsOPTION(from: String = "...") =
    corsPOST(HTTPResponse(JsString(from)))
}
