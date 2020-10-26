package models

import anorm.SqlParser.{int, str}
import anorm._
import javax.inject.Inject
import play.api.db.DBApi

import scala.concurrent.Future
import scala.util.{Failure, Success}


case class Topic(id: Int, title: String, status : String, author: String)

@javax.inject.Singleton
class TopicRepository @Inject()(dbapi: DBApi)(implicit ec: DatabaseExecutionContext) {

  private val db = dbapi.database("default")

  private[models] val simple = {
      int("Topic.topic_id") ~
      str("Topic.title") ~
      str("Topic.status") ~
      str("Topic.author") map {
      case id ~ title ~ status ~ author => Topic(id, title, status, author)
    }
  }


  def create(email : String, name : String): Future[Boolean] = Future(db.withConnection { implicit connection =>
    SQL"INSERT INTO Topic values ($email, $name)".execute()
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
