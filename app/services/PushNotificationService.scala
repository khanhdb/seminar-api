package services

import com.google.firebase.messaging.Message
import contant.AppConstant
import play.api.Logger

import javax.inject.{Inject, Singleton}
import scala.collection.JavaConverters.mapAsJavaMapConverter

trait PushNotificationService {
  def notify(data : Map[String, String])
  def send(data: Map[String, String], topic: String)
}

@Singleton
class FirebasePushNotification @Inject()(admin : FirebaseAdmin) extends PushNotificationService {
  private val logger: Logger = Logger(this.getClass)

  override def notify(data: Map[String, String]): Unit = {
    send(data, AppConstant.NOTIFICATION_TOPIC)
  }

  override def send(data: Map[String, String], topic: String): Unit = {
    val message = Message.builder.putAllData(data.asJava).setTopic(topic).build
    val response = admin.messagingService.send(message)
    logger.debug(s"message sent successfully $response")
  }
}
