package controllers

import java.util.NoSuchElementException

import javax.inject._
import models.{DatabaseExecutionContext, Notification, NotificationRepository}
import play.api.Logger
import play.api.libs.json.{JsObject, JsValue}
import play.api.mvc._
import services.AuthenticationActionBuilder

import scala.concurrent.Future


@Singleton
class NotificationController @Inject()(repository : NotificationRepository, auth : AuthenticationActionBuilder, cc: ControllerComponents, implicit val databaseExecutionContext: DatabaseExecutionContext) extends AbstractController(cc) {
  private val logger: Logger = Logger(this.getClass)

  def notifications: Action[AnyContent] = auth.async{ implicit request =>
    val email = request.session("connected")
     repository.notifications(email).map{notifications =>
        Ok(Notification.toJson(notifications))
    }
  }

  def create : Action[JsValue] = auth(parse.json).async{ implicit request =>
    request.body match {
       case JsObject(underlying) =>
             try {
               val subject = underlying("subject")
               val notifyTo = underlying("notify_to")
               repository.create(subject.as[String], notifyTo.as[String]).map(notUpdated => if (notUpdated) InternalServerError else Created)
             } catch {
               case _ : NoSuchElementException =>
                 Future.successful(BadRequest("wrong format"))
             }
       case _ => Future.successful(BadRequest("wrong format"))
    }
  }
}

