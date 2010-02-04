
object D
{
	val c = new C
	def main(args: Array[String])
	{
		val correct = c.x == 1 && c.y == 2 && c.z == 3
		if(!correct)
		{
			println("Incorrect value(s). Actual values were:")
			println("x: " + c.x)
			println("y: " + c.y)
			println("z: " + c.z)
			error("Incorrect value(s)")
		}
	}
}