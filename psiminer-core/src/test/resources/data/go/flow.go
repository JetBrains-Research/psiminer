package main

import "fmt"

func ifMethod() int {
	a, b := 0, 1
	if a > 0 {
		b = 2
	} else if a == 0 {
		b = 3
	} else {
		b = 4
	}
	b += 1
	return a + b
}

func gotoMethod() {
	i := 0
Start:
	fmt.Println(i)
	if i > 2 {
		goto End
	} else {
		i += 1
		goto Start
	}
End:
	i = 5
}

func recursive(x int) int {
	if x > 0 {
		return recursive(recursive(x - 1))
	} else if x > 0 {
		return recursive(x + 1)
	} else {
		return x
	}
}
