package controllers

import javax.inject.Inject
import models.{DatabaseExecutionContext, User, UserRepository}
import play.api.Logger
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}
import services.AuthenticationActionBuilder

class UserController  @Inject()(repository : UserRepository, auth : AuthenticationActionBuilder, cc: ControllerComponents, implicit val databaseExecutionContext: DatabaseExecutionContext) extends AbstractController(cc) {
  private val logger: Logger = Logger(this.getClass)

  def users : Action[AnyContent] = auth.async{ implicit request =>
    val email = request.session("connected")
    repository.allUsers.map{users =>
      Ok(User.toJson(users.filterNot(_.email.equals(email))))
    }
  }

}
