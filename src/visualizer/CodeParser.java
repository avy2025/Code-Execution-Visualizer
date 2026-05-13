package visualizer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * CodeParser is responsible for accepting raw multi-line source code as a String,
 * cleaning it up, and returning a structured List of executable lines.
 *
 * Cleaning steps applied by parseCode():
 *  1. Split on any line ending (\r\n, \r, or \n)
 *  2. Strip leading/trailing whitespace from each line
 *  3. Collapse multiple internal spaces/tabs into a single space
 *  4. Remove single-line comments (// ...)
 *  5. Discard blank lines after all transformations
 */
public class CodeParser {

    // Stores the last parsed result for inspection
    private List<String> lastParsedLines;

    public CodeParser() {
        this.lastParsedLines = new ArrayList<>();
    }

    // -----------------------------------------------------------------------
    // Primary public API
    // -----------------------------------------------------------------------

    /**
     * Parses raw multi-line code into a clean list of executable lines.
     *
     * @param code  raw source code string
     * @param lang  target language (JAVA or PYTHON)
     * @return      immutable list of cleaned lines
     */
    public List<String> parseCode(String code, Language lang) {
        lastParsedLines = new ArrayList<>();

        if (code == null || code.isBlank()) {
            System.out.println("[CodeParser] No input provided.");
            return Collections.unmodifiableList(lastParsedLines);
        }

        // Step 1 – Split into individual lines (handles \r\n, \r, \n)
        String[] rawLines = code.split("\\r?\\n|\\r");

        int lineNumber = 0;
        for (String rawLine : rawLines) {
            lineNumber++;

            String cleaned = rawLine.strip();

            // Step 3 – Remove comments based on language
            if (lang == Language.PYTHON) {
                int commentIndex = cleaned.indexOf("#");
                if (commentIndex != -1) {
                    cleaned = cleaned.substring(0, commentIndex).strip();
                }
            } else {
                int commentIndex = cleaned.indexOf("//");
                if (commentIndex != -1) {
                    cleaned = cleaned.substring(0, commentIndex).strip();
                }
                
                // Java Boilerplate Stripping
                if (isBoilerplate(cleaned)) {
                    System.out.println("[CodeParser] Skipping Java boilerplate: " + cleaned);
                    continue;
                }
            }

            // Step 4 – Collapse spaces
            cleaned = cleaned.replaceAll("[ \\t]+", " ");

            // Step 5 – Skip if blank after all transformations
            if (cleaned.isEmpty()) {
                System.out.println("[CodeParser] Line " + lineNumber + " skipped (empty/comment).");
                continue;
            }

            lastParsedLines.add(cleaned);
            System.out.println("[CodeParser] Line " + lineNumber + " accepted: \"" + cleaned + "\"");
        }

        System.out.println("[CodeParser] Parsing complete. " + lastParsedLines.size() + " executable line(s) found.");
        return Collections.unmodifiableList(lastParsedLines);
    }

    /**
     * Returns a formatted summary of the last parsed result.
     *
     * @return multi-line summary string
     */
    public String getParseSummary() {
        if (lastParsedLines.isEmpty()) {
            return "No lines have been parsed yet.";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("=== Parse Summary (").append(lastParsedLines.size()).append(" lines) ===\n");
        for (int i = 0; i < lastParsedLines.size(); i++) {
            sb.append(String.format("  [%2d] %s%n", i + 1, lastParsedLines.get(i)));
        }
        return sb.toString();
    }

    // -----------------------------------------------------------------------
    // Getters / Setters (Encapsulation)
    // -----------------------------------------------------------------------

    /**
     * Returns the last list produced by parseCode(), or an empty list if
     * parseCode() has not been called yet.
     */
    public List<String> getLastParsedLines() {
        return Collections.unmodifiableList(lastParsedLines);
    }

    // -----------------------------------------------------------------------
    // Legacy method – delegates to parseCode() for backward compatibility
    // -----------------------------------------------------------------------

    private boolean isBoilerplate(String line) {
        return line.matches(".*(public|private|protected)?\\s*(class|interface|enum)\\s+\\w+.*") ||
               line.matches(".*public\\s+static\\s+void\\s+main\\s*\\(.*") ||
               line.matches("^package\\s+.*;$") ||
               line.matches("^import\\s+.*;$") ||
               line.equals("{") || line.equals("}"); 
    }

    /** @deprecated Use {@link #parseCode(String, Language)} instead. */
    @Deprecated
    public List<String> parse(String code) {
        return parseCode(code, Language.JAVA);
    }
}
