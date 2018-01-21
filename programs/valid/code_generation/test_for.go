package main
func main(){
  var i = 0
  for {
    var c = "bla"
    print(c)
    if i == 10 {
      break
    }
    i++
  }
  var cond bool = true
  for cond {
    print("Test passed")
    cond = false
  }
  for i = 0; i < 3; i++ {
    print("yolo")
    if i == 2 {
      break
    }
  }
}
