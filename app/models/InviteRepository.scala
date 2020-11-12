package models

import java.util.Date

import anorm.SqlParser.{date, int, str, get}
import anorm._
import javax.inject.Inject
import play.api.db.DBApi
import play.api.libs.json.{JsValue, Json}

import scala.concurrent.Future
import scala.util.{Failure, Success}

object InviteStatus extends Enumeration {
  val NEW = Value("N")
  val FINISHED = Value("F")
}

object Invite {
  implicit val format = Json.format[Invite]
  def toJson(invites : Seq[Invite]): JsValue = Json.toJson(invites)
}

case class Invite(id : Int, topicId : Int, status : String, inviteTo : String, createdAt: Date, updatedAt : Option[Date])

@javax.inject.Singleton
class InviteRepository @Inject()(dbapi: DBApi)(implicit ec: DatabaseExecutionContext) {

  private val db = dbapi.database("default")

  private[models] val simple = {
      int("Invite.id") ~
      int("Invite.topic_id") ~
        str("Invite.status") ~
        str("Invite.invite_to") ~
        date("Invite.created_at") ~
        get[Option[Date]]("Invite.updated_at") map {
      case id ~ topicId ~ status ~ inviteTo ~ createdAt ~ updatedAt => Invite(id, topicId, status, inviteTo, createdAt, updatedAt)
    }
  }

  def create(topicId : Int, inviteTo : String): Future[Boolean] = Future(db.withConnection { implicit connection =>
    SQL"INSERT INTO Invite  (topic_id, status, invite_to, created_at) values ($topicId, 'N', $inviteTo, CURRENT_TIMESTAMP())".execute()
  })


  def invites(inviteTo : String): Future[Seq[Invite]] = Future(db.withConnection { implicit connection =>
    SQL"select * from Event WHERE invite_to=$inviteTo".
      fold(Seq.empty[Invite], ColumnAliaser.empty) { (acc, row) => // Anorm streaming
        row.as(simple) match {
          case Failure(parseErr) => {
            println(s"Fails to parse $row: $parseErr")
            acc
          }
          case Success(invite) =>
            invite +: acc
        }
      }
  }).flatMap {
    case Left(err :: _) => Future.failed(err)
    case Left(_) => Future(Seq.empty)
    case Right(acc) => Future.successful(acc.reverse)
  }
}
