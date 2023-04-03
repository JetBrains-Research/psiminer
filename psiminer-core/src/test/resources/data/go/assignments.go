package main

func simpleDeclarations() {
	var a = 0
	var b = a
	var c, d, e = b, b * 2, a + a
}

func constDeclarations() {
	const a = 0
	const b = a + a
}

func shortDeclarations() {
	a := 0
	b, c := a+1, a+2
	arr := [2]int{a, b}
}

func assignments() {
	var a, b, c = 0, 1, 2
	a = 10
	b, c = 20, a
	a += 10
}

func lambda() {
	f := func() int {
		i := 0
		return i + 1
	}
	f()
}

func withParameter(param int) {
	a := param
	b := a + param
}

func multipleDeclarations() int {
	a := 0
	{
		a := 1
		var b = a + 1
	}
	for a := 2; a < 5; a++ {
		b := a
	}
	return a
}
