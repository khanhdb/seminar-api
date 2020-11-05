package controllers

import java.util.{Date, NoSuchElementException}

import javax.inject._
import models.{DatabaseExecutionContext, Topic, TopicRepository}
import play.api.Logger
import play.api.libs.json.{JsObject, JsValue}
import play.api.mvc._
import services.AuthenticationActionBuilder

import scala.concurrent.Future


@Singleton
class TopicController @Inject()(topicRepository: TopicRepository, auth : AuthenticationActionBuilder, cc: ControllerComponents, implicit val databaseExecutionContext: DatabaseExecutionContext) extends AbstractController(cc) {
  private val logger: Logger = Logger(this.getClass)

  def topics : Action[AnyContent] = auth.async{ implicit request =>
     topicRepository.topics.map{topics =>
        Ok(Topic.toJson(topics))
    }
  }

  def create : Action[JsValue] = auth(parse.json).async{ implicit request =>
    val author = request.session("email")
    request.body match {
       case JsObject(underlying) =>
         try {
           val title = underlying("title").as[String]
           val link = underlying("link").as[String]
           val description = underlying("description").as[String]
           val time = underlying("time").as[Long]
           topicRepository.create(title, author, link, description, new Date(time)).map(notCreated => if (notCreated) InternalServerError else Created)
         } catch {
             case _: Throwable =>
               Future.successful(BadRequest("wrong format"))
           }
       case _ => Future.successful(BadRequest("wrong format"))
     }
  }

  def update : Action[JsValue] = auth(parse.json).async { request =>
    val author = request.session("email")
    request.body match {
      case JsObject(underlying) =>
        try {
          val id = underlying("id").as[Int]
          val title = underlying("title").as[String]
          val link = underlying("link").as[String]
          val description = underlying("description").as[String]
          val time = underlying("time").as[Long]
          topicRepository.update(title, author, link, description, new Date(time), id).map(notUpdated => if (notUpdated) InternalServerError else Ok)
        } catch {
          case _ : NoSuchElementException =>
            Future.successful(BadRequest("wrong format"))
        }
      case _ =>
        Future.successful(BadRequest("wrong format"))
    }
  }
}

