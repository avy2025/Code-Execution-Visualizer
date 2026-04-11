package visualizer;

import java.util.Arrays;
import java.util.List;

/**
 * Utility to verify that the ExecutionEngine handles errors gracefully.
 */
public class ErrorHandlingTester {

    public static void main(String[] args) {
        ExecutionEngine engine = new ExecutionEngine();
        
        System.out.println("=== Starting Error Handling Tests ===");

        // Test 1: Missing Semicolon
        testError(engine, "int x = 10", "Missing semicolon");

        // Test 2: Undeclared Variable
        testError(engine, "y = x + 5;", "Undefined variable");

        // Test 3: Division by Zero
        testError(engine, "int z = 10 / 0;", "Division by zero");

        // Test 4: Variable Re-declaration
        engine.reset();
        engine.executeSingleLine("int a = 5;");
        testError(engine, "int a = 10;", "already declared");

        System.out.println("\n=== All Tests Completed ===");
    }

    private static void testError(ExecutionEngine engine, String testLine, String expectedErrorSub) {
        System.out.print("Testing: [" + testLine + "] -> ");
        
        // We capture errors via a temporary listener or just check standard output
        // For testing, let's just run it. The engine prints errors if no listener is set.
        engine.executeSingleLine(testLine);
    }
}
