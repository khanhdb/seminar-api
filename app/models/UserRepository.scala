package models

import anorm.SqlParser.{get, str}
import anorm._
import javax.inject.Inject
import play.api.db.DBApi

import scala.concurrent.Future
import scala.util.{Failure, Success}

case class User(email: String, displayName: String)

@javax.inject.Singleton
class  UserRepository @Inject()(dbapi: DBApi)(implicit ec: DatabaseExecutionContext) {

  private val db = dbapi.database("default")

  /**
   * Parse a User from a ResultSet
   */
  private[models] val simple = {
    get[String]("user.email") ~ str("user.display_name") map {
      case id ~ name => User(id, name)
    }
  }


  /**
   * Construct the Seq[(String,String)] needed to fill a select options set.
   *
   * Uses `SqlQueryResult.fold` from Anorm streaming,
   * to accumulate the rows as an options list.
   */
  def options: Future[Seq[(String,String)]] = Future(db.withConnection { implicit connection =>
    SQL"select * from User".
      fold(Seq.empty[(String, String)], ColumnAliaser.empty) { (acc, row) => // Anorm streaming
        row.as(simple) match {
          case Failure(parseErr) => {
            println(s"Fails to parse $row: $parseErr")
            acc
          }

          case Success(User(email, name)) =>
            (email -> name) +: acc
        }
      }
  }).flatMap {
    case Left(err :: _) => Future.failed(err)
    case Left(_) => Future(Seq.empty)
    case Right(acc) => Future.successful(acc.reverse)
  }
}
