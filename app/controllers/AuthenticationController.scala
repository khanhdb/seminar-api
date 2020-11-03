package controllers

import com.google.firebase.FirebaseApp
import javax.inject._
import models.{DatabaseExecutionContext, NotificationRepository, NotificationStatus, UserRepository}
import play.api.{Configuration, Logger}
import play.api.libs.json.{JsObject, JsValue}
import play.api.mvc._
import services.{Authenticator, PushNotificationService, UserPayload}

import scala.concurrent.duration.Duration.Inf
import scala.concurrent.{Await, Future}
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.Message

@Singleton
class AuthenticationController @Inject()(config : Configuration, pushNotificationService: PushNotificationService, userRepository: UserRepository, notificationRepository: NotificationRepository, cc: ControllerComponents, authenticator: Authenticator, implicit val databaseExecutionContext: DatabaseExecutionContext) extends AbstractController(cc) {
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

  def login: Action[JsValue] = Action(parse.json).async{ implicit request =>
        request.body match {
            case JsObject(underlying) =>
              underlying.get("token") match {
                case None =>
                  Future.successful(BadRequest("invalid payload format"))
                case Some(token) =>
                  authenticator.verify(token.toString()) match {
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
            Redirect("/").withSession("connected" -> userPayload.email)
          case true =>
            logger.debug("unable to insert new user")
            Unauthorized
        }, Inf)

      case Some(user) =>
        this.notify(user.email)
        Redirect("/").withSession("connected" -> userPayload.email)
    }
  }

  private def notify(email: String): Unit = {
    notificationRepository.notifications(email).foreach{ notifications =>
       notifications.filter(_.statusEnum == NotificationStatus.NEW).foreach{notification =>
         pushNotificationService.sendToAll(Map("notification" -> notification.subject))
       }
    }
  }
}

