package com.minecrafttimeline.util;

import static org.assertj.core.api.Assertions.assertThat;

import com.minecrafttimeline.core.util.Logger;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for the {@link Logger} utility.
 */
public class LoggerTest {

    private PrintStream originalOut;
    private PrintStream originalErr;
    private ByteArrayOutputStream capturedOut;
    private ByteArrayOutputStream capturedErr;

    /**
     * Prepares custom streams so log output can be inspected.
     */
    @Before
    public void setUp() {
        originalOut = System.out;
        originalErr = System.err;
        capturedOut = new ByteArrayOutputStream();
        capturedErr = new ByteArrayOutputStream();
        System.setOut(new PrintStream(capturedOut));
        System.setErr(new PrintStream(capturedErr));
    }

    /**
     * Restores the original streams once a test completes.
     */
    @After
    public void tearDown() {
        System.setOut(originalOut);
        System.setErr(originalErr);
    }

    /**
     * Verifies that informational messages are written to the standard output stream.
     */
    @Test
    public void infoLogsToStandardOutput() {
        Logger.info("Sample info message");
        assertThat(capturedOut.toString()).contains("[INFO] Sample info message");
    }

    /**
     * Verifies that error messages are written to the error output stream.
     */
    @Test
    public void errorLogsToErrorOutput() {
        Logger.error("Serious error");
        assertThat(capturedErr.toString()).contains("[ERROR] Serious error");
    }

    /**
     * Ensures that timestamps are prepended to each log entry.
     */
    @Test
    public void logMessagesIncludeTimestamp() {
        Logger.info("Timestamp check");
        final String output = capturedOut.toString();
        assertThat(output).startsWith("[");
        assertThat(output).contains(" [INFO] Timestamp check");
    }
}
