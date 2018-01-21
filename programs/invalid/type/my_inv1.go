//Using an incorrect type in a function call parameter list.
package main;
func main(a, b, z int, w string, x, y float64) int {
  return 1
}

func doom() {
  var xa string;
  var xb int;
  var xc float64;
  xb = main(xb, xb, xb, xa, xc, xa);
}
