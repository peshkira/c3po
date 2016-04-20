// Comment to get more information during initialization
//logLevel := Level.Warn

// The Typesafe repository
resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

//resolvers += "Daniel's Repository" at "http://danieldietrich.net/repository/snapshots/"


//resolvers += "Play2war plugins release" at "http://repository-play-war.forge.cloudbees.com/release/"

resolvers += Resolver.url(
  "bintray-sbt-plugin-releases",
   url("http://dl.bintray.com/content/sbt/sbt-plugin-releases"))(
       Resolver.ivyStylePatterns)

// Use the Play sbt plugin for Play projects
//addSbtPlugin("play" % "sbt-plugin" % "2.1.0")
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.3.0")