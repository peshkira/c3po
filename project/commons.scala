import sbt._
import Keys._

object Commons {
  val appVersion = "0.5"

  val settings: Seq[Def.Setting[_]] = Seq(
    version := appVersion,
    resolvers += Opts.resolver.mavenLocalFile
  )
}