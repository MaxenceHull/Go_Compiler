package main
func main(){
  var x [3]int
  x[0] = 1
  x[1] = 2
  x[2] = 3

  var y [1]float64
  y[0] = 12.3
  y[0] = 1.2
  y[0] = 5.6

  var w [2]string
  w[0] = "bla"
  w[1] = `bonjour\n`

  var z [2]bool
  z[0] = true
  z[1] = false

  var u [1]rune
  u[0] = 'a'

  type num int
  var ex num
  var ex_1 [1]num
  ex_1[0] = ex

  var multi [1][1]int
  multi[0][0] = 20

  var a int = x[1]
  var b float64 = y[0]
  var c string = w[0]
  var d bool = z[0]
  var e rune = u[0]
  var f num = ex_1[0]
  var g int = multi[0][0]

}
