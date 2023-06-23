def factorial(number):
    if number < 0:
        return "bad"

    def inner_factorial(number):
        if number <= 1:
            return 1
        return number * inner_factorial(number - 1)

    return inner_factorial(number)


def triple_nested1():
    def triple_nested2():
        def triple_nested3():
            triple_nested1() + triple_nested2()

        return triple_nested3() + 1

    return triple_nested2() + triple_nested1()


def outside():
    return 42


def to_outside():
    return outside() + to_outside()
