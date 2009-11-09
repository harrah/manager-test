import sbt._

class ManagerTest(info: ProjectInfo) extends DefaultProject(info)
{
	override def scratch = true
	override def compileOptions = CompileOption("-Xno-varargs-conversion") :: Nil
	val sbtTest = "org.scala-tools.sbt" %% "test" % "0.6.3"
}