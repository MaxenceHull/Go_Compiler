package main
func main(){
  var a, entier int
  var aa bool
  var ab = true
  var b = 0
  var c = 'a'
  var d = "bonjour"
  var e float64 = 0.3

  type num int
  var f, ff num
  type text string
  var g text

  var array, arr [3]int
  var array_2 [3][6]num
  var slice, slc []int
  var slice_2 [][]int

  type my_array [3]int
  var array_3, array_5 my_array
  var array_4 []my_array
  type my_array_2 [4][2][5]float64
  var array_6, array_7 my_array_2

  type my_slice []string
  var slice_3, slice_5 my_slice
  var slice_4 []my_slice

  var (
    var_1 int
    var_2 my_array_2
  )

}
