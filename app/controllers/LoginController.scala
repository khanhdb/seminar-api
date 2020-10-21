package controllers

import javax.inject.Inject
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.ExecutionContext


class PostController @Inject()(cc: PostControllerComponents)(implicit ec: ExecutionContext)
  extends PostBaseController(cc) {

  def show(id: String): Action[AnyContent] = PostAction.async { implicit request =>
    postResourceHandler.lookup(id).map { post =>
      Ok(Json.toJson(post))
    }
  }
}

