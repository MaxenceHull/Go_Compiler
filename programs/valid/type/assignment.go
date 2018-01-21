package main;

type num int
type array []float64
type test struct {
  x,y int
  z num
}

func main(){
  var x int = 3
  var y = 2
  y = x
  var a = "bla"
  var b string
  b, x = a, y
  var c num
  var d num
  var e array
  var f array
  var g test
  var h test
  c, e, g = d, f, h

}
