package de.johoop.jacoco4sbt

import de.johoop.jacoco4sbt.JacocoPlugin.autoImport.Jacoco
import sbt.Keys._
import sbt._

object JacocoItPlugin extends BaseJacocoPlugin with Merging {

  object autoImport {
    lazy val ItJacoco: Configuration = config("it-jacoco").extend(IntegrationTest).hide

    lazy val merge: TaskKey[Unit] = taskKey[Unit]("Merges all '*.exec' files into a single data file.")

    lazy val mergedExecutionDataFile: SettingKey[File] =
      settingKey[File]("Execution data file contain unit test and integration test data.")

    lazy val mergeReports: SettingKey[Boolean] =
      settingKey[Boolean]("Indication whether to merge the unittest and integration test reports. Defaults to true.")
  }

  import autoImport._

  override protected val pluginConfig: Configuration = ItJacoco
  lazy val srcConfig: Configuration = IntegrationTest

  lazy val conditionalMerge: Def.Initialize[Task[Unit]] = Def.task {
    conditionalMergeAction(
      (executionDataFile in Jacoco).value,
      (executionDataFile in ItJacoco).value,
      (mergedExecutionDataFile in ItJacoco).value,
      streams.value,
      mergeReports.value)
  }

  lazy val forceMerge: Def.Initialize[Task[Unit]] = Def.task {
    mergeAction(
      (executionDataFile in Jacoco).value,
      (executionDataFile in ItJacoco).value,
      (mergedExecutionDataFile in ItJacoco).value,
      streams.value)
  }

  override def projectSettings: Seq[Setting[_]] =
    super.projectSettings ++
      Seq(
        report in ItJacoco := ((report in ItJacoco) dependsOn conditionalMerge).value,
        merge := forceMerge.value,
        mergeReports := true,
        (mergedExecutionDataFile in ItJacoco) := (outputDirectory in ItJacoco).value / "jacoco-merged.exec",
        (report in ItJacoco) := reportAction(
          (outputDirectory in ItJacoco).value,
          (mergedExecutionDataFile in ItJacoco).value,
          (reportFormats in ItJacoco).value,
          (reportTitle in ItJacoco).value,
          (coveredSources in ItJacoco).value,
          (classesToCover in ItJacoco).value,
          (sourceEncoding in ItJacoco).value,
          (sourceTabWidth in ItJacoco).value,
          (thresholds in ItJacoco).value,
          (streams in ItJacoco).value
        ),
        report in ItJacoco := ((report in ItJacoco) dependsOn conditionalMerge).value
      )
}