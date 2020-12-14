package models

import anorm.SqlParser.{get, str}
import anorm._
import contant.AppConstant
import play.api.db.DBApi
import play.api.libs.json.{JsValue, Json, OFormat}
import services.{FirebaseAdmin, FirebasePushNotification}

import javax.inject.Inject
import scala.collection.JavaConverters.seqAsJavaListConverter
import scala.concurrent.Future

case class User(email: String, name: String, displayName: Option[String] = None)

object User {
  def toJson(users: Seq[User]): JsValue = {
    implicit val format: OFormat[User] = Json.format[User]
    Json.toJson(users)
  }
}

@javax.inject.Singleton
class UserRepository @Inject()(dbapi: DBApi, fcmTokenRepository: FCMtokenRepository, admin: FirebaseAdmin)(implicit ec: DatabaseExecutionContext) extends AbstractRepository[User](dbapi) {


  def create(email: String, name: String): Future[Boolean] = Future(db.withConnection { implicit connection =>
    SQL"INSERT INTO User(email, name) values($email, $name)".execute()
  })

  def addNotificationToken(email: String, token: String): Future[Boolean] = {
    // subscribe topic 'notification_all'
    admin.messagingService.subscribeToTopic(List(token).asJava, AppConstant.NOTIFICATION_TOPIC)
    fcmTokenRepository.create(email, token)
  }

  def allUsers: Future[Seq[User]] = super.query(SQL"SELECT * FROM User")

  override protected def rowParser: RowParser[User] = {
    get[String]("User.email") ~
      str("User.name") map {
      case email ~ name => User(email, name)
    }
  }
}

case class FCMtoken(email: String, token: String)

@javax.inject.Singleton
class FCMtokenRepository @Inject()(dbapi: DBApi)(implicit ec: DatabaseExecutionContext) extends AbstractRepository[FCMtoken](dbapi) {

  def create(email: String, token: String): Future[Boolean] = Future(db.withConnection { implicit connection =>
    SQL"INSERT INTO User_fcm_token(email, token) values($email, $token)".execute()
  })

  def allTokens(email : String): Future[Seq[FCMtoken]] = {
    super.query(SQL"SELECT * FROM User_fcm_token WHERE email=$email")
  }

  override protected def rowParser: RowParser[FCMtoken] = {
    str ("User_fcm_token.email") ~
      str ("User_fcm_token.token") map {
      case email ~ token => FCMtoken (email, token)
    }
  }
}