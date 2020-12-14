package models

import anorm.SqlParser.{get, str}
import anorm._
import play.api.db.DBApi
import play.api.libs.json.{JsValue, Json}

import javax.inject.Inject
import scala.concurrent.Future

case class User(email: String, name: String, displayName : Option[String] = None)

object User{
  def toJson(users: Seq[User]) : JsValue= {
    implicit val format = Json.format[User]
    Json.toJson(users)
  }
}

@javax.inject.Singleton
class  UserRepository @Inject()(dbapi: DBApi)(implicit ec: DatabaseExecutionContext) extends AbstractRepository[User](dbapi) {
  override protected def rowParser: RowParser[User] = {
    get[String]("User.email") ~
    str("User.name") map {
      case email ~ name => User(email, name)
    }
  }

  def create(email : String, name : String): Future[Boolean] = Future(db.withConnection { implicit connection =>
    SQL"INSERT INTO User(email, name) values($email, $name)".execute()
  })

  def addNotificationToken(email: String, token : String) : Future[Boolean] = Future(db.withConnection { implicit connection =>
    SQL"INSERT INTO User_fcm_token(email, token) values($email, $token)".execute()
  })

  def allUsers: Future[Seq[User]] = super.query(SQL"SELECT * FROM User")
}
