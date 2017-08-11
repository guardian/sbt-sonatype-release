import bintray.Keys._
import com.typesafe.sbt.SbtGit._

name := "sbt-sonatype-release"

organization := "com.gu"

sbtPlugin := true

publishMavenStyle := false

bintrayPublishSettings

credentialsFile in bintray in Global := new File("local.bintraycredentials.properties")

repository in bintray := "sbt-plugins"

licenses += ("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0.html"))

bintrayOrganization in bintray := Some("guardian")

versionWithGit

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "3.0.1" % "test"
)

addSbtPlugin("com.jsuereth" % "sbt-pgp" % "1.0.1")

addSbtPlugin("org.xerial.sbt" % "sbt-sonatype" % "2.0")

//resolvers += "jgit-repo" at "http://download.eclipse.org/jgit/maven"

addSbtPlugin("com.typesafe.sbt" % "sbt-git" % "0.9.3")

ScriptedPlugin.scriptedSettings

scriptedLaunchOpts := { scriptedLaunchOpts.value ++
  Seq("-Xmx1024M", "-XX:MaxPermSize=256M", "-Dplugin.version=" + version.value, "-Dsbt.log.format=false")
}

scriptedBufferLog := false