package services

import javax.inject._


case class UserPayload(email : String, name : String)

trait Authenticator {
  def verify(token: String): Option[UserPayload]
}

@Singleton
class GoogleAuthenticator @Inject()(fire: FireBaseAuthentication) extends Authenticator {
  override def verify(token: String): Option[UserPayload] = {
    fire.verify(token)
  }
}