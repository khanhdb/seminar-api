package services

import com.google.firebase.auth.FirebaseAuth
import contant.AppConstant
import javax.inject.Inject
import play.api.{Configuration, Logger}

@Inject
class FireBaseAuthentication @Inject()(config : Configuration, admin: FirebaseAdmin){
  private val logger = Logger(this.getClass)
  def verify(idTokenString : String) : Option[UserPayload]= {
    val ACCEPTED_EMAIL_DOMAIN = config.underlying.getString(AppConstant.ACCEPTED_EMAIL_DOMAIN)
    try {
      val decodedToken = admin.authService.verifyIdToken(idTokenString)
      val email = decodedToken.getEmail
      Option(email) match {
        case None =>
          logger.debug("Invalid ID token.")
          None
        case Some(email) =>
          if (decodedToken.isEmailVerified){
            val hostedDomain = email.split("@")(1)
            if (hostedDomain.equals(ACCEPTED_EMAIL_DOMAIN)){
              Some(UserPayload(email, decodedToken.getName))
            } else {
              logger.debug("invalid email")
              None
            }
          }else {
            logger.debug("email is not verified")
            None
          }
      }
    } catch {
      case e : Exception =>
        e.printStackTrace()
        logger.debug("invalid token")
        None
    }
  }
}

