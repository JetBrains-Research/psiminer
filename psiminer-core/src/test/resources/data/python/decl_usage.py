def simple():
    a = 5
    b = a + a
    c = a + b
    return c

def with_parameter(a):
    b = a + a
    return a + b

def with_if():
    a = 3
    b = 0
    if a > 4:
        b = 1
    elif a > 3:
        b = 2
    else:
        print(b)
    b += 1

def complex(param):
    a = 5
    for a in [param, 2, 3]:
        b = a + a
    print(a)
    a = 4
    return a * 2
