
object D
{
	val c = new B
	def main(args: Array[String])
	{
		val correct = c.x == 1 && c.y == 2
		if(!correct)
		{
			println("Incorrect value(s). Actual values were:")
			println("x: " + c.x)
			println("y: " + c.y)
			error("Incorrect value(s).")
		}
	}
}