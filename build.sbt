organization := "com.github.acrisci"
name := "commander"
version := "0.1.1"
scalaVersion := "2.12.0"
crossScalaVersions := Seq("2.10.6", "2.11.8", "2.12.0")

libraryDependencies ++= Seq(
  //"com.github.pathikrit" %% "better-files" % "2.16.0" withSources(),
  "org.scalatest"        %% "scalatest"   % "3.0.0" % "test"
)

scalacOptions ++= (
  scalaVersion {
    case sv if sv.startsWith("2.10") => Nil
    case _ => List(
      "-target:jvm-1.8",
      "-Ywarn-unused"
    )
  }.value ++ Seq(
    "-deprecation",
    "-encoding", "UTF-8",
    "-feature",
    "-unchecked",
    "-Ywarn-adapted-args",
    "-Ywarn-dead-code",
    "-Ywarn-numeric-widen",
    "-Ywarn-value-discard",
    "-Xfuture",
    "-Xlint"
  )
)

javacOptions ++= Seq(
  "-Xlint:deprecation",
  "-Xlint:unchecked",
  "-source", "1.8",
  "-target", "1.8",
  "-g:vars"
)

testOptions in Test += Tests.Argument(TestFrameworks.ScalaTest, "-oF")

publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}

pomExtra :=
  <url>https://github.com/acrisci/commander-scala</url>
  <licenses>
    <license>
      <name>MIT</name>
      <url>https://opensource.org/licenses/MIT</url>
      <distribution>repo</distribution>
    </license>
  </licenses>
  <scm>
    <url>git@github.com:acrisci/commander-scala</url>
    <connection>scm:git:git@github.com:acrisci/commander-scala.git</connection>
  </scm>
  <developers>
    <developer>
      <id>tonyctl</id>
      <name>Tony Crisci</name>
      <url>http://dubstepdish.com</url>
    </developer>
  </developers>

//pgpReadOnly := false

//logLevel := Level.Warn

// Only show warnings and errors on the screen for compilations.
// This applies to both test:compile and compile and is Info by default
logLevel in compile := Level.Warn

// Level.INFO is needed to see detailed output when running tests
logLevel in test := Level.Debug

// define the statements initially evaluated when entering 'console', 'console-quick', but not 'console-project'
initialCommands in console := """
                                |""".stripMargin

cancelable := true
