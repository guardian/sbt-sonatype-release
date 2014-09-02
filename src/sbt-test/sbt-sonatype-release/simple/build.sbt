import com.gu.release.ShipAutoPlugin
import com.gu.release.ShipAutoPlugin.ShipKeys._
import com.typesafe.sbt.S3Plugin.S3._
import com.typesafe.sbt.S3Plugin._
import com.typesafe.sbt.SbtPgp._
import xerial.sbt.Sonatype.SonatypeKeys
import xerial.sbt.Sonatype._

name := "ztmp-scala-automation"

organization := "com.gu"

credentials += Credentials(new File("/Users/jduffell/ws/scala-automation/local.sonatypecredentials.properties"))

//sonatypeSettings

//s3Settings

lazy val root = (project in file(".")).enablePlugins(ShipAutoPlugin)

mappings in upload := Seq((new java.io.File("local.changelog.html"),"test-changelog.html"))

host in upload := "scala-automation.s3.amazonaws.com"

credentials in upload += Credentials(new File("/Users/jduffell/ws/scala-automation/local.s3credentials.properties"))

pgpSecretRing in PgpKeys.publishSigned := file("secring.gpg")

pgpPublicRing in PgpKeys.publishSigned := file("pubring.gpg")

scmInfo := Some(ScmInfo(
  url("https://github.com/guardian/scala-automation"),
  "scm:git:git@github.com:guardian/scala-automation.git"
))

pomExtra := (
  <url>https://github.com/guardian/scala-automation</url>
    <developers>
      <developer>
        <id>johnduffell</id>
        <name>John Duffell</name>
        <url>https://github.com/johnduffell</url>
      </developer>
      <developer>
        <id>istvanpamer</id>
        <name>Istvan Pamer</name>
        <url>https://github.com/istvanpamer</url>
      </developer>
    </developers>
  )

licenses := Seq("Apache V2" -> url("http://www.apache.org/licenses/LICENSE-2.0.html"))
