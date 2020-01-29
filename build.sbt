name := "once-ler"
organization := "com.github.hrhino"
scalaVersion in ThisBuild := "2.12.11-bin-SNAPSHOT"

lazy val runtime = project
  .in(file("runtime"))

lazy val plugin = project
  .in(file("plugin"))
  .settings(
    libraryDependencies += "org.scala-lang" % "scala-compiler" % scalaVersion.value,
  )

lazy val test = project
  .in(file("test"))
  .dependsOn(runtime)
  .settings(
    scalacOptions += s"-Xplugin:${jarOf(plugin).value}"
  )

commands in Global += Command.command("repl")("test/console" :: _)

def jarOf(project: Project): TaskKey[File] = packageBin in Compile in project
