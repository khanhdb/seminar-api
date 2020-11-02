package models

import java.util.Date

import anorm.SqlParser.{date, int, str, get}
import anorm._
import javax.inject.Inject
import play.api.db.DBApi
import play.api.libs.json.{JsValue, Json}

import scala.concurrent.Future
import scala.util.{Failure, Success}

object NotificationStatus extends Enumeration {
  val NEW = Value("N")
  val SEEN = Value("S")
}

object Notification {
  implicit val notificationFormat = Json.format[Notification]
  def toJson(notifications : Seq[Notification]): JsValue = Json.toJson(notifications)
}

case class Notification(id: Long, subject: String, status: String, belongTo : String, createdAt : Date, updatedAt : Option[Date])

@javax.inject.Singleton
class NotificationRepository @Inject()(dbapi: DBApi)(implicit ec: DatabaseExecutionContext) {

  private val db = dbapi.database("default")

  private[models] val simple = {
      int("Notification.id") ~
      str("Notification.subject") ~
      str("Notification.status") ~
      str("Notification.notify_to") ~
      date("Notification.created_at") ~
      get[Option[Date]]("Notification.updated_at") map {
      case id ~ subject ~ status ~ notifyTo ~ createdAt ~ updatedAt => Notification(id, subject,  status, notifyTo, createdAt, updatedAt)
    }
  }


  def create(subject : String, notifyTo : String): Future[Boolean] = Future(db.withConnection { implicit connection =>
    SQL"INSERT INTO Notification (subject, status, notify_to, created_at) values ($subject, 'N', $notifyTo, CURRENT_TIMESTAMP())".execute()
  })


  def notifications(email : String): Future[Seq[Notification]] = Future(db.withConnection { implicit connection =>
    SQL"select * from Notification where notify_to = $email".
      fold(Seq.empty[Notification], ColumnAliaser.empty) { (acc, row) => // Anorm streaming
        row.as(simple) match {
          case Failure(parseErr) => {
            println(s"Fails to parse $row: $parseErr")
            acc
          }
          case Success(notification) =>
            notification +: acc
        }
      }
  }).flatMap {
    case Left(err :: _) => Future.failed(err)
    case Left(_) => Future(Seq.empty)
    case Right(acc) => Future.successful(acc.reverse)
  }
}
