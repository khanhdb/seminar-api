package controllers

import models.{DatabaseExecutionContext, User, UserRepository}
import play.api.libs.json.JsValue
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}
import services.AuthenticationActionBuilder

import javax.inject.Inject

class UserController  @Inject()(repository : UserRepository, auth : AuthenticationActionBuilder, cc: ControllerComponents, implicit val databaseExecutionContext: DatabaseExecutionContext) extends AbstractController(cc) with JsonBodyHandler {
  def users : Action[AnyContent] = auth.async{ implicit request =>
    val email = request.session("email")
    repository.allUsers.map{users =>
      Ok(User.toJson(users.filterNot(_.email.equals(email))))
    }
  }

  def registerNotification : Action[JsValue] = auth(parse.json).async{ implicit request =>
    val email = request.session("email")
    handleJsObject{ map =>
      val fcmToken = map("fcm_token").as[String]
      repository.addNotificationToken(email, fcmToken).map{ notCreated =>
        if(notCreated){
          Ok("not added")
        } else {
          Ok("token added")
        }
      }
    }
  }

}
