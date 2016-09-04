package controllers

import play.api.mvc.{Action, Controller}

class CORSController
  extends Controller
  with CanCrossOrigin {

  def preFlight(path: String) = Action { request =>
    corsOPTION(path)
  }

}
