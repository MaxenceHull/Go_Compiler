package main
func main(){
  var x []int
  x = append(x, 1)
  x = append(x, 2)
  x = append(x, 3)

  var y []float64
  y = append(y, 12.3)

  var w []string
  w = append(w, "bla")
  w = append(w, `bonjour\n`)

  var z []bool
  z = append(z, true)
  z = append(z, false)

  var u []rune
  u = append(u, 'a')

  type num int
  var ex num
  var ex_1 []num
  ex_1 = append(ex_1, ex)

  var a int = x[1]
  var b float64 = y[0]
  var c string = w[0]
  var d bool = z[0]
  var e rune = u[0]
  var f num = ex_1[0]

}
