package visualizer;

/**
 * Supported languages for the visualizer.
 */
public enum Language {
    JAVA("Java"),
    PYTHON("Python");

    private final String displayName;

    Language(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
