package xsbt.deptest

final class TestException(msg: String, exception: Throwable) extends RuntimeException(msg, exception)
{
	def this(msg: String) = this(msg, null)
}