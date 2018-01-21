package main
func main(){
  var a, b = 1, 1
  if a == b {
    print("Test passed")
  }
  c := 2
  if c == a {
    print("Test failed")
  }

  var d, e = 12.1 , 12.1
  if d == e {
    print("Test passed")
  }
  f := .12
  if f == e {
    print("Test failed")
  }

  var g, h = "Bonjour", "Bonjour"
  if g == h {
    print("Test passed")
  }
  i := "bonjour"
  if h == i {
    print("Test failed")
  }

  var k, j = 'a', 'a'
  l:= 'b'
  if k == j {
    print("Test passed")
  }
  if k == l {
    print("Test failed")
  }

  var m = true
  if m == m {
    print("Test passed")
  }
  var n = false
  if m == n {
    print("Test failed")
  }

  var q, r, t [2]float64
  q[0] = 1.2
  q[1] = 1.5
  r[0] = 1.2
  r[1] = 1.5
  if q == r {
    print("Test passed")
  }
  t[0] = 1.1
  t[1] = 1.5
  if q == t {
    print("Test failed")
  }

  type point struct {
    x,y int
  }
  var point_1 point
  point_1.x = 1
  point_1.y = 1
  var point_2 point
  point_2.x = 1
  point_2.y = 1
  if point_1 == point_2 {
    print("Test passed")
  }
  var point_3 point
  point_3.y = 2
  point_3.x = 2
  if point_3 == point_1 {
    print("Test failed")
  }
}
