import sbt._
import Keys._

object BuildSettings {


  val buildOrganization = "com.petpet"
  val buildVersion = "0.5.0"

  val buildSettings = Defaults.defaultSettings ++ Seq(
    organization := buildOrganization,
    version := buildVersion
    //shellPrompt  := ShellPrompt.buildShellPrompt
  )


}


object C3PO extends Build {




 // lazy val root = Project(
 //   "root",
 //   file(".")) aggregate (c3pocore)

  // lazy val c3powebapi = Project(
  //   "c3po-webapi",
  //   file("c3po-webapi")) dependsOn (c3pocore)

  lazy val c3pocore = Project(
    "c3po-core",
    file("c3po-core"),
    settings =  Seq(libraryDependencies ++= Seq(
      "com.opencsv" % "opencsv" % "3.3",
      "com.fasterxml.jackson.core" % "jackson-databind" % "2.5.1",
      "com.google.code.gson" % "gson" % "2.3.1",
      "org.mongodb" % "mongo-java-driver" % "3.2.0",
      "org.apache.commons" % "commons-compress" % "1.2",
      "org.apache.commons" % "commons-digester3" % "3.2",
      "dom4j" % "dom4j" % "1.6.1",
      "org.slf4j" % "slf4j-api" % "1.7.21",
      "commons-io" % "commons-io" % "2.4",
      "org.mockito" % "mockito-core" % "1.9.5" % "test"  // Test-only dependency
    ))) dependsOn (c3poapi)


  lazy val c3poapi = Project(
    "c3po-api",
    file("c3po-api"),
    settings =  Seq(libraryDependencies ++= Seq(
      "org.slf4j" % "slf4j-api" % "1.7.21",
      "commons-io" % "commons-io" % "2.4")    ))


}