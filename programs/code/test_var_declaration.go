package main

func main() {
  var a = 1
  var b = 2.5
  var e = "string"
  var f = 'a'
  var g int
  var h float64
  var i string
  var j = true
  var z = false
  var k bool
  var l, m int
  var n, o float64 = 32.5, 45.8
  var (
    p, q = 42, 43
    r = "yolo"
  )
  type num int
  var s num
  type point struct {
    x, y int
    z float64
  }
  var w point
  var x []int
  var y [][]float64
  var points []point
  var test [41]int
  var raw_string = `bonjour\n`

  type num2 num
  var s_2 num2


  t, u := 1, "bonjour"

  var float float64 = .12
  var float_2 float64 = 12.

}
