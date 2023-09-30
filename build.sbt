ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.3.1"

lazy val root = (project in file("."))
  .settings(
    name := "ScalaBalancing",
    idePackagePrefix := Some("systems.miso")
  )

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "3.2.15" % "test",
  "org.scala-lang.modules" %% "scala-xml" % "2.1.0",
  "org.apache.logging.log4j" % "log4j-api" % "2.20.0",
  "org.apache.logging.log4j" % "log4j-core" % "2.20.0",
  "org.mongodb" % "mongodb-driver-sync" % "4.10.2",
  "org.mongodb" % "bson" % "4.10.2"

)
