package xsbt.deptest

import java.io.File
import xsbt.test.StatementHandler

class ManagerInterface(scalaHome: File, baseDirectory: File, refined: Boolean, loader: ClassLoader) extends StatementHandler
{
	type State = AnyRef
	def initialState = newManager(Nil)
	def newManager(args: List[String]): AnyRef = // BuildManager
	{
		val settings: AnyRef = settingsC.newInstance // new Settings
		val error = null // how to create an instance of String => Unit ?
		import Paths._
		val bootPath = bootDefault.invoke(settings) + File.pathSeparator + (scalaHome / "lib" / "scala-library.jar").getAbsolutePath
		val modifiedArgs = "-bootclasspath" :: bootPath :: args
		commandConstructor.newInstance(toList(modifiedArgs), settings, error, java.lang.Boolean.valueOf(false)) //new CompilerCommand(args, settings, settings.error, false)
		//if(refined) new RefinedBuildManager(settings) else new SimpleBuildManager(settings)
		if(refined) newRefinedC.newInstance(settings) else newSimpleC.newInstance(settings)
	}
	def apply(command: String, arguments: List[String], manager: AnyRef): AnyRef =
	{
		val newState =
		command match
		{
			case "init" => newManager(arguments.map( _.replace("$PWD", baseDirectory.getAbsolutePath) ))
			case "add" => addSourceFiles.invoke(manager, fromStrings(arguments)); manager
			case "remove" => removeFiles.invoke(manager, fromStrings(arguments)); manager
			case "update" =>
				arguments.break(_ == "-") match { case (add, remove) =>
					update.invoke(manager, fromStrings(add), fromStrings(remove.drop(1)) )
				}
				manager
		}
		// val reporter = manager.compiler.reporter
		val compiler = managerC.getMethod("compiler").invoke(manager)
		val reporter = compiler.getClass.getMethod("reporter").invoke(compiler)
		val reporterClass = reporter.getClass
		// val hasErrors = reporter.hasErrors
		val hasErrors = reporterClass.getMethod("hasErrors").invoke(reporter).asInstanceOf[Boolean]
		if(hasErrors) error(command + " failed")
		reporterClass.getMethod("reset").invoke(reporter) // reporter.reset
		newState
	}
	
	
	def c(name: String) = Class.forName(name, true, loader).asInstanceOf[Class[T forSome { type T <: AnyRef}]]
	val settingsC = c("scala.tools.nsc.Settings")
	val compilerCommandC = c("scala.tools.nsc.CompilerCommand")
	val refinedC = c("scala.tools.nsc.interactive.RefinedBuildManager")
	val simpleC = c("scala.tools.nsc.interactive.SimpleBuildManager")
	val fileC = c("scala.tools.nsc.io.AbstractFile")
	val fileO = getObject(fileC.getName)
	val listC = c("scala.collection.immutable.List")
	val errorC = c("scala.Function1")
	val managerC = c("scala.tools.nsc.interactive.BuildManager")

	val setC = c("scala.collection.Set")
	val arrayOpsRefC = c("scala.collection.mutable.ArrayOps$ofRef")
	val newArrayOpsRef = arrayOpsRefC.getConstructor(classOf[Array[AnyRef]])

	val bootDefault =
	{
		val m = settingsC.getMethod("bootclasspathDefault")
		m.setAccessible(true)
		m
	}
	val newRefinedC = refinedC.getConstructor(settingsC)
	val newSimpleC = simpleC.getConstructor(settingsC)
	val getFile = fileO.getClass.getMethod("getFile", classOf[String])
	val fromArray = listC.getMethod("fromArray", classOf[AnyRef])
	val addSourceFiles = managerC.getMethod("addSourceFiles", setC)
	val removeFiles = managerC.getMethod("removeFiles", setC)
	val update = managerC.getMethod("update", setC, setC)
	val commandConstructor = compilerCommandC.getConstructor(listC, settingsC, errorC, classOf[Boolean])

	def rawArray[T <: AnyRef](s: Iterable[T]): Array[AnyRef] = s.toSeq.toArray[AnyRef]
	def toSet[T <: AnyRef](s: Iterable[T]): AnyRef = //Set[T]
	{
		// Set() ++ s
		val ops = newArrayOpsRef.newInstance(rawArray(s))
		arrayOpsRefC.getMethod("toSet").invoke(ops)
	}
	def toList[T <: AnyRef](s: Seq[T]): AnyRef =
	{
		val a = s.toArray[AnyRef]
		fromArray.invoke(null, rawArray(s) ) // List.fromArray(s.toArray)
	}
	def fromStrings(paths: List[String]): AnyRef  = // Set[AbstractFile]
	{
		// Set() ++ paths.map(path => AbstractFile.getFile(new File(baseDirectory, path)))
		toSet(  paths.map(path => getFile.invoke(fileO,  filePath(path) ) )  )
	}
	def filePath(path: String) = new File(baseDirectory, path).getAbsolutePath

	def getObject(className: String) =
	{
		val obj = Class.forName(className + "$", true, loader)
		val singletonField = obj.getField("MODULE$")
		singletonField.get(null)
	}
}