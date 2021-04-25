import ReleaseTransformations._

lazy val catsVersion = "2.6.0"
lazy val catsEffectVersion = "2.5.0"
lazy val neo4jDriverVersion = "4.0.3"
lazy val shapelessVersion = "2.3.4"
lazy val fs2Version = "2.5.5"
lazy val kindProjectorVersion = "0.11.3"
lazy val slf4jVersion = "1.7.30"
lazy val logbackVersion = "1.2.3"
lazy val scalaTestVersion = "3.1.4"
lazy val testContainersVersion = "0.39.3"

lazy val buildSettings = Seq(
  organization := "com.nryanov.neo4s",
  scalaVersion := "2.13.2",
  crossScalaVersions := Seq("2.12.10", "2.13.2")
)

lazy val noPublish = Seq(
  publish := {},
  publishLocal := {},
  publishArtifact := false
)

lazy val publishSettings = Seq(
  publishMavenStyle := true,
  publishArtifact := true,
  publishTo := {
    val nexus = "https://oss.sonatype.org/"
    if (isSnapshot.value)
      Some("snapshots".at(nexus + "content/repositories/snapshots"))
    else
      Some("releases".at(nexus + "service/local/staging/deploy/maven2"))
  },
  publishArtifact in Test := false,
  releasePublishArtifactsAction := PgpKeys.publishSigned.value,
  releaseIgnoreUntrackedFiles := true,
  licenses := Seq("Apache 2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")),
  homepage := Some(url("https://github.com/nryanov/neo4s")),
  autoAPIMappings := true,
  apiURL := Some(url("https://github.com/nryanov/neo4s")),
  scmInfo := Some(
    ScmInfo(
      url("https://github.com/nryanov/neo4s"),
      "scm:git:git@github.com:nryanov/neo4s.git"
    )
  ),
  releaseVersionBump := sbtrelease.Version.Bump.Minor,
  releaseCrossBuild := true,
  releaseProcess := {
    Seq[ReleaseStep](
      checkSnapshotDependencies,
      inquireVersions,
      runClean,
      runTest,
      setReleaseVersion,
      releaseStepCommandAndRemaining("+publishSigned"),
      releaseStepCommand("sonatypeBundleRelease"),
      setNextVersion
    )
  },
  pomExtra :=
    <developers>
      <developer>
        <id>nryanov</id>
        <name>Nikita Ryanov</name>
      </developer>
    </developers>
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

lazy val allSettings = commonSettings ++ buildSettings ++ publishSettings

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
