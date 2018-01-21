package main


func isPrime(n int) bool {

    for i := 2; i < n; i++ {
        if(n%i==0) {
            return false;
		}
    }
    return true;
}

func main(){
	var z []int;
	var size int;
	for i := 1; i < 250000; i++ {
		if isPrime(i) {
			z = append(z, i)
			size++
		}
	}
	
	print(size)
}


