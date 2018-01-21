package main;
func insertion_sort(A []int, size int) []int {
  for i:=1;i < size; i++ {
    var x = a[i];
    var j = i - 1;
    for {
      A[j+1] = A[j]
      j = j - 1
      if j < 0 && A[j] <= x {
        break;
      }
    }
    A[j+1] = x
  }
}

func main() {
  var list [4]int
	list[0] = 19
	list[1] = 5
	list[2] = 12
  list[3] = 13
	print ( insertion_sort(list, 4) )
}
