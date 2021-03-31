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
    // dependencies
    libraryDependencies ++= http4s ++ logging ++ fs2 ++ log4cats,
    libraryDependencies ++= Seq(
      "org.typelevel" %% "munit-cats-effect-2" % "0.7.0" % "it, test"
    ),
    scalacOptions -= "-Xfatal-warnings", // enable all options from sbt-tpolecat except fatal warnings
    // Test
    testOptions += Tests.Argument(TestFrameworks.ScalaTest, "-oF"),
    testFrameworks += new TestFramework("munit.Framework"),
  )
  .configs(IntegrationTest)
  .settings(
    Test / testOptions += Tests.Argument("-oF"), // log to std output the full test stacktraces
    Defaults.itSettings,
    inConfig(IntegrationTest)(org.scalafmt.sbt.ScalafmtPlugin.scalafmtConfigSettings)
  )

def dep(org: String, version: String, prefix: String = "")(modules: String*) =
  modules.map(m => org %% (prefix ++ m) % version)

def jdep(org: String, version: String, prefix: String = "")(modules: String*) =
  modules.map(m => org % (prefix ++ m) % version)

val fs2 = dep("co.fs2", "3.0.0")("fs2-core", "fs2-reactive-streams", "fs2-io")
val http4s = dep("org.http4s", "1.0.0", "http4s-")("blaze-server", "blaze-client")
val logging = dep("com.typesafe.scala-logging", "3.9.2")("scala-logging")
val log4cats = dep("io.chrisdavenport", "1.1.1", "log4cats-")("core", "slf4j")

val logstash = jdep("net.logstash.logback", "4.11")("logstash-logback-encoder")
val logback = jdep("ch.qos.logback", "1.2.3")("logback-classic", "logback-access")