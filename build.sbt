Global / onChangedBuildSource := ReloadOnSourceChanges

lazy val webcralwer = (project in file("."))
  .enablePlugins(
    JavaAppPackaging
  )
  .settings(
    organization := "henryrowett",
    name := "webcrawler",
    scalaVersion := "2.13.5",
    version := "1.0",
    scalafmtOnCompile := true,
    libraryDependencies ++= http4s ++ fs2 ++ log4cats ++ jsoup,
    libraryDependencies ++= Seq(
      "org.typelevel" %% "munit-cats-effect-3" % "1.0.1" % "test"
    ),
    scalacOptions -= "-Xfatal-warnings", // enable all options from sbt-tpolecat except fatal warnings
    // Test
    testOptions += Tests.Argument(TestFrameworks.ScalaTest, "-oF"),
    testFrameworks += new TestFramework("munit.Framework"),
  )
  .settings(
    Test / testOptions += Tests.Argument("-oF"), // log to std output the full test stacktraces
  )

def dep(org: String, version: String, prefix: String = "")(modules: String*) =
  modules.map(m => org %% (prefix ++ m) % version)

def jdep(org: String, version: String, prefix: String = "")(modules: String*) =
  modules.map(m => org % (prefix ++ m) % version)

val fs2 = dep("co.fs2", "3.0.0")("fs2-core","fs2-reactive-streams","fs2-io")
val http4s = dep("org.http4s", "1.0.0-M16", "http4s-")("blaze-server","blaze-client","dsl")
val log4cats = dep("org.typelevel", "2.0.0", "log4cats-")("core","slf4j")

val logback = jdep("ch.qos.logback", "1.2.3", "logback-")("core", "classic")
val jsoup = jdep("org.jsoup", "1.13.1")("jsoup")