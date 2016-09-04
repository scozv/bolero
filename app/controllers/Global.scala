import models.interop.{HTTPResponse, HTTPResponseError}
import play.api._
import play.api.libs.json.Json
import play.api.mvc.Results._
import play.api.mvc._

object Global extends GlobalSettings {

  override def onHandlerNotFound(request: RequestHeader) = {
    base.fs(Ok(Json.toJson(HTTPResponse(HTTPResponseError.ACTION_NOT_FOUND))))
  }
}