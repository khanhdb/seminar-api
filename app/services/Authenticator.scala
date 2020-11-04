package services

import javax.inject._


case class UserPayload(email : String, name : String)

trait Authenticator {
  def verify(token: String): Option[UserPayload]
}

@Singleton
class GoogleAuthenticator @Inject()(googleAPIClient: GoogleAPIClient) extends Authenticator {
  override def verify(token: String): Option[UserPayload] = {
    googleAPIClient.verify(token).map{payload =>
       UserPayload(payload.getEmail, payload.get("name").toString)
    }
  }
}