package main
func main(){
  var a, b int = 1, 1
  //Assignment
  a, b = 2, 3
  if (a == 2) && (b == 3) {
    println("Test passed")
  } else {
    println("Test failed")
  }
  var c int = a
  if c == 2 {
    println("Test passed")
  } else {
    println("Test failed")
  }
  //Declaration
  type num int
  var d num
  type text string
  var e text
  type num_2 float64
  var f num_2
  var g bool = true
  var h rune

  //short Declaration
  i, j := "bonjour", 'a'
  if (i == "bonjour"){
    println("Test passed")
  } else {
    println("Test failed")
  }

  //Increment and decrement
  var k int
  k++
  if k == 1{
    println("Test passed")
  } else {
    println("Test failed")
  }

  //Print
  print("salut", 1, 1.0, 'n', true, a)
  println("salut", 1, 1.0, 'n', true, a)

  //if
  var cond bool = true
  if cond {
    print("OK !")
  } else if a == 2 {
    print("OK !")
  } else if true{
    print("OK !")
  } else {
    println("Test failed")
  }

}
