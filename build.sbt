libraryDependencies += compilerPlugin("org.scala-lang.plugins" % ("scala-continuations-plugin_" + scalaVersion.value) % "1.0.2")

scalaSource in Compile <<= baseDirectory(_ / "src")

unmanagedJars in Compile <<= baseDirectory map { base => ((base ** "lib") ** "*.jar").classpath }

classDirectory in Compile <<= target(_ / "scala/classes")

classDirectory in Test <<= target(_ / "scala/test-classes")

autoCompilerPlugins := true

unmanagedClasspath in Runtime <+= (baseDirectory) map { bd => Attributed.blank(bd / "basicexamples" /"configs") }

fork := true

baseDirectory in run := baseDirectory.value

scalacOptions += "-P:continuations:enable"

javaOptions in run += "-Xmx2G" 

javaOptions in run += "-Xms1G"
