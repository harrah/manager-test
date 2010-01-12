trait A
{
	def x: Int =
	{
		var y = 0
		def x0 = { y = 3; 9 }
		class C { def x00 = y = x0 }
		(new C).x00
		y
	}
}
