import sbt._
import Keys._
//import play.Project._
import com.github.play2war.plugin._

object ApplicationBuild extends Build {

    val appName         = "c3po"
    val appVersion      = "0.3.0"

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

      resolvers += "Play2war plugins release" at "http://repository-play-war.forge.cloudbees.com/release/",
      Play2WarKeys.servletVersion := "3.0",
      scalaVersion := "2.11.1"
    ).settings(Play2WarPlugin.play2WarSettings: _*)//.settings( templatesImport ++= Seq("play.mvc.Http.Context.Implicit._") )

}
