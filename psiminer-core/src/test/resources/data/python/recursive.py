def sum(a, b):
    c = sum(a, 2)
    return a + sum(c, sum(b, 2))

def mult(a, b):
    return a * b