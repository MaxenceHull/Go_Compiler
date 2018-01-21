package main;

func main(){
  print();
  var x int;
  var y float64;
  var w string;
  var z rune;
  var b bool;
  print(x, y, w, x, z, b, 3.0, 2, "bla", 'r')
  println(x, y, w, x, z, b, 3.0, 2, "bla", 'r')

  type test int
  type test2 test
  var a test2
  print(a)
}
