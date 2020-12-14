package controllers

import contant.AppConstant
import models.{DatabaseExecutionContext, User, UserRepository}
import play.api.libs.json.JsValue
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}
import services.{AuthenticationActionBuilder, FirebaseAdmin}

import javax.inject.Inject
import scala.collection.JavaConverters.seqAsJavaListConverter

class UserController  @Inject()(repository : UserRepository, auth : AuthenticationActionBuilder, cc: ControllerComponents, firebaseAdmin: FirebaseAdmin, implicit val databaseExecutionContext: DatabaseExecutionContext) extends AbstractController(cc) with JsonBodyHandler {
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
          // subscribe topic 'notification_all'
          firebaseAdmin.messagingService.subscribeToTopic(List(fcmToken).asJava, AppConstant.NOTIFICATION_TOPIC)
          Ok("token added")
        }
      }
    }
  }

}
