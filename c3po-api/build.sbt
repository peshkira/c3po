import AssemblyKeys._

assemblySettings

// Project name (artifact name in Maven)
name := "c3po-api"

// orgnization name (e.g., the package name of the project)
organization := "com.petpet"

version := "0.5.0"

// project description
description := "C3PO module with API"

// Enables publishing to maven repo
publishMavenStyle := true

// Do not append Scala versions to the generated artifacts
crossPaths := false

// This forbids including Scala related libraries into the dependency
autoScalaLibrary := false

// library dependencies. (orginization name) % (project name) % (version)
libraryDependencies ++= Seq(
    "org.slf4j" % "slf4j-api" % "1.7.21",
    "commons-io" % "commons-io" % "2.4",
   "org.apache.commons" % "commons-math3" % "3.1.1",
   "org.fluentd" % "fluent-logger" % "0.2.10",
   "org.mockito" % "mockito-core" % "1.9.5" % "test"  // Test-only dependency
)