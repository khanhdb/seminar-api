package controllers

import javax.inject._
import models.{DatabaseExecutionContext, NotificationRepository, NotificationStatus, UserRepository}
import play.api.libs.json.{JsObject, JsValue}
import play.api.mvc._
import play.api.{Configuration, Logger}
import services.{Authenticator, PushNotificationService, UserPayload}

import scala.concurrent.duration.Duration.Inf
import scala.concurrent.{Await, Future}

@Singleton
class AuthenticationController @Inject()(config : Configuration, pushNotificationService: PushNotificationService, userRepository: UserRepository, notificationRepository: NotificationRepository, cc: ControllerComponents, authenticator: Authenticator, implicit val databaseExecutionContext: DatabaseExecutionContext) extends AbstractController(cc) {
  private val logger: Logger = Logger(this.getClass)

  def logout: Action[AnyContent] = Action{ implicit request =>
    request.session.get("email") match {
      case None =>
        logger.debug("user has no session")
        Unauthorized("you are not in any session")
      case Some(_) =>
        Ok.withNewSession
    }
  }

  def login: Action[JsValue] = Action(parse.json).async{ implicit request =>
        request.body match {
            case JsObject(underlying) =>
              underlying.get("token") match {
                case None =>
                  Future.successful(BadRequest("invalid payload format"))
                case Some(token) =>
                  authenticator.verify(token.as[String]) match {
                    case None =>
                      Future.successful(Unauthorized("invalid token"))
                    case Some(userPayload) =>
                      loginSuccessfullyResult(userPayload)
                 }
              }
            case _ =>
              Future.successful(BadRequest("invalid payload format"))
          }
  }

  def loginSuccessfullyResult(userPayload: UserPayload) : Future[Result] = {
    userRepository.allUsers.map(_.find(_.email.equals(userPayload.email))).map{
      case None =>
       Await.result(userRepository.create(userPayload.email, userPayload.name).map[Result]{
          case false =>
            logger.debug(s"created new user ${userPayload.email}")
            Ok.withSession("email" -> userPayload.email, "name" -> userPayload.name)
          case true =>
            logger.debug("unable to insert new user")
            Unauthorized
        }, Inf)

      case Some(user) =>
        this.notify(user.email)
        Ok.withSession("email" -> userPayload.email, "name" -> userPayload.name)
    }
  }

  private def notify(email: String): Unit = {
    notificationRepository.notifications(email).foreach{ notifications =>
       notifications.filter(_.statusEnum == NotificationStatus.NEW).foreach{notification =>
         pushNotificationService.notify(Map("notification" -> notification.subject))
       }
    }
  }
}

