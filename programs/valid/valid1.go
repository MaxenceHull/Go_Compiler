package main;
func max( input []int, size int) []int {
	var max int = 0
	for i := 0; i < size; i++ {
		if max < input[i] {
			max = input[i]
		}
	}
	return max
}

func main () {
	var list [6][][3]int
	list[0] = 2
	list[1] = 5
	list[2] = 12
	print ( max(list, 3) )
}
