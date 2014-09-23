scalaSource in Compile <<= baseDirectory(_ / "src")

unmanagedJars in Compile <<= baseDirectory map { base => ((base ** "lib") ** "*.jar").classpath }

autoCompilerPlugins := true

classDirectory in Compile <<= target(_ / "scala/classes")

classDirectory in Test <<= target(_ / "scala/test-classes")

fork := true

unmanagedClasspath in Runtime <+= (baseDirectory) map { bd => Attributed.blank(bd / "basicexamples" /"configs") }

baseDirectory in run := baseDirectory.value

javaOptions in run += "-Xmx2G" 

javaOptions in run += "-Xms1G"
