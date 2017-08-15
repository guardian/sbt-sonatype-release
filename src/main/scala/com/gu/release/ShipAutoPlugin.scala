package com.gu.release

import com.typesafe.sbt.SbtGit.git
import com.typesafe.sbt.git.ConsoleGitRunner
import com.typesafe.sbt.pgp.PgpKeys
import sbt.Keys._
import sbt._
import xerial.sbt.Sonatype
import xerial.sbt.Sonatype.SonatypeCommand

object ShipAutoPlugin extends AutoPlugin {

  //Declare dependencies on other plugins by defining the requires method
  override def requires: Plugins = empty

  // these are the externally exposed keys
  object autoImport {

    val testAndOptionallyShip = taskKey[Unit]("travis task")

  }

  import com.gu.release.ShipAutoPlugin.autoImport._

  // the latestGitTag is used to find out what version to publish as
  lazy val latestGitTag = settingKey[String]("either v1.0 or v1.0-1-2fdd54b depends if it's on the tag")

  lazy val buildingNewVersion = settingKey[Boolean]("whether we're building a new version to ship")

  val shipToSonatype = Command.command("shipToSonatype") { state =>
    "test" ::
      "publishSigned" ::
      "sonatypeReleaseAll" ::
      state
  }

  /**
    * Convert the given command string to a release step action, preserving and invoking remaining commands
    * stolen from https://github.com/sbt/sbt-release/blob/7e0abd698f66f75f9e85ebd0f722b67b581d96c3/src/main/scala/ReleasePlugin.scala#L102
    */
  def releaseStepCommandAndRemaining(command: String, log: Logger): State => State = { st: State =>

    @annotation.tailrec
    def runCommand(command: String, state: State): State = {
      import sbt.complete.Parser
      log.info(s"running command: $command")
      val nextState = Parser.parse(command, state.combinedParser) match {
        case Right(cmd) => cmd()
        case Left(msg) => throw sys.error(s"Invalid programmatic input:\n$msg")
      }
      nextState.remainingCommands.toList match {
        case Nil => nextState
        case head :: tail => runCommand(head, nextState.copy(remainingCommands = tail))
      }
    }
    runCommand(command, st.copy(remainingCommands = Nil)).copy(remainingCommands = st.remainingCommands)
  }

  override def projectSettings: Seq[Def.Setting[_]] =
    Sonatype.sonatypeSettings ++
    Seq[Setting[_]](

    buildingNewVersion := git.gitCurrentTags.value.contains(latestGitTag.value)
      ,
    latestGitTag := ConsoleGitRunner("describe", "--match","v[0-9]*","HEAD")(file("."))
      ,
    // the version is the latest tag starting with v, however if it's not the current commit then add -SNAPSHOT on the end
    version in ThisBuild := {
      val version = latestGitTag.value.substring(1)
      val versionStem = if (version.contains("-")) version.substring(0, version.indexOf("-")) else version
      versionStem + (if (buildingNewVersion.value) "" else "-SNAPSHOT")
    }
      ,
    testAndOptionallyShip := {
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
            releaseStepCommandAndRemaining("shipToSonatype", log)(state.value)
            log.info("shipped")
            ()
          }
        else {
          Def.task {
            () // test has already happened hopefully
          }
        }
      }.value
    },
      commands += shipToSonatype
//      ,
    // to release we have to publish the signed artefacts first
//    SonatypeCommand.sonatypeReleaseAll <<= SonatypeCommand.sonatypeReleaseAll.dependsOn(PgpKeys.publishSigned)
//      ,
    // not needed usually, but we don't want to publish something we didn't test
//    PgpKeys.publishSigned <<= PgpKeys.publishSigned.dependsOn(test in Test)
      ,
    // to ship it we have to test first
    testAndOptionallyShip <<= testAndOptionallyShip.dependsOn(test in Test)

  )

}
