package services

import java.util.Collections

import com.google.api.client.googleapis.auth.oauth2.{GoogleIdToken, GoogleIdTokenVerifier}
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import contant.AppConstant
import javax.inject.Inject
import play.api.{Configuration, Logger}

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseToken

@Inject
class FireBaseAuthentication @Inject()(config : Configuration, fire: FirebasePushNotification){

   private val logger = Logger(this.getClass)

   private val verifier :  GoogleIdTokenVerifier = {
     val CLIENT_ID = config.underlying.getString(AppConstant.CLIENT_ID)
     val transport = new NetHttpTransport()
     val jsonFactory = JacksonFactory.getDefaultInstance
     new GoogleIdTokenVerifier.Builder(transport, jsonFactory)
       .setAudience(Collections.singletonList(CLIENT_ID))
       .build()
   }


  def verify(idTokenString : String) : Option[GoogleIdToken.Payload]= {
    val ACCEPTED_EMAIL_DOMAIN = config.underlying.getString(AppConstant.ACCEPTED_EMAIL_DOMAIN)
    try {

      val decodedToken = FirebaseAuth.getInstance(fire.firebaseApp).verifyIdToken(idTokenString)
      val email = decodedToken.getEmail
      Option(email) match {
        case None =>
          logger.debug("Invalid ID token.")
          None
        case Some(email) =>
//          val payload = idToken.getPayload
//          logger.debug(s"User ID: ${payload.getSubject}")
//          payload.getHostedDomain match {
//            case ACCEPTED_EMAIL_DOMAIN =>
//              Some(payload)
//            case _ =>
//               None
           None
      }
    } catch {
      case e : Exception =>
        e.printStackTrace()
        logger.debug("invalid token")
        None
    }
  }
}

