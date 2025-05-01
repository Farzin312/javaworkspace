/*
    1) Create different operations using different functions
 */

public class Calculator {

    public static void main(String[] args) {
        if (args.length != 3) {
            System.out.println("Usage: java Calculator <num1> <operator> <num2>");
            return;
        }
        try {
            int a = Integer.parseInt(args[0]);
            String operator = args[1];
            int b = Integer.parseInt(args[2]);

            int result = 0;
            boolean valid = true;

            switch (operator) {
                case "+":
                    result = add(a, b);
                    break;
                case "-":
                    result = subtract(a, b);
                    break;
                case "*":
                    result = multiply(a, b);
                    break;
                case "/":
                    if (a == 0 || b == 0) {
                        System.out.println("Cannot divide by zero.");
                        return;
                    }
                    result = division(a, b);
                    break;
                default:
                    valid = false;
                    System.out.println("Unsupported operator" + operator);
            }
            if (valid) {
                System.out.println("Result: " + result);
            }

        } catch (NumberFormatException e) {
            System.out.println("Please enter valid number or operator.");
        }
    }

    private static int add(int a, int b) {
        return a + b;
    }

    private static int subtract(int a, int b) {
        return a - b;
    }

    private static int multiply(int a, int b) {
        return a * b;
    }

    private static int division(int a, int b) {
        return a / b;
    }
}
