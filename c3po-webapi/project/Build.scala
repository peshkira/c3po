import sbt._
import Keys._
//import play.Project._

object ApplicationBuild extends Build {

    val appName         = "c3po"
    val appVersion      = "0.4.0"

    val appDependencies = Seq(
      //javaCore,
      //javaJdbc,
      //javaEbean,
      //cache,
      "dom4j" % "dom4j" % "1.6.1",
      "org.apache.commons" % "commons-digester3" % "3.2",
      "org.apache.commons" % "commons-math" % "2.2",
      "org.mongodb" % "mongo-java-driver" % "2.7.2",
      "com.typesafe.play.plugins" %% "play-plugins-mailer" % "2.3.1"
    )

    //val main = play.Project(appName, appVersion, appDependencies).settings(
      // Add your own project settings here      
      
    val main = Project(appName, file(".")).enablePlugins(play.PlayJava).settings(
      version := appVersion,
      libraryDependencies ++= appDependencies
    ).settings(
      scalaVersion := "2.11.1"
    )
}
