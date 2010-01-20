package xsbt.deptest

import java.io.File
import java.net.{URL, URLClassLoader}

import xsbt.{FileUtilities, GlobFilter, Paths}
import xsbt.test.{CommentHandler, FileCommands, FilteredLoader, ScriptRunner, TestScriptParser}

object BuildManagerTest
{
	val colored = false//System.getProperty("os.name").toLowerCase.indexOf("win") < 0
	def main(args: Array[String])
	{
		if(args.isEmpty)
			fail("No arguments provided.  Expected path to Scala home directory and a list of tests to run.")
		val scalaHomePath = new File(args(0))
		ScalaJars(scalaHomePath).fold(fail, run(args))
	}
	def run(args: Array[String])(scalaJars: ScalaJars)
	{
		val results = (new ManagerTest(scalaJars, args.slice(1), colored)).run
		val failures = results.flatMap { case (test, success) => if(success) Nil else List(test) }
		if(!failures.isEmpty)
			fail(failures.mkString("\nFailed:\n\t", "\n\t", ""))
	}
	def fail(msg: String)
	{
		System.err.println(msg)
		System.exit(1)
	}
	def classpath(scalaHome: File): Either[String, Array[URL]] =
	{
		val lib = new File(scalaHome, "lib")
		val jarFiles = Array(new File(lib, "scala-library.jar"), new File(lib, "scala-compiler.jar"))
		val jars = jarFiles.map(_.toURI.toURL)
		val absent = jarFiles.filter(!_.exists)
		if(absent.isEmpty)
			Right(jars)
		else
			Left("Scala jar(s) do not exist:\n\t" + absent.map(_.getAbsolutePath).mkString("\n\t"))
	}
}
class ManagerTest(scalaJars: ScalaJars, tests: Seq[String], colored: Boolean) extends NotNull
{
	def run =
	{
		val loader = new URLClassLoader(scalaJars.classpath, new FilteredLoader(getClass.getClassLoader))
		tests.toList.flatMap(expandPath).map { testScript =>
			val path = testScript.getPath
			println("\n\n\t" + color("Test: " + testScript, Console.BLUE) + "\n")
			val scriptDirectory = testScript.getParentFile
			import Paths._
			FileUtilities.withTemporaryDirectory { base =>
				FileUtilities.copy( (scriptDirectory ***) x FileMapper.rebase(scriptDirectory, base) )
				(path, runScript(path, testScript, base, loader))
			}
		}
	}
	def expandPath(path: String): Set[File] =
	{
		import Paths._
		import GlobFilter._
		def expand(bases: Set[File], components: List[String]): Set[File] =
			components match
			{
				case Nil => bases
				case x :: xs => expand(bases * (x -- HiddenFileFilter), xs)
					//expand(if(x.contains("*")) bases * (x -- HiddenFileFilter) else bases / x, xs)
			}
		expand(Set(new File(".")), path.split("/").toList)
	}
	def runScript(path: String, scriptFile: File, baseDirectory: File, loader: ClassLoader): Boolean =
	{
		val run = new ScriptRunner
		val fileHandler = new FileCommands(baseDirectory)
		val compileHandler = new ManagerInterface(scalaJars.libraryJar, baseDirectory, true, loader)
		val parser = new TestScriptParser(Map('$' -> fileHandler, '>' -> compileHandler, '#' -> CommentHandler))
		try
		{
			run(parser.parse(scriptFile))
			System.out.println("\n" + color(" + " + path, Console.GREEN))
			true
		}
		catch {
			case e: Exception =>
				System.err.println("\n" + color(" x " + path, Console.RED))
				processException(e); false
		}
	}
	final def processException(e: Throwable)
	{
		e match
		{
			case _: xsbt.test.TestException | _: xsbt.deptest.TestException =>
				System.err.println("   " + e.getMessage)
				if(e.getCause ne null)
					processException(e.getCause)
			case _ =>
				e.printStackTrace
		}
	}
	def color(msg: String, color: String) =
		if(colored) color + msg + reset else msg
	val reset = Console.RESET
}
