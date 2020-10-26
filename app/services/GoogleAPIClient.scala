package services;

import java.util.Collections

import com.google.api.client.googleapis.auth.oauth2.{GoogleIdToken, GoogleIdTokenVerifier}
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import javax.inject.Inject
import play.api.{Configuration, Logger};

@Inject
class GoogleAPIClient @Inject() (config : Configuration){

   private val logger = Logger(this.getClass)

   private val verifier :  GoogleIdTokenVerifier = {
     val CLIENT_ID = config.underlying.getString("CLIENT_ID")
     val transport = new NetHttpTransport()
     val jsonFactory = JacksonFactory.getDefaultInstance
     new GoogleIdTokenVerifier.Builder(transport, jsonFactory)
       .setAudience(Collections.singletonList(CLIENT_ID))
       .build()
   }


  def verify(idTokenString : String) : Option[GoogleIdToken.Payload]= {
    val ACCEPTED_EMAIL_DOMAIN = config.underlying.getString("ACCEPTED_EMAIL_DOMAIN")
    try {
      Option(verifier.verify(idTokenString)) match {
        case None =>
          logger.debug("Invalid ID token.")
          None
        case Some(idToken) =>
          val payload = idToken.getPayload
          logger.debug(s"User ID: ${payload.getSubject}")
          payload.getHostedDomain match {
            case ACCEPTED_EMAIL_DOMAIN =>
              Some(payload)
            case _ =>
               None
          }
      }
    } catch {
      case _ : Exception =>
        None
    }
  }
}

