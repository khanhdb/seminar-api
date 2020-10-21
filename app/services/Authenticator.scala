package services

import java.io.IOException
import java.security.GeneralSecurityException

import javax.inject._

trait Authenticator {
  def verify(token: String): Boolean
}

@Singleton
class GoogleAuthenticator extends Authenticator {
  override def verify(token: String): Boolean = {
    try{
      GoogleAPIClient.verify(token)
    }catch {
      case e : GeneralSecurityException =>
        e.printStackTrace()
        false
      case ioe: IOException =>
        ioe.printStackTrace()
        false
    }

  }
}
