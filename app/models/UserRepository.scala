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
      case email ~ name => User(email, name)
    }
  }


  def create(email : String, name : String): Future[Boolean] = Future(db.withConnection { implicit connection =>
    SQL"INSERT INTO User values ($email, $name)".execute()
  })


  def allUsers: Future[Seq[User]] = Future(db.withConnection { implicit connection =>
    SQL"select * from User".
      fold(Seq.empty[User], ColumnAliaser.empty) { (acc, row) => // Anorm streaming
        row.as(simple) match {
          case Failure(parseErr) => {
            println(s"Fails to parse $row: $parseErr")
            acc
          }

          case Success(user) =>
            user +: acc
        }
      }
  }).flatMap {
    case Left(err :: _) => Future.failed(err)
    case Left(_) => Future(Seq.empty)
    case Right(acc) => Future.successful(acc.reverse)
  }
}
