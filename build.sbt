libraryDependencies += "junit" % "junit" % "4.12"
libraryDependencies += "org.scalatest" % "scalatest_2.11" % "2.2.4"

lazy val root = (project in file(".")).
  settings(
    name := "hello",
    version := "1.0",
    scalaVersion := "2.11.7"
  )
