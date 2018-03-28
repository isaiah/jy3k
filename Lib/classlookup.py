x = 1
class Foo():
    z = 3
    def __init__(self):
        self.y = 2

    def bar(self):
        print(x)
        print(self.x)
        print(self.z)

    @property
    def x(self):
        return self.y


if __name__ == '__main__':
    Foo().bar()
