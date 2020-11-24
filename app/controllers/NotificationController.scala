package controllers

import javax.inject._
import models.{DatabaseExecutionContext, Notification, NotificationRepository}
import play.api.Logger
import play.api.libs.json.JsValue
import play.api.mvc._
import services.AuthenticationActionBuilder


@Singleton
class NotificationController @Inject()(repository : NotificationRepository, auth : AuthenticationActionBuilder, cc: ControllerComponents, implicit val databaseExecutionContext: DatabaseExecutionContext) extends AbstractController(cc) with JsonBodyHandler {
  private val logger: Logger = Logger(this.getClass)

  def notifications: Action[AnyContent] = auth.async{ implicit request =>
    val email = request.session("email")
     repository.notifications(email).map{notifications =>
        Ok(Notification.toJson(notifications))
    }
  }

  def seen(id : Int) : Action[AnyContent] = auth.async{ request =>
    val email = request.session("email")
    repository.changeStatus(email, id).map(notUpdated => if (notUpdated) InternalServerError else Ok)
  }

  def create : Action[JsValue] = auth(parse.json).async { implicit request =>
    handleJsObject { mapValues =>
      val subject = mapValues("subject")
      val notifyTo = mapValues("notify_to")
      repository.create(subject.as[String], notifyTo.as[String]).map(notUpdated => if (notUpdated) InternalServerError else Created)
    }
  }
}

