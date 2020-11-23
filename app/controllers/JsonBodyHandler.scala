package controllers

import play.api.libs.json.{JsArray, JsObject, JsValue}
import play.api.mvc.Results.BadRequest
import play.api.mvc.{Request, Result}

import scala.collection.Map
import scala.concurrent.Future

trait JsonBodyHandler {
  def handleJsObject(f: Map[String, JsValue] => Future[Result])(implicit request: Request[JsValue]): Future[Result] = {
    request.body match {
      case JsObject(map) =>
        try {
          f(map)
        } catch {
          case _: NoSuchElementException =>
            Future.successful(BadRequest("wrong format"))
        }
      case _ => Future.successful(BadRequest("wrong format"))
    }
  }

  def handleJsArray(f: IndexedSeq[JsValue] => Future[Result])(implicit request: Request[JsValue]): Future[Result] =
    request.body match {
      case JsArray(seq) =>
        try {
          f(seq)
        } catch {
          case _: NoSuchElementException => Future.successful(BadRequest("wrong format"))
        }
      case _ => Future.successful(BadRequest("wrong format"))
    }
}
