package main
func main(){
  type num int
  var x num
  var y num
  type point struct {
    x, y int
    z float64
  }
  var w point
  a, y, c, d := -3*4, x, w, "bla"+"bla"
  var res string = d
  var res2 point = c
  var res3 num = y
  var res4 int = a
}
