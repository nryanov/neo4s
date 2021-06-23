lazy val catsVersion = "2.6.1"
lazy val catsEffectVersion = "3.1.1"
lazy val neo4jDriverVersion = "4.3.2"
lazy val shapelessVersion = "2.3.7"
lazy val fs2Version = "2.5.6"
lazy val kindProjectorVersion = "0.13.0"
lazy val slf4jVersion = "1.7.30"
lazy val logbackVersion = "1.2.3"
lazy val scalaTestVersion = "3.2.9"
lazy val testContainersVersion = "0.39.5"

val scala2_12 = "2.12.13"
val scala2_13 = "2.13.5"

val compileAndTest = "compile->compile;test->test"

lazy val buildSettings = Seq(
  sonatypeProfileName := "com.nryanov",
  organization := "com.nryanov.neo4s",
  homepage := Some(url("https://github.com/nryanov/neo4s")),
  licenses := List("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")),
  developers := List(
    Developer(
      "nryanov",
      "Nikita Ryanov",
      "ryanov.nikita@gmail.com",
      url("https://nryanov.com")
    )
  ),
  scalaVersion := scala2_13,
  crossScalaVersions := Seq(scala2_12, scala2_13)
)

lazy val noPublish = Seq(
  publish := {},
  publishLocal := {},
  publishArtifact := false
)

def compilerOptions(scalaVersion: String) = Seq(
  "-deprecation",
  "-encoding",
  "UTF-8",
  "-feature",
  "-language:existentials",
  "-language:higherKinds",
  "-language:implicitConversions",
  "-unchecked",
  "-Ywarn-dead-code",
  "-Ywarn-numeric-widen",
  "-Xlog-implicits",
  "-Xlint",
  "-language:existentials",
  "-language:postfixOps"
) ++ (CrossVersion.partialVersion(scalaVersion) match {
  case Some((2, scalaMajor)) if scalaMajor == 12 => scala212CompilerOptions
  case Some((2, scalaMajor)) if scalaMajor == 13 => scala213CompilerOptions
})

lazy val scala212CompilerOptions = Seq(
  "-Yno-adapted-args",
  "-Ywarn-unused-import",
  "-Xfuture"
)

lazy val scala213CompilerOptions = Seq(
  "-Wunused:imports"
)

lazy val commonSettings = Seq(
  libraryDependencies ++= Seq(
    "org.scalatest" %% "scalatest" % scalaTestVersion % Test
  ),
  scalacOptions ++= compilerOptions(scalaVersion.value),
  addCompilerPlugin(("org.typelevel" %% "kind-projector" % kindProjectorVersion).cross(CrossVersion.full)),
  Test / parallelExecution := false
)

lazy val allSettings = commonSettings ++ buildSettings

lazy val neo4s = project
  .in(file("."))
  .settings(allSettings)
  .settings(noPublish)
  .aggregate(
    core
  )

lazy val core = project
  .in(file("modules/core"))
  .settings(allSettings)
  .settings(
    name := "neo4s-core",
    libraryDependencies ++= Seq(
      "org.neo4j.driver" % "neo4j-java-driver" % neo4jDriverVersion,
      "org.typelevel" %% "cats-core" % catsVersion,
      "org.typelevel" %% "cats-effect" % catsEffectVersion,
      "org.typelevel" %% "cats-free" % catsVersion,
      "com.chuusai" %% "shapeless" % shapelessVersion,
      "co.fs2" %% "fs2-core" % fs2Version,
      "co.fs2" %% "fs2-io" % fs2Version,
      "co.fs2" %% "fs2-reactive-streams" % fs2Version,
      "org.slf4j" % "slf4j-api" % slf4jVersion,
      scalaOrganization.value % "scala-reflect" % scalaVersion.value % Provided,
      scalaOrganization.value % "scala-compiler" % scalaVersion.value % Provided,
      "com.dimafeng" %% "testcontainers-scala" % testContainersVersion % Test,
      "com.dimafeng" %% "testcontainers-scala-neo4j" % testContainersVersion % Test,
      "ch.qos.logback" % "logback-classic" % logbackVersion % Test
    )
  )

lazy val examples = project
  .in(file("examples"))
  .dependsOn(core)
  .settings(allSettings)
  .settings(noPublish)
  .settings(
    name := "examples",
    libraryDependencies ++= Seq(
      "ch.qos.logback" % "logback-classic" % logbackVersion
    )
  )
