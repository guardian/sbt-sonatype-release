package com.gu.ship

import com.typesafe.sbt.S3Plugin
import com.typesafe.sbt.SbtGit.git
import com.typesafe.sbt.git.ConsoleGitRunner
import com.typesafe.sbt.pgp.PgpKeys
import sbt.Keys._
import sbt._
import xerial.sbt.Sonatype.SonatypeKeys

object ShipAutoPlugin extends AutoPlugin {

  //Declare dependencies on other plugins by defining the requires method
  override def requires: Plugins = empty

  object ShipKeys {

    val testAndOptionallyShip = taskKey[Unit]("travis task")

    val releaseToMaven = taskKey[Unit]("wrapper for release all")

    val releaseChangeLog = taskKey[Unit]("wrapper for shipping the changelog")

    lazy val password = settingKey[String]("the password")

  }

  import com.gu.ship.ShipAutoPlugin.ShipKeys._

  lazy val releaseToMavenDef = releaseToMaven.:=({
    // needs to be ordered not parallel
//    PgpKeys.publishSigned.doFinally(SonatypeKeys.sonatypeReleaseAll.toTask.taskValue.map(_ => ()))
    for {
      _ <- PgpKeys.publishSigned
      release <- SonatypeKeys.sonatypeReleaseAll.toTask.taskValue
    } yield ()
    ()
  })

  lazy val releaseChangeLogDef = releaseChangeLog := {
    // needs to be ordered not parallel
    for {
      _ <- ChangeLogBuild.changeLog
      _ <- S3Plugin.S3.upload.toTask.taskValue
    } yield ()
  }

  lazy val versionDef = // the version is the latest tag starting with v, however if it's not the current commit then add -SNAPSHOT on the end
    version in ThisBuild := {
      val version = latestGitTag.value.substring(1)
      val versionStem = if (version.contains("-")) version.substring(0, version.indexOf("-")) else version
      versionStem + (if (buildingNewVersion.value) "" else "-SNAPSHOT")
    }

  lazy val testAndOptionallyShipDef = testAndOptionallyShip := {
    val log = streams.value.log
    log.info(">>> log some values")
    log.info(s"gitCurrentBranch: ${git.gitCurrentBranch.value}")
    log.info(s"gitCurrentTags: ${git.gitCurrentTags.value}")
    //println(s"branch: ${git.branch.value}")
    log.info(s"gitHeadCommit: ${git.gitHeadCommit.value}")
    //  println(s"versionProperty: ${git.versionProperty.value}")
    log.info(s"version value: ${version.value}")
    log.info(s"latestGitTag: ${latestGitTag.value}")
    log.info(s"buildingNewVersion: ${buildingNewVersion.value}")
    log.info("<<< finished logging some values")
    Def.taskDyn {
      if (buildingNewVersion.value)
        Def.task {
          val log = streams.value.log
          log.info("shipIt")
          // happens in parallel (I hope!)
          releaseToMaven.value
          releaseChangeLog.value
          ()
        }
      else {
        Def.task {
          () // test has already happened hopefully
        }
      }
    }.value
  }

  lazy val passwordDef = password := {
    val password = System.getenv("SONATYPE_PASSWORD")
    if (password == null) ""
    else password
  }

  // the latestGitTag is used to find out what version to publish as
  lazy val latestGitTag = settingKey[String]("either v1.0 or v1.0-1-2fdd54b depends if it's on the tag")

  lazy val latestGitTagDef = latestGitTag := ConsoleGitRunner("describe", "--match","v[0-9]*","HEAD")(file("."))

  lazy val buildingNewVersion = settingKey[Boolean]("whether we're building a new version to ship")

  lazy val buildingNewVersionDef = buildingNewVersion := git.gitCurrentTags.value.contains(latestGitTag.value)

  override def projectSettings: Seq[Def.Setting[_]] = Seq[Setting[_]](
    testAndOptionallyShipDef,
    versionDef,
    testAndOptionallyShip <<= testAndOptionallyShip.dependsOn(test in Test),
    releaseChangeLogDef,
    latestGitTagDef,
    buildingNewVersionDef,
  passwordDef
  )

}
