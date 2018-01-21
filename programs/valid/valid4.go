package main

var array [10]int

func bubble_sort(input [10]int) {
    n := 10
    swapped := true
    for swapped {
        swapped = false
        for i := 1; i < n-1; i++ {
            if input[i-1] > input[i] {
                print("Swapping")
                input[i], input[i-1] = input[i-1], input[i]
                swapped = true
            }
        }
    }
    println(input)
}


func main() {
    array[0], array[1], array[2] = 1, 5, 2
    array[3], array[4], array[5] = 9, 15,42
    array[6], array[7], array[8] = 1, 98,7
    array[9] = 8
    bubbleSort(array)

}
