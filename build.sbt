ThisBuild / organization := "io.cardell"
ThisBuild / name := "abac4s"
ThisBuild / scmInfo := Some(
  ScmInfo(
    url("https://github.com/alexcardell/abac4s"),
    "scm:git@github.com:alexcardell/abac4s.git"
  )
)
ThisBuild / licenses := List(
  "Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")
)
ThisBuild / developers := List(
  Developer(
    "alexcardell",
    "Alex Cardell",
    "29524087+alexcardell@users.noreply.github.com",
    url("https://github.com/alexcardell")
  )
)
ThisBuild / homepage := Some(
  url("https://github.com/alexcardell/abac4s")
)
ThisBuild / publishTo := sonatypePublishToBundle.value
ThisBuild / sonatypeCredentialHost := "s01.oss.sonatype.org"
ThisBuild / versionScheme := Some("early-semver")

lazy val scala2_12 = "2.12.15"
lazy val scala2_13 = "2.13.8"
lazy val scala3_1 = "3.1.3"
lazy val scala3_2 = "3.2.0"
lazy val crossVersions = Seq(scala2_13)
ThisBuild / crossScalaVersions := crossVersions
ThisBuild / scalaVersion := scala2_13

ThisBuild / scalafixDependencies += "com.github.liancheng" %% "organize-imports" % "0.6.0"
ThisBuild / semanticdbEnabled := true
ThisBuild / semanticdbVersion := scalafixSemanticdb.revision

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
    name := "abac4s"
  )

lazy val deps = Seq(
  libraryDependencies ++= Seq(
    "org.typelevel" %%% "cats-core" % "2.7.0",
    "com.disneystreaming" %%% "weaver-cats" % "0.8.0" % Test
  )
)

lazy val core = crossProject(JVMPlatform, JSPlatform, NativePlatform)
  .crossType(CrossType.Pure)
  .in(file("core"))
  .settings(commonSettings)
  .settings(deps: _*)
  .settings(
    moduleName := "abac4s-core",
    testFrameworks += new TestFramework("weaver.framework.CatsEffect")
  )

lazy val docs = project
  .in(file("mdoc-src"))
  .dependsOn(core.jvm)
  .settings(
    libraryDependencies += "org.typelevel" %%% "cats-effect" % "3.3.14"
  )
  .settings(
    moduleName := "abac4s-docs",
    mdocVariables := Map(
      "VERSION" -> version.value
    )
  )
  .enablePlugins(MdocPlugin)
