package models

import java.util.Date

import anorm.SqlParser.{date, int, str}
import anorm._
import javax.inject.Inject
import play.api.db.DBApi
import play.api.libs.json.{JsValue, Json}

import scala.concurrent.Future
import scala.util.{Failure, Success}

object Status extends Enumeration {
  val NEW = Value("N")
  val ACTIVE = Value("A")
}

object Topic {
  implicit val topicFormat = Json.format[Topic]
  def toJson(topics : Seq[Topic]): JsValue = Json.toJson(topics)
}

case class Topic(id: Int, title: String, status : String, author: String, createdAt : Date){
  def toJson : JsValue = {
    Json.toJson(this)
  }
}

@javax.inject.Singleton
class TopicRepository @Inject()(dbapi: DBApi)(implicit ec: DatabaseExecutionContext) {

  private val db = dbapi.database("default")

  private[models] val simple = {
      int("Topic.id") ~
      str("Topic.title") ~
      str("Topic.status") ~
      str("Topic.author") ~
      date("Topic.created_at")  map {
      case id ~ title ~ status ~ author ~ createdAt => Topic(id, title,  status, author, createdAt)
    }
  }


  def create(title : String, author : String): Future[Boolean] = Future(db.withConnection { implicit connection =>
    SQL"INSERT INTO Topic (title, author, status, created_at) values ($title, $author, 'N', CURRENT_TIMESTAMP())".execute()
  })

  def update(title : String, email: String, id : String): Future[Boolean] = Future(db.withConnection {implicit connection =>
    SQL"UPDATE Topic SET title=$title WHERE author = $email AND id = $id".execute()
  })


  def topics: Future[Seq[Topic]] = Future(db.withConnection { implicit connection =>
    SQL"select * from Topic".
      fold(Seq.empty[Topic], ColumnAliaser.empty) { (acc, row) => // Anorm streaming
        row.as(simple) match {
          case Failure(parseErr) => {
            println(s"Fails to parse $row: $parseErr")
            acc
          }
          case Success(topic) =>
            topic +: acc
        }
      }
  }).flatMap {
    case Left(err :: _) => Future.failed(err)
    case Left(_) => Future(Seq.empty)
    case Right(acc) => Future.successful(acc.reverse)
  }
}
