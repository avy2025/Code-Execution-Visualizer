package visualizer;

import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("===========================================");
        System.out.println("         Code Execution Visualizer         ");
        System.out.println("===========================================");
        System.out.println("Enter your multi-line Java-like code.");
        System.out.println("(Type 'END' on a new line to finish):");
        System.out.println("-------------------------------------------");

        StringBuilder codeBuilder = new StringBuilder();
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            if ("END".equalsIgnoreCase(line.trim())) {
                break;
            }
            codeBuilder.append(line).append("\n");
        }

        String userCode = codeBuilder.toString();

        System.out.println("\n--- 1. Parsing Code ---");
        CodeParser parser = new CodeParser();
        List<String> parsedLines = parser.parseCode(userCode);  // Enhanced method

        System.out.println();
        System.out.print(parser.getParseSummary());             // Print clean summary

        System.out.println("\n--- 2. Executing Code ---");
        ExecutionEngine engine = new ExecutionEngine();
        engine.execute(parsedLines);

        scanner.close();
    }
}
