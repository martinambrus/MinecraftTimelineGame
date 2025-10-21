package com.minecrafttimeline.core.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the {@link Logger} utility.
 */
class LoggerTest {

    private PrintStream originalOut;
    private PrintStream originalErr;
    private ByteArrayOutputStream capturedOut;
    private ByteArrayOutputStream capturedErr;

    @BeforeEach
    void setUp() {
        originalOut = System.out;
        originalErr = System.err;
        capturedOut = new ByteArrayOutputStream();
        capturedErr = new ByteArrayOutputStream();
        System.setOut(new PrintStream(capturedOut));
        System.setErr(new PrintStream(capturedErr));
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
        System.setErr(originalErr);
    }

    @Test
    void infoLogsToStandardOutput() {
        Logger.info("Sample info message");
        assertThat(capturedOut.toString()).contains("[INFO] Sample info message");
    }

    @Test
    void errorLogsToErrorOutput() {
        Logger.error("Serious error");
        assertThat(capturedErr.toString()).contains("[ERROR] Serious error");
    }

    @Test
    void logMessagesIncludeTimestamp() {
        Logger.info("Timestamp check");
        final String output = capturedOut.toString();
        assertThat(output).startsWith("[");
        assertThat(output).contains(" [INFO] Timestamp check");
    }
}
