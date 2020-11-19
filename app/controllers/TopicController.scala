package controllers

import java.util.{Date, NoSuchElementException}

import javax.inject._
import models._
import play.api.Logger
import play.api.libs.json.{JsArray, JsObject, JsValue}
import play.api.mvc._
import services.AuthenticationActionBuilder

import scala.concurrent.Future


@Singleton
class TopicController @Inject()(ratingRepository: RatingRepository, inviteRepository: InviteRepository, topicRepository: TopicRepository, auth: AuthenticationActionBuilder, cc: ControllerComponents, implicit val databaseExecutionContext: DatabaseExecutionContext) extends AbstractController(cc) {
  private val logger: Logger = Logger(this.getClass)

  def topics: Action[AnyContent] = auth.async { implicit request =>
    topicRepository.topics.map { topics =>
      Ok(Topic.toJson(topics))
    }
  }

  def ratings(id : Int): Action[AnyContent] = auth.async { implicit request =>
    ratingRepository.ratings(id).map{ratings =>
      Ok(Rating.toJson(ratings))
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

  def delete(id : Int) : Action[AnyContent] = auth.async{ request =>
    val author = request.session("email")
    topicRepository.changeStatus(id, author, TopicStatus.DELETED.toString).map{ notUpdated =>
      if (notUpdated) InternalServerError else Ok
    }
  }

  def createInvite(topicId: Int): Action[JsValue] = auth(parse.json).async { request =>
    request.body match {
      case JsArray(inviteArray) =>
        val createFuture = inviteArray.map {
          case JsObject(map) =>
            val inviteTo = map("inviteTo")
            inviteRepository.create(topicId, inviteTo.as[String])
          case _ => Future.successful(true)
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

  def createRating(topicId : Int) : Action[JsValue] = auth(parse.json).async { request =>
    request.body match {
      case JsObject(map) =>
        val createdBy = request.session("email")
        val point = map("point").as[Double]
        val comment = map("comment").as[String]
        //todo 1 user has only 1 comment per topic
        ratingRepository.create(topicId, createdBy, point, comment).map { notSuccess =>
          if (notSuccess) InternalServerError else Created
        }

      case _ => Future.successful(BadRequest("wrong format"))
    }
  }

  def updateRating(topicId: Int) : Action[JsValue] = auth(parse.json).async{ request =>
    request.body match {
      case JsObject(map) =>
        val createdBy = request.session("email")
        val id = map("id").as[Int]
        val point = map("point").as[Double]
        val comment = map("comment").as[String]
        ratingRepository.update(id, topicId, point, comment, createdBy).map { notSuccess =>
          if (notSuccess) BadRequest("query failed") else Ok
        }

      case _ => Future.successful(BadRequest("wrong format"))
    }
  }
}

