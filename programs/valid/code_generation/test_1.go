package main

type num int
func main() {
  var a = "blal"
  var b = 17.3
  var c = 1
  var d = 'a'
  var z, w num
  z = z &^ w
  //c = function_1(1, 6.3, "bla")
  if 3 > c {
    c = c * 2
    c = c + 1
    c = c + 3
  } else if 3 < c {
    c = c * 3
  } else {
    a = "yolo"
    a = "blabal"
  }
}

func function_1 (a int, b float64, c string) int {
  var e, f = 1, 2
  var g bool
  e = 2
  e++
  f--
  e, f = 5, 6
  f = 3*5+2
  f = (3+1)*5
  f = -e
  f = -3
  print(a)
  println(a)
  print(a, b)
  println(a, b)
  return a
}
