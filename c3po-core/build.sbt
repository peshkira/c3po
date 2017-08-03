import AssemblyKeys._

assemblySettings

// Project name (artifact name in Maven)
name := "c3po-core"

// orgnization name (e.g., the package name of the project)
organization := "com.petpet"

version := "0.5.0"

// project description
description := "C3PO module with Core"

// Enables publishing to maven repo
publishMavenStyle := true

// Do not append Scala versions to the generated artifacts
crossPaths := false

// This forbids including Scala related libraries into the dependency
autoScalaLibrary := false

// library dependencies. (orginization name) % (project name) % (version)
libraryDependencies ++= Seq(
    "com.opencsv" % "opencsv" % "3.3",
    "com.fasterxml.jackson.core" % "jackson-databind" % "2.5.1",
    "com.google.code.gson" % "gson" % "2.3.1",
    "com.petpet" % "c3po-api" % "0.5.0",
    "org.mongodb" % "mongo-java-driver" % "3.2.0",
    "org.apache.commons" % "commons-compress" % "1.2",
   "org.apache.commons" % "commons-digester3" % "3.2",
    "dom4j" % "dom4j" % "1.6.1",
    "org.slf4j" % "slf4j-api" % "1.7.21",
    "commons-io" % "commons-io" % "2.4",
   "org.mockito" % "mockito-core" % "1.9.5" % "test"  // Test-only dependency
)