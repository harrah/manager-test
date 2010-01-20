package xsbt.deptest

import java.io.File
import java.net.URL

import xsbt.Paths._

object ScalaJars
{
	def apply(scalaHome: File): Either[String, ScalaJars] =
		try { Right(make(scalaHome)) }
		catch { case e: TestException => Left(e.getMessage) }

	private def make(scalaHome: File) =
	{
		exists(scalaHome, "Scala home directory")
		val lib = scalaHome / "lib"
		exists(lib, "Scala lib directory")
		val libraryJar = lib / "scala-library.jar"
		exists(libraryJar, "scala-library.jar")
		val compilerJar = lib / "scala-compiler.jar"
		exists(compilerJar, "scala-compiler.jar")
		val classpath = Array(libraryJar, compilerJar).map(_.toURI.toURL)
		new ScalaJars(scalaHome, lib, libraryJar, compilerJar, classpath)
	}
	def exists(file: File, label: String) = if(!file.exists) throw new TestException(label + " does not exist")
}
final case class ScalaJars(home: File, lib: File, libraryJar: File, compilerJar: File, classpath: Array[URL]) extends NotNull