package main

func main(){
  type num int
  type num_2 float64
  type num_3 string
  type num_4 bool
  type num_5 num
  type point struct {
    x,y,z int
    w float64
    name string
  }
  type (
    arr_1 []int
    arr_2 []float64
    arr_3 []bool
    arr_4 []string
    arr_5 [][]int
    arr_6 []point
    arr_7 [7]int
    point_2 point
  )
  {
    type block int
  }
}
