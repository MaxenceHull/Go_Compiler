package main;

type test struct {
  x,y int
  z num
}

type test2 struct {
  x,y int
  z num
}

func main(){
  var x int = 3
  var y = 2
  var a test
  var b test2
  x, a = y, b

}
