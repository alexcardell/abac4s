import dependencies._

lazy val scala212 = "2.12.15"
lazy val scala213 = "2.13.8"
lazy val crossVersions = Seq(scala212, scala213)

ThisBuild / organization := "io.cardell"
ThisBuild / version := "0.0.1-SNAPSHOT"
ThisBuild / scalaVersion := scala212

lazy val root = project
  .in(file("."))
  .aggregate(core)
  .settings(
    crossScalaVersions := Nil,
    publish / skip := true
  )

lazy val core = project
  .in(file("core"))
  .settings(
    crossScalaVersions := crossVersions,
    libraryDependencies ++= mainDeps ++ testDeps,
    testFrameworks += new TestFramework("weaver.framework.CatsEffect")
  )
