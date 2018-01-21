package main

func main(){
  var a struct {
    x,y int
    z float64
    w string
    isBool bool
    stamp rune
  }

  var b struct{
    x []int
    y [][]float64
  }

  type num int
  var c struct {
    x []num
    y [3]num
    z [2][4]num
    w [2][3][4]float64
  }
  a.x = 2

  type point struct {
      x, y int
  }
  var pt point
  var pt2 point
  if pt == pt2{
    print("Tests passed")
  } else {
    print("Test failed")
  }
  pt2.x = 3
  if pt != pt2 {
    print("Test passed")
  }else{
    print("Test failed")
  }


}
