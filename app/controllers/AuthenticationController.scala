package controllers

import javax.inject._
import models.{DatabaseExecutionContext, UserRepository}
import play.api.Logger
import play.api.libs.json.{JsObject, JsValue}
import play.api.mvc._
import services.{Authenticator, UserPayload}

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt


@Singleton
class AuthenticationController @Inject()(userRepository: UserRepository, cc: ControllerComponents, authenticator: Authenticator, implicit val databaseExecutionContext: DatabaseExecutionContext) extends AbstractController(cc) {
  private val logger: Logger = Logger(this.getClass)

  def logout: Action[AnyContent] = Action{ implicit request =>
    request.session.get("connected") match {
      case None =>
        logger.debug("user has no session")
        Unauthorized("you are not in any session")
      case Some(_) =>
        Redirect("/").withNewSession
    }
  }

  def login: Action[JsValue] = Action(parse.json) { implicit request =>
        request.body match {
            case JsObject(underlying) =>
              underlying.get("token") match {
                case None =>
                  BadRequest("invalid payload format")
                case Some(token) =>
                  authenticator.verify(token.toString()) match {
                    case None =>
                      Unauthorized("invalid token")
                    case Some(userPayload) =>
                      loginSuccessfullyResult(userPayload)
                 }
              }
            case _ =>
              BadRequest("invalid payload format")
          }
  }

  def loginSuccessfullyResult(userPayload: UserPayload) : Result = {
    Redirect("/").withSession("connected" -> userPayload.email)
  }
}

