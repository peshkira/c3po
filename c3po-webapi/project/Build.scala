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
      "org.slf4j" % "slf4j-log4j12" % "1.6.1"
    )

    val main = PlayProject(appName, appVersion, appDependencies, mainLang = JAVA).settings(
      // Add your own project settings here      
    )

}
