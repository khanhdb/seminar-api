package controllers

import java.util.Date

import javax.inject._
import models._
import play.api.Logger
import play.api.libs.json.{JsObject, JsValue}
import play.api.mvc._
import services.AuthenticationActionBuilder

import scala.concurrent.Future


@Singleton
class TopicController @Inject()(ratingRepository: RatingRepository, inviteRepository: InviteRepository, topicRepository: TopicRepository, auth: AuthenticationActionBuilder, cc: ControllerComponents, implicit val databaseExecutionContext: DatabaseExecutionContext) extends AbstractController(cc) with JsonBodyHandler {
  private val logger: Logger = Logger(this.getClass)

  def topics: Action[AnyContent] = auth.async { implicit request =>
    topicRepository.topics.map { topics =>
      Ok(Topic.toJson(topics))
    }
  }

  def ratings(id: Int): Action[AnyContent] = auth.async { implicit request =>
    ratingRepository.ratings(id).map { ratings =>
      Ok(Rating.toJson(ratings))
    }
  }

  def topic(id: Int): Action[AnyContent] = auth.async { implicit request =>
    // TODO implement
    return null
  }


  def create: Action[JsValue] = auth(parse.json).async { implicit request =>
    val author = request.session("email")
    handleJsObject { mapValues =>
      val title = mapValues("title").as[String]
      val link = mapValues("link").as[String]
      val description = mapValues("description").as[String]
      val time = mapValues("time").as[Long]
      topicRepository.create(title, author, link, description, new Date(time)).map(notCreated => if (notCreated) NotAcceptable else Created)
    }
  }

  def update: Action[JsValue] = auth(parse.json).async { implicit request =>
    val author = request.session("email")
    handleJsObject { mapValues =>
      val id = mapValues("id").as[Int]
      val title = mapValues("title").as[String]
      val link = mapValues("link").as[String]
      val description = mapValues("description").as[String]
      val time = mapValues("time").as[Long]
      topicRepository.update(title, author, link, description, new Date(time), id).map(notUpdated => if (notUpdated) NotAcceptable else Ok)
    }
  }


  def delete(id: Int): Action[AnyContent] = auth.async { request =>
    val author = request.session("email")
    topicRepository.changeStatus(id, author, TopicStatus.DELETED.toString).map { notUpdated =>
      if (notUpdated) InternalServerError else Ok
    }
  }

  def createInvite(topicId: Int): Action[JsValue] = auth(parse.json).async { implicit request =>
    handleJsArray { inviteSeq =>
      val createFuture = inviteSeq.map {
        case JsObject(map) =>
          val inviteTo = map("inviteTo")
          inviteRepository.create(topicId, inviteTo.as[String])
        case _ => Future.successful(true)
      }

      Future.sequence(createFuture).map { results =>
        if (results.exists(notCreated => notCreated)) {
          NotAcceptable
        } else {
          Created(s"created ${inviteSeq.size} invites")
        }
      }
    }
  }

  def createRating(topicId: Int): Action[JsValue] = auth(parse.json).async { implicit request =>
    handleJsObject{ map =>
      val createdBy = request.session("email")
      val point = map("point").as[Double]
      val comment = map("comment").as[String]
      //todo 1 user has only 1 comment per topic
      ratingRepository.create(topicId, createdBy, point, comment).map { notSuccess =>
        if (notSuccess) NotAcceptable else Created
      }
    }
  }

  def updateRating(topicId: Int): Action[JsValue] = auth(parse.json).async { implicit request =>
    handleJsObject { map =>
      val createdBy = request.session("email")
      val id = map("id").as[Int]
      val point = map("point").as[Double]
      val comment = map("comment").as[String]
      ratingRepository.update(id, topicId, point, comment, createdBy).map { notSuccess =>
        if (notSuccess) BadRequest("query failed") else Ok
      }
    }
  }
}

