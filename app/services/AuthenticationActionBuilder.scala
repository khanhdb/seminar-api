package services

import javax.inject.Inject
import play.api.Logger
import play.api.mvc.Results.Unauthorized
import play.api.mvc.{ActionBuilderImpl, BodyParsers, Request, Result}

import scala.concurrent.{ExecutionContext, Future}

class AuthenticationActionBuilder @Inject()(parser: BodyParsers.Default)(implicit ec: ExecutionContext)
    extends ActionBuilderImpl(parser){

  private val logger: Logger = Logger(this.getClass)

    override def invokeBlock[A](request: Request[A], block: Request[A] => Future[Result]): Future[Result] = {
      request.session.get("connected") match {
        case None =>
          Future.successful(Unauthorized)
        case Some(email) =>
          logger.debug(s"$email passed authentication")
          block(request)
      }
    }
}
