package main
func main(){
  var x = 3
  switch {
    case x > 5:
        var y = 2
        print(y)
    case x > 3, x > 4:
        var z = 12
        print(z)
    default:
        print(x)
  }

  switch x {
  case 1:
    print(1)
  case 2:
    print(2)
  default:
    print("default")
  }
}
