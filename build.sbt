name := "seminarAPI"
 
version := "1.0"

packageDescription := "Seminar API"

lazy val packagerSettings = Seq(
  maintainer := "khanhdb@sandinh.net",
  dockerBaseImage := "lwieske/java-8:server-jre-8u92",
  dockerRepository := Some("r.bennuoc.com"),
  dockerExposedPorts := Seq(9000),
  version in Docker := "latest"
)
lazy val `seminarAPI` = (project in file(".")).
  enablePlugins(PlayScala, DockerPlugin).
  settings(packagerSettings)

javacOptions in Universal ++= Seq(
  // JVM memory tuning
  "-J-Xmx1024m",
  "-J-Xms512m",
   s"-Dpidfile.path=/dev/null"
)

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"
      
resolvers += "Akka Snapshot Repository" at "https://repo.akka.io/snapshots/"

scalaVersion := "2.12.2"

val playDependencies =  Seq( jdbc , ehcache , ws , specs2 % Test , guice )
val otherDependencies = Seq(
  "com.google.api-client" % "google-api-client" % "1.30.10",
  "mysql" % "mysql-connector-java" % "8.0.22",
  "com.typesafe.play" %% "anorm" % "2.5.3",
  "com.google.firebase" % "firebase-admin" %"7.0.1"

)

libraryDependencies ++= playDependencies ++ otherDependencies

      