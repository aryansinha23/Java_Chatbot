package com.javamentor.ui;

/**
 * ANSI Color and Formatting Constants for rich Terminal CLI UX.
 */
public class AnsiTheme {

    public static final String RESET = "\u001B[0m";
    public static final String BOLD = "\u001B[1m";
    public static final String DIM = "\u001B[2m";

    // Colors
    public static final String BLACK = "\u001B[30m";
    public static final String RED = "\u001B[31m";
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String BLUE = "\u001B[34m";
    public static final String MAGENTA = "\u001B[35m";
    public static final String CYAN = "\u001B[36m";
    public static final String WHITE = "\u001B[37m";

    // Bright Colors
    public static final String BRIGHT_CYAN = "\u001B[96m";
    public static final String BRIGHT_GREEN = "\u001B[92m";
    public static final String BRIGHT_YELLOW = "\u001B[93m";
    public static final String BRIGHT_WHITE = "\u001B[97m";

    // Backgrounds
    public static final String BG_BLUE = "\u001B[44m";
    public static final String BG_DARK_GRAY = "\u001B[100m";

    private static boolean ansiSupported = true;

    static {
        // Check if console supports ANSI or if running on legacy dumb terminal
        String term = System.getenv("TERM");
        if ("dumb".equalsIgnoreCase(term)) {
            ansiSupported = false;
        }
    }

    public static void setAnsiSupported(boolean supported) {
        ansiSupported = supported;
    }

    public static String color(String code, String text) {
        if (!ansiSupported) return text;
        return code + text + RESET;
    }

    public static String bold(String text) {
        if (!ansiSupported) return text;
        return BOLD + text + RESET;
    }

    public static String dim(String text) {
        if (!ansiSupported) return text;
        return DIM + text + RESET;
    }

    public static String cyan(String text) {
        return color(CYAN, text);
    }

    public static String brightCyan(String text) {
        return color(BRIGHT_CYAN, text);
    }

    public static String green(String text) {
        return color(GREEN, text);
    }

    public static String brightGreen(String text) {
        return color(BRIGHT_GREEN, text);
    }

    public static String yellow(String text) {
        return color(YELLOW, text);
    }

    public static String brightYellow(String text) {
        return color(BRIGHT_YELLOW, text);
    }

    public static String red(String text) {
        return color(RED, text);
    }

    public static String magenta(String text) {
        return color(MAGENTA, text);
    }
}
