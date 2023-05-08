def base():
    a = "Hello"

def multiple():
    a = b = 5.0

def parallel(c):
    a, b = c, 6*30

def augmented(a):
    a += 2
    a += a

def unpacking():
    (a, b) = ['H', 'e']
    [c, d] = (a, b)

def complex(n):
    a = [augmented(i) for i in range(n)]
