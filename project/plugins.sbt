logLevel := Level.Warn

libraryDependencies <+= (sbtVersion) { sv =>
  "org.scala-sbt" % "scripted-plugin" % sv
}

addSbtPlugin("com.typesafe.sbt" % "sbt-git" % "0.6.4")
