package controllers

import javax.inject._
import play.api.mvc._
import services.Counter


@Singleton
class LoginController @Inject()(cc: ControllerComponents,
                                counter: Counter) extends AbstractController(cc) {



  def login = Action { Ok(counter.nextCount().toString) }

}
