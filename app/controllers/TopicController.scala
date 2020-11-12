package controllers

import java.util.{Date, NoSuchElementException}

import javax.inject._
import models.{DatabaseExecutionContext, InviteRepository, Topic, TopicRepository}
import play.api.Logger
import play.api.libs.json.{JsArray, JsObject, JsValue}
import play.api.mvc._
import services.AuthenticationActionBuilder

import scala.concurrent.duration.Duration.Inf
import scala.concurrent.{Await, Future}


@Singleton
class TopicController @Inject()(inviteRepository: InviteRepository, topicRepository: TopicRepository, auth: AuthenticationActionBuilder, cc: ControllerComponents, implicit val databaseExecutionContext: DatabaseExecutionContext) extends AbstractController(cc) {
  private val logger: Logger = Logger(this.getClass)

  def topics: Action[AnyContent] = auth.async { implicit request =>
    topicRepository.topics.map { topics =>
      Ok(Topic.toJson(topics))
    }
  }

  def topic(id : Int) : Action[AnyContent] = auth.async{implicit request =>
    // TODO implement
    return null
  }

  def create: Action[JsValue] = auth(parse.json).async { implicit request =>
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

  def update: Action[JsValue] = auth(parse.json).async { request =>
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
          case _: NoSuchElementException =>
            Future.successful(BadRequest("wrong format"))
        }
      case _ =>
        Future.successful(BadRequest("wrong format"))
    }
  }

  def createInvite(topicId: Int): Action[JsValue] = auth(parse.json).async { request =>
    request.body match {
      case JsArray(inviteArray) =>
        val createFuture = inviteArray.map {
          case JsObject(map) =>
            val inviteTo = map("invite_to")
            inviteRepository.create(topicId, inviteTo.as[String])
        }

        Future.sequence(createFuture).map{ results =>
          if (results.exists(notCreated => notCreated)) {
            InternalServerError
          } else {
           Created(s"created ${inviteArray.size} invites")
          }
        }

      case _ => Future.successful(BadRequest("wrong format"))
    }
  }
}

