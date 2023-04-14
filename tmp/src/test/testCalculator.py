import sys

from tmp.src.main.calculator import Calculator

try:
    if sys.version_info < (2, 7):
        import unittest2
    else:
        raise ImportError()
except ImportError:
    import unittest

class CalculatorTest(unittest.TestCase):
    def setUp(self):
        self.calc = Calculator("Calculator")

    def test_sum(self):
        value = self.calc.sum(4,5)
        self.assertEqual(value, 9)

    def test_mul(self):
        value = self.calc.mul(4,5)
        self.assertEqual(value, 20)

    def test_sub(self):
        value = self.calc.sub(4,2)
        self.assertEqual(value, 2)



if __name__ == '__main__':
    unittest.main()