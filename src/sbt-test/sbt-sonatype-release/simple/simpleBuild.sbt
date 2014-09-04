import com.gu.release.ShipAutoPlugin
import com.typesafe.sbt.S3Plugin.S3._
import com.typesafe.sbt.S3Plugin._
import com.typesafe.sbt.SbtPgp._
import xerial.sbt.Sonatype.SonatypeKeys
import xerial.sbt.Sonatype._

name := "ztmp-scala-automation"

organization := "com.gu"

// enable the plugin
lazy val root = (project in file(".")).enablePlugins(ShipAutoPlugin)

// add any additional files to upload to s3
mappings in upload ++= Seq((new java.io.File("local.uploadTest.txt"),"uploadTest.txt"))

// add the amazon hostname for the s3 bucket
host in upload := "scala-automation.s3.amazonaws.com"

// here is your PGP secret key and public key rings
pgpSecretRing := file("local.secring.gpg")

pgpPublicRing := file("local.pubring.gpg")

// the following are required for validation on sonatype
scmInfo := Some(ScmInfo(
  url("https://github.com/guardian/sbt-sonatype-release"),
  "scm:git:git@github.com:guardian/sbt-sonatype-release.git"
))

pomExtra := (
  <url>https://github.com/guardian/scala-automation</url>
    <developers>
      <developer>
        <id>johnduffell</id>
        <name>John Duffell</name>
        <url>https://github.com/johnduffell</url>
      </developer>
    </developers>
  )

licenses := Seq("Apache V2" -> url("http://www.apache.org/licenses/LICENSE-2.0.html"))
