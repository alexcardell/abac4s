import dependencies._

lazy val scala2_12 = "2.12.15"
lazy val scala2_13 = "2.13.8"
lazy val scala3_1 = "3.1.3"
lazy val scala3_2 = "3.2.0"

/* lazy val crossVersions = Seq(scala2_12, scala2_13, scala3_1, scala3_2) */
lazy val crossVersions = Seq(scala2_12, scala2_13)

ThisBuild / organization := "io.cardell"
ThisBuild / version := "0.0.1-SNAPSHOT"
ThisBuild / scalaVersion := scala2_13

ThisBuild / crossScalaVersions := crossVersions

ThisBuild / scalafixDependencies += "com.github.liancheng" %% "organize-imports" % "0.6.0"
ThisBuild /  semanticdbEnabled := true
ThisBuild /  semanticdbVersion := scalafixSemanticdb.revision

lazy val commonSettings = Seq(
  addCompilerPlugin(
    "org.typelevel" % "kind-projector" % "0.13.2" cross CrossVersion.full
  ),
  scalacOptions := Seq("-Wunused")
)

lazy val root = crossProject(JVMPlatform, JSPlatform, NativePlatform)
  .crossType(CrossType.Pure)
  .in(file("."))
  .aggregate(core)
  .settings(
    moduleName := "abac4s",
    name := "abac4s"
  )

lazy val core = crossProject(JVMPlatform, JSPlatform, NativePlatform)
  .crossType(CrossType.Pure)
  .in(file("core"))
  .settings(commonSettings)
  .settings(
    moduleName := "abac4s-core",
    name := "abac4s core",
    libraryDependencies ++= mainDeps ++ testDeps,
    testFrameworks += new TestFramework("weaver.framework.CatsEffect")
  )

ThisBuild / credentials +=
  Credentials("GnuPG Key ID", "gpg", "0x24D44BD082D48FBE", "")
