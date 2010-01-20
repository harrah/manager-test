import sbt._

class ManagerTest(info: ProjectInfo) extends DefaultProject(info)
{
	override def scratch = true
	override def compileOptions = CompileOption("-Xno-varargs-conversion") :: Nil
	override def managedStyle = ManagedStyle.Ivy
	val technically = Resolver.url("databinder.net", new java.net.URL("http://databinder.net/repo/"))

	val sbtIO = "org.scala-tools.sbt" %% "io" % "0.6.11"
	val sbtTest = "org.scala-tools.sbt" %% "test" % "0.6.11" intransitive()
}
