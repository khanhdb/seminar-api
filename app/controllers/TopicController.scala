package controllers

import javax.inject._
import models.{DatabaseExecutionContext, Topic, TopicRepository}
import play.api.Logger
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.Future


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
          Ok(Json.arr(topics.toList))
        }
    }
  }
}

