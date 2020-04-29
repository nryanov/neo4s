name := "neo4s"

lazy val catsVersion = "2.1.1"
lazy val catsEffectVersion = "2.1.1"
lazy val shapelessVersion = "2.3.3"
lazy val neo4jDriverVersion = "4.0.0"
lazy val kindProjectorVersion = "0.11.0"
lazy val scalaReflectVersion = "2.13.2"
lazy val scalaTestVersion = "3.1.1"

lazy val commonSettings = Seq(
  libraryDependencies ++= Seq(
    "org.typelevel" %% "cats-core" % catsVersion,
    "org.typelevel" %% "cats-effect" % catsVersion,
    "com.chuusai" %% "shapeless" % shapelessVersion,
    "org.scala-lang" % "scala-reflect" % scalaReflectVersion,
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
      "org.neo4j.driver" % "neo4j-java-driver" % neo4jDriverVersion
    )
  )
