package controllers

import javax.inject._
import models.{DatabaseExecutionContext, Topic, TopicRepository}
import play.api.Logger
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.mvc._
import services.AuthenticationActionBuilder

import scala.concurrent.Future


@Singleton
class TopicController @Inject()(topicRepository: TopicRepository, auth : AuthenticationActionBuilder, cc: ControllerComponents, implicit val databaseExecutionContext: DatabaseExecutionContext) extends AbstractController(cc) {
  private val logger: Logger = Logger(this.getClass)

  def topics : Action[AnyContent] = auth.async{ implicit request =>
     topicRepository.topics.map{topics =>
        implicit val topicFormat = Json.format[Topic]
        Ok(Json.toJson(topics))
    }
  }

  def create : Action[JsValue] = auth(parse.json).async{ implicit request =>
    val email = request.session("connected")
    request.body match {
       case JsObject(underlying) =>
           underlying.get("title") match {
             case None =>
               Future.successful(BadRequest("wrong format"))
             case Some(title) =>
               topicRepository.create(title.toString(), email).map(notCreated => if (notCreated) ServiceUnavailable else Created)
           }
       case _ => Future.successful(BadRequest("wrong format"))
    }
  }
}

