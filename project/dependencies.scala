import sbt._

object dependencies {
  lazy val mainDeps = Seq (
    "org.typelevel" %% "cats-core"    % "2.7.0",
    "org.typelevel" %% "cats-effect"    % "3.3.11",
    "org.typelevel" %% "mouse"    % "1.0.10"
  )

  lazy val testDeps = Seq(
    "com.disneystreaming" %% "weaver-cats" % "0.7.11" ,
    "com.disneystreaming" %% "weaver-discipline" % "0.7.11" ,
    "com.disneystreaming" %% "weaver-scalacheck" % "0.7.11" ,
    "org.typelevel" %% "cats-laws" % "2.7.0" 
  ).map(_ % Test)
}
