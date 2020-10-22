package controllers

import javax.inject._
import play.api.libs.json.{JsObject, JsValue}
import play.api.mvc._
import services.Authenticator


@Singleton
class LoginController @Inject()(cc: ControllerComponents, authenticator: Authenticator) extends AbstractController(cc) {

  def login: Action[JsValue] = Action(parse.json) { request =>
      request.body match {
        case JsObject(underlying) =>
          underlying.get("token") match {
            case None =>
              BadRequest("invalid payload format")
            case Some(token) =>
             val verified =  authenticator.verify(token.toString())
              if (verified){
                Ok("logged in")
              } else {
                Unauthorized("invalid token")
              }
          }
        case _ =>
          BadRequest("invalid payload format")
      }

    }
}

