package models

import anorm.{ColumnAliaser, Row, RowParser, SQL, SimpleSql, SqlQuery}
import javax.inject.Inject
import play.api.Logger
import play.api.db.{DBApi, Database}

import scala.concurrent.Future
import scala.util.{Failure, Success}

abstract class AbstractRepository[M] @Inject()(dbapi: DBApi, databaseName: String = "default")(implicit ec : DatabaseExecutionContext) {
  private val logger = Logger(this.getClass)

  protected def rowParser: RowParser[M]

  protected def db: Database = dbapi.database(databaseName)

  def query(sql : SimpleSql[Row]) = Future(dbapi.database(databaseName).withConnection { implicit connection =>
      sql.fold(Seq.empty[M], ColumnAliaser.empty) { (acc, row) =>
        row.as(rowParser) match {
          case Failure(parseErr) => {
            println(s"Fails to parse $row: $parseErr")
            acc
          }
          case Success(model) =>
            model +: acc
        }
      }
  }).flatMap {
    case Left(err :: _) => Future.failed(err)
    case Left(_) => Future(Seq.empty)
    case Right(acc) => Future.successful(acc.reverse)
  }

  def query(queryString: String /*TODO replace by sqlQuery*/): Future[Seq[M]] = Future(dbapi.database(databaseName).withConnection { implicit connection =>
    logger.debug("execute: " + queryString)
    SQL(queryString)
      .fold(Seq.empty[M], ColumnAliaser.empty) { (acc, row) =>
        row.as(rowParser) match {
          case Failure(parseErr) => {
            println(s"Fails to parse $row: $parseErr")
            acc
          }
          case Success(model) =>
            model +: acc
        }
      }
  }).flatMap {
    case Left(err :: _) => Future.failed(err)
    case Left(_) => Future(Seq.empty)
    case Right(acc) => Future.successful(acc.reverse)
  }
}
