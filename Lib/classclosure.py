def f(x):
    class Foo():
        @classmethod
        def x(cls):
            print(__class__)
            print(cls)

        @staticmethod
        def y(x):
            print(x)
            print(__class__)

        def bar(self):
            print(x)
            print(__class__)
    return Foo

def xxxxxxxx():
    f(1000)().bar()
    f(1).y(1)
    f(1).x()
