package controllers

import javax.inject._
import models.{DatabaseExecutionContext, Topic, TopicRepository}
import play.api.Logger
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.mvc._

import scala.concurrent.duration.Duration.Inf
import scala.concurrent.{Await, Future}


@Singleton
class TopicController @Inject()(topicRepository: TopicRepository, cc: ControllerComponents, implicit val databaseExecutionContext: DatabaseExecutionContext) extends AbstractController(cc) {
  private val logger: Logger = Logger(this.getClass)

  def topics : Action[AnyContent] = Action.async{ implicit request =>
    request.session.get("connected") match {
      case None =>
        Future.successful(Unauthorized)
      case Some(_) =>
        topicRepository.topics.map{topics =>
          implicit val topicFormat = Json.format[Topic]
          Ok(Json.toJson(topics))
        }
    }
  }

  def create : Action[JsValue] = Action(parse.json){ implicit request =>
    request.session.get("connected") match {
      case None =>
        Unauthorized
      case Some(email) =>
        request.body match {
          case JsObject(underlying) =>
            val created =  !Await.result(topicRepository.create(underlying("title").toString(), email), Inf)
            if (created) {
              Created
            } else {
              BadRequest
            }
        }
    }
  }
}

