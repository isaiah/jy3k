def foo():
    yield 1


a = foo()
print(next(a))
print(next(a))
print(next(a))
