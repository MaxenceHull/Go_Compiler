package com;
func main(int a){
  type num int;
  type (
    num int;
    blabla float64;
  )
  type point []int;
  type point [3]int;
  type (
    point []int;
    point [3] int;
  )
}
