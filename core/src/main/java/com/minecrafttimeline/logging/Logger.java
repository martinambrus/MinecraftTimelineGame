package com.minecrafttimeline.logging;

import java.io.PrintStream;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;

/**
 * Thread-safe logging utility that provides leveled logging for the project.
 */
public final class Logger {

    /**
     * Enumeration representing the available logging levels ordered by priority.
     */
    public enum LogLevel {
        /** Log errors only. */
        ERROR,
        /** Log warnings and errors. */
        WARN,
        /** Log informational, warning, and error messages. */
        INFO,
        /** Log every message, including debug output. */
        DEBUG
    }

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_INSTANT;
    private static final Lock LOCK = new ReentrantLock();
    private static volatile LogLevel currentLevel = LogLevel.INFO;
    private static volatile PrintStream standardStream = System.out;
    private static volatile PrintStream errorStream = System.err;

    private Logger() {
        // Utility class.
    }

    /**
     * Configures the minimum {@link LogLevel} that will be emitted.
     *
     * @param logLevel the minimum level to log
     */
    public static void setLogLevel(final LogLevel logLevel) {
        currentLevel = Objects.requireNonNull(logLevel, "logLevel");
    }

    /**
     * Retrieves the active {@link LogLevel}.
     *
     * @return the currently configured level
     */
    public static LogLevel getLogLevel() {
        return currentLevel;
    }

    /**
     * Overrides the output streams used when writing log messages.
     *
     * @param standardOut the stream that receives INFO and DEBUG messages
     * @param errorOut    the stream that receives WARN and ERROR messages
     */
    public static void setOutputStreams(final PrintStream standardOut, final PrintStream errorOut) {
        standardStream = Objects.requireNonNull(standardOut, "standardOut");
        errorStream = Objects.requireNonNull(errorOut, "errorOut");
    }

    /**
     * Logs a debug-level message.
     *
     * @param message   the log message template
     * @param arguments optional template arguments
     */
    public static void debug(final String message, final Object... arguments) {
        log(LogLevel.DEBUG, message, null, arguments);
    }

    /**
     * Logs an informational message.
     *
     * @param message   the log message template
     * @param arguments optional template arguments
     */
    public static void info(final String message, final Object... arguments) {
        log(LogLevel.INFO, message, null, arguments);
    }

    /**
     * Logs a warning message.
     *
     * @param message   the log message template
     * @param arguments optional template arguments
     */
    public static void warn(final String message, final Object... arguments) {
        log(LogLevel.WARN, message, null, arguments);
    }

    /**
     * Logs an error message without a {@link Throwable}.
     *
     * @param message   the log message template
     * @param arguments optional template arguments
     */
    public static void error(final String message, final Object... arguments) {
        log(LogLevel.ERROR, message, null, arguments);
    }

    /**
     * Logs an error message accompanied by a {@link Throwable} cause.
     *
     * @param message   the log message template
     * @param throwable the associated exception
     * @param arguments optional template arguments
     */
    public static void error(final String message, final Throwable throwable, final Object... arguments) {
        log(LogLevel.ERROR, message, throwable, arguments);
    }

    private static void log(final LogLevel level, final String message, final Throwable throwable,
                            final Object... arguments) {
        Objects.requireNonNull(level, "level");
        if (level.ordinal() > currentLevel.ordinal()) {
            return;
        }
        final String formattedMessage = formatMessage(message, arguments);
        final String timestampedMessage = String.format("[%s] [%s] %s", DATE_FORMATTER.format(Instant.now()),
            level.name(), formattedMessage);
        LOCK.lock();
        try {
            final PrintStream targetStream = level.ordinal() <= LogLevel.WARN.ordinal() ? errorStream : standardStream;
            targetStream.println(timestampedMessage);
            if (throwable != null) {
                throwable.printStackTrace(targetStream);
            }
            targetStream.flush();
        } finally {
            LOCK.unlock();
        }
    }

    private static String formatMessage(final String message, final Object... arguments) {
        if (message == null) {
            return "";
        }
        if (arguments == null || arguments.length == 0) {
            return message;
        }
        String formatted = message;
        for (final Object argument : arguments) {
            final String replacement = Matcher.quoteReplacement(String.valueOf(argument));
            formatted = formatted.replaceFirst("\\{}", replacement);
        }
        return formatted;
    }
}
