logLevel := Level.Warn

libraryDependencies += {
  "org.scala-sbt" % "scripted-plugin" % sbtVersion.value
}

addSbtPlugin("com.typesafe.sbt" % "sbt-git" % "0.6.4")
