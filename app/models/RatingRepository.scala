package models

import java.util.Date

import anorm.SqlParser._
import anorm._
import javax.inject.Inject
import play.api.db.DBApi
import play.api.libs.json.{JsValue, Json}

import scala.concurrent.Future

object Rating {
  implicit val format = Json.format[Rating]
  def toJson(ratings : Seq[Rating]): JsValue = Json.toJson(ratings)
}

case class Rating(id : Int, topicId : Int, createdBy : String, point : Double, comment: String, createdAt: Date, updatedAt: Option[Date])

@javax.inject.Singleton
class RatingRepository @Inject()(api: DBApi)(implicit ec: DatabaseExecutionContext) extends AbstractRepository[Rating](api){

  override protected def rowParser: RowParser[Rating] = {
        int("Rating.id") ~
        int("Rating.topic_id") ~
        str("Rating.created_by") ~
        double("Rating.point") ~
        str("Rating.comment") ~
        date("Rating.created_at") ~
        get[Option[Date]]("Rating.updated_at") map {
      case id ~ topicId ~ createdBy ~ point ~ comment ~ createdAt ~ updatedAt =>
          Rating(id, topicId, createdBy, point, comment, createdAt, updatedAt)
    }
  }

  def create(topicId : Int, createdBy : String, point : Double, comment: String): Future[Boolean] = Future(db.withConnection { implicit connection =>
    SQL"INSERT INTO Rating (topic_id, created_by, point, comment, created_at) values($topicId,$createdBy,$point,$comment,CURRENT_TIMESTAMP())".execute()
  })

  def ratings(topicId : Int): Future[Seq[Rating]] = super.query(s"SELECT * FROM Rating WHERE topic_id='$topicId'")
}
