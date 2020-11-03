package models

import java.util.Date

import anorm.SqlParser.{date, int}
import anorm._
import javax.inject.Inject
import play.api.db.DBApi
import play.api.libs.json.{JsValue, Json}

import scala.concurrent.Future
import scala.util.{Failure, Success}

object EventStatus extends Enumeration {
  val NEW = Value("N")
  val FINISHED = Value("F")
}

object Event {
  implicit val format = Json.format[Event]
  def toJson(events : Seq[Event]): JsValue = Json.toJson(events)
}

case class Event(id : Int, startTime : Date, endTime : Date)

@javax.inject.Singleton
class EventRepository @Inject()(dbapi: DBApi)(implicit ec: DatabaseExecutionContext) {

  private val db = dbapi.database("default")

  private[models] val simple = {
      int("Event.id") ~
      date("Event.start_time") ~
      date("Event.end_time") map {
      case id ~ startTime ~ endTime => Event(id, startTime, endTime)
    }
  }

  def create(startTime : Date, endTime : Date): Future[Boolean] = Future(db.withConnection { implicit connection =>
    SQL"INSERT INTO Event (start_time, end_time) values ($startTime, $endTime)".execute()
  })


  def events: Future[Seq[Event]] = Future(db.withConnection { implicit connection =>
    SQL"select * from Event".
      fold(Seq.empty[Event], ColumnAliaser.empty) { (acc, row) => // Anorm streaming
        row.as(simple) match {
          case Failure(parseErr) => {
            println(s"Fails to parse $row: $parseErr")
            acc
          }
          case Success(event) =>
            event +: acc
        }
      }
  }).flatMap {
    case Left(err :: _) => Future.failed(err)
    case Left(_) => Future(Seq.empty)
    case Right(acc) => Future.successful(acc.reverse)
  }
}
