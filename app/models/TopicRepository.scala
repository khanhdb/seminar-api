package models

import java.util.Date

import anorm.SqlParser.{date, get, int, str}
import anorm._
import javax.inject.Inject
import play.api.db.DBApi
import play.api.libs.json.{JsValue, Json}

import scala.concurrent.Future

object TopicStatus extends Enumeration {
  val NEW = Value("N")
  val PASSED = Value("P")
  val REJECTED = Value("R")
  val DELETED = Value("X")
  val ACTIVE = Value("A")
  val FINISHED = Value("F")
}

object Topic {
  implicit val topicFormat = Json.format[Topic]
  def toJson(topics : Seq[Topic]): JsValue = Json.toJson(topics)
}

case class Topic(id: Int, title: String, status : String, author: String, link : Option[String],
                 description : Option[String], time : Date, updatedAt : Option[Date], createdAt : Date){
  def toJson : JsValue = {
    Json.toJson(this)
  }
}

@javax.inject.Singleton
class TopicRepository @Inject()(dbapi: DBApi)(implicit ec: DatabaseExecutionContext) extends AbstractRepository[Topic](dbapi){
  def create(title : String, author : String, link : String, description : String, time : Date): Future[Boolean] = Future(db.withConnection { implicit connection =>
    SQL"INSERT INTO Topic (title, author, link, description, time, status, created_at) values ($title, $author,$link, $description, $time, 'N', CURRENT_TIMESTAMP())".execute()
  })

  def update(title : String, author : String, link : String, description : String, time : Date, id : Int): Future[Boolean] = Future(db.withConnection {implicit connection =>
    SQL"UPDATE Topic SET title=$title, link=$link, description=$description, time=$time WHERE author = $author AND id = $id".execute()
  })

  def changeStatus(id :Int, author :String, status: String): Future[Boolean] = Future(db.withConnection { implicit connection =>
     SQL"UPDATE Topic SET status=$status WHERE id=$id AND author=$author".execute()
  })


  def topics: Future[Seq[Topic]] = super.query(SQL"SELECT * FROM Topic WHERE status <> 'F'  ORDER BY time")

  override protected def rowParser: RowParser[Topic] =
    int("Topic.id") ~
    str("Topic.title") ~
    str("Topic.status") ~
    str("Topic.author") ~
    get[Option[String]]("Topic.link") ~
    get[Option[String]]("Topic.description") ~
    date("Topic.time") ~
    get[Option[Date]]("Topic.updated_at") ~
    date("Topic.created_at")  map {
    case id ~ title ~ status ~ author ~ link ~ description ~ time ~ updatedAt ~ createdAt =>
      Topic(id, title,  status, author, link, description, time, updatedAt, createdAt)
  }
}
