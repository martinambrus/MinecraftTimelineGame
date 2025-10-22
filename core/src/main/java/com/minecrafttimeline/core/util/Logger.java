package com.minecrafttimeline.core.util;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
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

    /**
     * Logs an error message along with a stack trace for the provided {@link Throwable}.
     *
     * @param message the message to log, {@code null} becomes an empty string
     * @param throwable optional exception whose stack trace should accompany the log
     */
    public static void error(final String message, final Throwable throwable) {
        final String stackTrace = throwable == null ? "" : System.lineSeparator() + toStackTrace(throwable);
        writeLog("ERROR", (message == null ? "" : message) + stackTrace, System.err);
    }

    private static void writeLog(final String level, final String message, final PrintStream stream) {
        final String safeMessage = message == null ? "" : message;
        final String timestamp = new SimpleDateFormat(TIMESTAMP_PATTERN).format(new Date());
        synchronized (LOCK) {
            stream.println(String.format("[%s] [%s] %s", timestamp, level, safeMessage));
            stream.flush();
        }
    }

    private static String toStackTrace(final Throwable throwable) {
        final StringWriter stringWriter = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(stringWriter);
        throwable.printStackTrace(printWriter);
        printWriter.flush();
        return stringWriter.toString();
    }
}
