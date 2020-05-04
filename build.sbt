name := "neo4s"

lazy val catsVersion = "2.1.1"
lazy val catsEffectVersion = "2.1.2"
lazy val fs2Version = "2.3.0"
lazy val shapelessVersion = "2.3.3"
lazy val neo4jDriverVersion = "4.0.0"
lazy val kindProjectorVersion = "0.11.0"
lazy val scalaReflectVersion = "2.13.2"
lazy val slf4jVersion = "1.7.30"
lazy val scalaTestVersion = "3.1.1"
lazy val testContainersVersion = "0.36.0"

lazy val commonSettings = Seq(
  libraryDependencies ++= Seq(
    "org.scalatest" %% "scalatest" % scalaTestVersion % Test
  ),
  scalacOptions := Seq(
    "-encoding",
    "utf8",
    "-Xfatal-warnings",
    "-Xlog-implicits",
    "-deprecation",
    "-unchecked",
    "-language:implicitConversions",
    "-language:higherKinds",
    "-language:existentials",
    "-language:postfixOps"
  ),
  scalaVersion := "2.13.2",
  addCompilerPlugin(("org.typelevel" %% "kind-projector" % kindProjectorVersion).cross(CrossVersion.full)),
  Test / parallelExecution := false
)

lazy val neo4s = project
  .in(file("."))
  .settings(commonSettings)
  .aggregate(
    core
  )

lazy val core = project
  .in(file("modules/core"))
  .settings(commonSettings)
  .settings(
    name := "neo4s-core",
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-core" % catsVersion,
      "org.typelevel" %% "cats-effect" % catsEffectVersion,
      "org.typelevel" %% "cats-free" % catsVersion,
      "com.chuusai" %% "shapeless" % shapelessVersion,
      "co.fs2" %% "fs2-core" % fs2Version,
      "co.fs2" %% "fs2-io" % fs2Version,
      "co.fs2" %% "fs2-reactive-streams" % fs2Version,
      "org.scala-lang" % "scala-reflect" % scalaReflectVersion,
      "org.slf4j" % "slf4j-api" % slf4jVersion,
      "org.neo4j.driver" % "neo4j-java-driver" % neo4jDriverVersion,
      "com.dimafeng" %% "testcontainers-scala" % testContainersVersion % Test,
      "com.dimafeng" %% "testcontainers-scala-neo4j" % testContainersVersion % Test
    )
  )

lazy val examples = project
  .in(file("modules/examples"))
  .dependsOn(core)
  .settings(commonSettings)
  .settings(
    name := "neo4s-examples",
    libraryDependencies ++= Seq(
      "ch.qos.logback" % "logback-classic" % "1.2.3"
    )
  )
