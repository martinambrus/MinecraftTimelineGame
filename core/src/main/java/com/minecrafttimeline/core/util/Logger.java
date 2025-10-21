package com.minecrafttimeline.core.util;

import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Thread-safe logging helper that writes formatted messages to the standard streams.
 */
public final class Logger {

    private static final Object LOCK = new Object();
    private static final String TIMESTAMP_PATTERN = "yyyy-MM-dd HH:mm:ss.SSS";

    private Logger() {
        // Utility class
    }

    /**
     * Logs an informational message.
     *
     * @param message the message to log, {@code null} becomes an empty string
     */
    public static void info(final String message) {
        writeLog("INFO", message, System.out);
    }

    /**
     * Logs a debug-level message intended for verbose diagnostics.
     *
     * @param message the message to log, {@code null} becomes an empty string
     */
    public static void debug(final String message) {
        writeLog("DEBUG", message, System.out);
    }

    /**
     * Logs a warning message to highlight recoverable issues.
     *
     * @param message the message to log, {@code null} becomes an empty string
     */
    public static void warn(final String message) {
        writeLog("WARN", message, System.err);
    }

    /**
     * Logs an error message to highlight fatal or unrecoverable issues.
     *
     * @param message the message to log, {@code null} becomes an empty string
     */
    public static void error(final String message) {
        writeLog("ERROR", message, System.err);
    }

    private static void writeLog(final String level, final String message, final PrintStream stream) {
        final String safeMessage = message == null ? "" : message;
        final String timestamp = new SimpleDateFormat(TIMESTAMP_PATTERN).format(new Date());
        synchronized (LOCK) {
            stream.println(String.format("[%s] [%s] %s", timestamp, level, safeMessage));
            stream.flush();
        }
    }
}
