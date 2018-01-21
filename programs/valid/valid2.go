package main;
func numberofsmaller( input []int, size int, int value) []int {
	var numberofsmallerints int = 0
	for i := 0; i < size; i++ {
		if input[i] < value {
			numberofsmallerints += 1
		}
	}
	//return numberofsmallerints
}

func main () {
	var list [5]int
	list[0] = 2
	list[1] = 5
	list[2] = 12
	list[3] = 11
	list[4] = 3
	print ( numberofsmallerints(list, 5, 7) )
}
