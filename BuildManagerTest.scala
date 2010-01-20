package xsbt.deptest

import java.io.File
import java.net.URLClassLoader

import xsbt.FileUtilities
import xsbt.test.{CommentHandler, FileCommands, FilteredLoader, ScriptRunner, TestScriptParser}

object BuildManagerTest
{
	def main(args: Array[String]): Unit =
	{
		val results = (new ManagerTest(new File(args(0)), args.slice(1))).run
		val failures = results.flatMap { case (test, success) => if(success) Nil else List(test) }
		if(failures.isEmpty)
			()
		else
		{
			println(failures.mkString("Failed:\n\t", "\n\t", ""))
			System.exit(1)
		}
	}
}
class ManagerTest(scalaHome: File, tests: Seq[String]) extends NotNull
{
	def run =
	{
		val lib = new File(scalaHome, "lib")
		val jarFiles = Array(new File(lib, "scala-library.jar"), new File(lib, "scala-compiler.jar"))
		val jars = jarFiles.map(_.toURI.toURL)
		jarFiles.foreach(jar => assert(jar.exists, "Scala jar " + jar.getAbsolutePath + " does not exist"))
		val loader = new URLClassLoader(jars, new FilteredLoader(getClass.getClassLoader))
		tests.toList.map { path =>
			val testScript = new File(path)
			val scriptDirectory = testScript.getParentFile
			import Paths._
			FileUtilities.withTemporaryDirectory { base =>
				FileUtilities.copy( (scriptDirectory ***) x FileMapper.rebase(scriptDirectory, base) )
				(path, runScript(path, testScript, base, loader))
			}
		}
	}
	def runScript(path: String, scriptFile: File, baseDirectory: File, loader: ClassLoader): Boolean =
	{
		val run = new ScriptRunner
		val fileHandler = new FileCommands(baseDirectory)
		val compileHandler = new ManagerInterface(scalaHome, baseDirectory, true, loader)
		val parser = new TestScriptParser(Map('$' -> fileHandler, '>' -> compileHandler, '#' -> CommentHandler))
		try
		{
			run(parser.parse(scriptFile))
			System.out.println("+ " + path)
			true
		}
		catch {
			case e: Exception =>
				System.err.println("x " + path)
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
}
