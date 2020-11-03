package services


import java.io.FileInputStream

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.messaging.{FirebaseMessaging, Message}
import com.google.firebase.{FirebaseApp, FirebaseOptions}
import contant.AppConstant
import javax.inject.{Inject, Singleton}
import play.api.{Configuration, Logger}

import scala.collection.JavaConverters.mapAsJavaMapConverter

trait PushNotificationService {
  def sendToAll(data : Map[String, String])
}

@Singleton
class FirebasePushNotification @Inject() (config : Configuration) extends PushNotificationService {
  private val privateKeys = new FileInputStream("/Users/khanhdb/Downloads/seminar-api-5401e82f6173.json")
  private val logger: Logger = Logger(this.getClass)
  private val options: FirebaseOptions = FirebaseOptions.builder
    .setCredentials(GoogleCredentials.fromStream(privateKeys))
    .build
  private val appName = config.underlying.getString(AppConstant.FIREBASE_APP_NAME)
  FirebaseApp.initializeApp(options, appName)

  private lazy val instance: FirebaseMessaging = FirebaseMessaging.getInstance(FirebaseApp.getInstance(appName))


  override def sendToAll(data: Map[String, String]): Unit = {
    val topic = "notification"
    val message = Message.builder.putAllData(data.asJava).setTopic(topic).build
    val response = this.instance.send(message)
    logger.debug(s"message sent successfully ${response}")
  }
}
