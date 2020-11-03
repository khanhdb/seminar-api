package controllers

import java.util.{Date, NoSuchElementException}

import javax.inject._
import models.{DatabaseExecutionContext, Event, EventRepository}
import play.api.Logger
import play.api.libs.json.{JsObject, JsValue}
import play.api.mvc._
import services.AuthenticationActionBuilder

import scala.concurrent.Future


@Singleton
class EventController @Inject()(repository : EventRepository, auth : AuthenticationActionBuilder, cc: ControllerComponents, implicit val databaseExecutionContext: DatabaseExecutionContext) extends AbstractController(cc) {
  private val logger: Logger = Logger(this.getClass)

  def events: Action[AnyContent] = auth.async{ implicit request =>
    repository.events.map{events =>
      Ok(Event.toJson(events))
    }
  }

  def create : Action[JsValue] = auth(parse.json).async{ implicit request =>
    request.body match {
      case JsObject(underlying) =>
        try {
          val startTime = underlying("start_time")
          val endTime = underlying("end_time")
          repository.create( new Date(startTime.as[Long]), new Date(endTime.as[Long])).map(notUpdated => if (notUpdated) InternalServerError else Created)
        } catch {
          case _ : NoSuchElementException =>
            Future.successful(BadRequest("wrong format"))
        }
      case _ => Future.successful(BadRequest("wrong format"))
    }
  }
}

