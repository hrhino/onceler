scalaVersion in ThisBuild := "2.12.11-bin-SNAPSHOT"

lazy val runtime = project
  .in(file("runtime"))

lazy val plugin = project
  .in(file("plugin"))
  .settings(
    libraryDependencies += "org.scala-lang" % "scala-compiler" % scalaVersion.value,
  )

lazy val tests = project
  .in(file("tests"))
  .dependsOn(plugin, runtime)
  .settings(
    libraryDependencies += "org.scala-lang.modules" %% "scala-partest" % "1.1.9",
  )
  .settings(inConfig(Test)(List(
    scalacOptions += s"-Xplugin:${jarOf(plugin).value}",
    fork := true,
    // partest magic incantations
    testFrameworks += TestFramework("scala.tools.partest.sbt.Framework"),
    javaOptions ++= List(
      s"-Dpartest.root=${(baseDirectory in LocalProject("tests")).value}",
    ),
    testOptions += Tests.Argument(s"-Dpartest.scalac_opts=-Xplugin:${(packageBin in (plugin, Compile)).value}"),
    definedTests := {
      object fingerprint extends testing.AnnotatedFingerprint { val isModule = true; val annotationName = "partest" }
      new TestDefinition("partest", fingerprint, explicitlySpecified = true, selectors = Array.empty)
    } :: Nil,
  )))

commands in Global += Command.command("repl")("tests/test:console" :: _)

def jarOf(project: Project): TaskKey[File] = packageBin in Compile in project
