name := "sbt-sonatype-release"

organization := "com.gu"

version := "1.0"

sbtPlugin := true

//libraryDependencies ++= Seq(
//  "com.typesafe.sbt" %% "sbt-pgp" % "0.8.2",
//  "org.xerial.sbt" %% "sbt-sonatype" % "0.2.1",
//  "com.typesafe.sbt" %% "sbt-s3" % "0.8",
//  "com.typesafe.sbt" %% "sbt-git" % "0.6.4"
//)

addSbtPlugin("com.typesafe.sbt" % "sbt-pgp" % "0.8.2")

addSbtPlugin("org.xerial.sbt" % "sbt-sonatype" % "0.2.1")

addSbtPlugin("com.typesafe.sbt" % "sbt-s3" % "0.8")

//resolvers += "jgit-repo" at "http://download.eclipse.org/jgit/maven"

addSbtPlugin("com.typesafe.sbt" % "sbt-git" % "0.6.4")
