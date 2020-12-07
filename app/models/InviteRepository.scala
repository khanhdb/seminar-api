package models

import java.util.Date

import anorm.SqlParser.{date, get, int, str}
import anorm._
import javax.inject.Inject
import play.api.db.DBApi
import play.api.libs.json.{JsValue, Json}

import scala.concurrent.Future

object InviteStatus extends Enumeration {
  val PENDING = Value("P")
  val ACCEPTED = Value("A")
  val INTERESTED = Value("I")
  val REJECT = Value("X")
}

object Invite {
  implicit val format = Json.format[Invite]
  def toJson(invites : Seq[Invite]): JsValue = Json.toJson(invites)
}

case class Invite(id : Int, topicId : Int, status : String, inviteTo : String, createdAt: Date, updatedAt : Option[Date])

@javax.inject.Singleton
class InviteRepository @Inject()(dbapi: DBApi)(implicit ec: DatabaseExecutionContext) extends AbstractRepository [Invite](dbapi){

  def create(topicId : Int, inviteTo : String): Future[Boolean] = Future(db.withConnection { implicit connection =>
    SQL"INSERT INTO Invite  (topic_id, status, invite_to, created_at) values ($topicId, 'A', $inviteTo, CURRENT_TIMESTAMP())".execute()
  })

  def updateStatus(id : Int, topicId : Int, inviteTo: String, newStatus: String) = Future(db.withConnection { implicit connection =>
    SQL"UPDATE Invite SET status=$newStatus, UPDATED_AT=CURRENT_TIMESTAMP() WHERE id=$id AND topic_id=$topicId AND invite_to=$inviteTo".execute()
  })

  def invites(inviteTo : String): Future[Seq[Invite]] = super.query(s"SELECT * FROM Invite WHERE invite_to='$inviteTo'")

  override protected def rowParser: RowParser[Invite] =
      int("Invite.id") ~
      int("Invite.topic_id") ~
      str("Invite.status") ~
      str("Invite.invite_to") ~
      date("Invite.created_at") ~
      get[Option[Date]]("Invite.updated_at") map {
      case id ~ topicId ~ status ~ inviteTo ~ createdAt ~ updatedAt => Invite(id, topicId, status, inviteTo, createdAt, updatedAt)
    }

}
