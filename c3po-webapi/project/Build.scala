import sbt._
import Keys._
import PlayProject._

object ApplicationBuild extends Build {

    val appName         = "c3po-webapi"
    val appVersion      = "1.0-SNAPSHOT"

    val appDependencies = Seq(
      // Add your project dependencies here,
      "dom4j" % "dom4j" % "1.6.1",
      "org.apache.commons" % "commons-digester3" % "3.2",
      "org.apache.commons" % "commons-math" % "2.2",
      "org.mongodb" % "mongo-java-driver" % "2.7.2",
      "com.github.play2war" %% "play2-war-core" % "0.5",
      "com.typesafe" %% "play-plugins-mailer" % "2.0.2"
    )

    val main = PlayProject(appName, appVersion, appDependencies, mainLang = JAVA).settings(
      // Add your own project settings here      
      resolvers += "Play2war plugins release" at "http://repository-play-war.forge.cloudbees.com/release"
    )

}
