"""
    Example of a calculator
"""
class Calculator:
    """
    Class that represent the Calculator model
    """
    def __init__(self, name):
        """
            Calculator constructor that takes the name in input
        """
        self.name = name

    def sum(self, num1, num2):
        """
            Method that calculate given two number num1 and num2
        """
        return num1 + num2

    def mul(self, num1, num2):
        """
           Method that calculate the multiplication by two numbers num1 and num2
        """
        return num1 * num2

    def sub(self, num1, num2):
        """
            Method that calculate the subtraction by two numbers num1 and num2
        """
        return num1 - num2

    def div(self, num1, num2):
        """
            Method that calculate the division by two numbers num1 and num2
        """
        return num1 / num2
