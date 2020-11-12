package controllers

import javax.inject._
import models.{DatabaseExecutionContext, Invite, InviteRepository}
import play.api.libs.json.{JsObject, JsValue}
import play.api.mvc._
import services.AuthenticationActionBuilder

import scala.concurrent.Future


@Singleton
class InviteController @Inject()(repository : InviteRepository, auth : AuthenticationActionBuilder, cc: ControllerComponents, implicit val databaseExecutionContext: DatabaseExecutionContext) extends AbstractController(cc) {

  def invites: Action[AnyContent] = auth.async{ implicit request =>
    val email = request.session("email")
    repository.invites(email).map{invites =>
      Ok(Invite.toJson(invites))
    }
  }

  def update : Action[JsValue] = auth(parse.json).async{ implicit request =>
    val email = request.session("email")
    request.body match {
      case JsObject(map) =>
        try {
          val id = map("id").as[Int]
          val topicId = map("topicId").as[Int]
          val status = map("status").as[String]
          repository.updateStatus(id, topicId, email, status).map{ notUpdated =>
            if (notUpdated) InternalServerError("failed to update") else Ok
          }
        } catch {
          case _ : NoSuchElementException =>
            Future.successful(BadRequest("wrong data format"))
        }
      case _ =>
        Future.successful(BadRequest("wrong data format"))
    }
  }
}

