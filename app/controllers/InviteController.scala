package controllers

import javax.inject._
import models.{DatabaseExecutionContext, Invite, InviteRepository}
import play.api.mvc._
import services.AuthenticationActionBuilder


@Singleton
class InviteController @Inject()(repository : InviteRepository, auth : AuthenticationActionBuilder, cc: ControllerComponents, implicit val databaseExecutionContext: DatabaseExecutionContext) extends AbstractController(cc) {

  def invites: Action[AnyContent] = auth.async{ implicit request =>
    val email = request.session("email")
    repository.invites(email).map{invites =>
      Ok(Invite.toJson(invites))
    }
  }
}

