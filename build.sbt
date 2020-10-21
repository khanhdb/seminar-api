name := "seminarAPI"
 
version := "1.0" 
      
lazy val `seminarAPI` = (project in file(".")).enablePlugins(PlayScala)

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"
      
resolvers += "Akka Snapshot Repository" at "https://repo.akka.io/snapshots/"
      
scalaVersion := "2.12.2"

val playDependencies =  Seq( jdbc , ehcache , ws , specs2 % Test , guice )
val otherDependencies = Seq(
  "com.google.api-client" % "google-api-client" % "1.30.10"
)

libraryDependencies ++= playDependencies ++ otherDependencies

unmanagedResourceDirectories in Test <+=  baseDirectory ( _ /"target/web/public/test" )  

      