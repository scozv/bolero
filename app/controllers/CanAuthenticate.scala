package controllers

import biz.{UserProfileBiz, AuthBiz}
import base.mongo
import models.interop.{HTTPResponseError, HTTPResponse}
import models.User
import play.api.libs.json.Json
import play.api.mvc._
import play.modules.reactivemongo.MongoController
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import mongo.userFields.IdentityType

import scala.concurrent.Future

trait CanAuthenticate {
  self: CanCrossOrigin with MongoController =>

  val TOKEN_QUERY_KEY = base.runtime.HTTP_AUTH2_HEADER_KEY

  case class UserRequest[A](userId: String, authenticatedUser: Option[User], request: Request[A])
    extends WrappedRequest[A](request)

  object UserAction
    extends ActionBuilder[UserRequest]
    with ActionRefiner[Request, UserRequest] {

    override protected def refine[A](request: Request[A]): Future[Either[Result, UserRequest[A]]] = {
      transform(request).map { userRequest =>
        userRequest.authenticatedUser match {
          case None => Left(corsGET(HTTPResponse(HTTPResponseError.AUTH2_USER_NOT_AUTHENTICATED)))
          case _ => Right(userRequest)
        }
      }
    }

    def transform[A](request: Request[A]): Future[UserRequest[A]] = {
      val token =
        request.headers.get(TOKEN_QUERY_KEY)

      AuthBiz.getUserId(db, token.getOrElse(base.STRING_NIL)).flatMap {
        case None => Future.successful(
          UserRequest(base.STRING_NIL, None, request))
        case Some(userId) => UserProfileBiz.getProfile(db, userId, IdentityType.UserId).map(
          UserRequest(userId, _, request))
      }
    }
  }
}