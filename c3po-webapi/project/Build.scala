import sbt._
import Keys._
import play.Project._
import com.github.play2war.plugin._

object ApplicationBuild extends Build {

    val appName         = "c3po"
    val appVersion      = "0.4.0-SNAPSHOT"

    val appDependencies = Seq(
      javaCore,
      "com.typesafe" %% "play-plugins-mailer" % "2.1-RC2",
      "com.petpet" % "c3po-api" % "0.4.0",
      "com.petpet" % "c3po-core" % "0.4.0"
    )

    val main = play.Project(appName, appVersion, appDependencies).settings(
      // Add your own project settings here    
      resolvers += "Play2war plugins release" at "http://repository-play-war.forge.cloudbees.com/release/",
      resolvers += "C3PO maven repo" at "http://dl.bintray.com/peshkira/c3po-maven/",
      Play2WarKeys.servletVersion := "3.0"  
    ).settings(Play2WarPlugin.play2WarSettings: _*)

}