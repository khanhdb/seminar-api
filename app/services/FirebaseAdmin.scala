package services

import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import contant.AppConstant
import play.api.Configuration

import javax.inject.Inject

class FirebaseAdmin @Inject()(config : Configuration ){
  private val appName = config.underlying.getString(AppConstant.FIREBASE_APP_NAME)
  private val firebaseApp: FirebaseApp = FirebaseApp.initializeApp(appName)
  lazy val messagingService : FirebaseMessaging = FirebaseMessaging.getInstance(FirebaseApp.getInstance(this.appName))
  lazy val authService : FirebaseAuth= FirebaseAuth.getInstance(firebaseApp)
}
