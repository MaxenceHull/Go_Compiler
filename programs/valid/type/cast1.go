package super;

func main(a int) int {
	var b int = 3
	var c float64 = 5.0
	c = float64(b)
	b = int(c)
	type num int
	var d num = 88;
	b = int(d);
	d = num(b);
	return b;
}