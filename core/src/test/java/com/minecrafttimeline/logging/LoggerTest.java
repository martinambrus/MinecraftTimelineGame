package com.minecrafttimeline.logging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.minecrafttimeline.logging.Logger.LogLevel;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link Logger} verifying log routing and level handling.
 */
class LoggerTest {

    private ByteArrayOutputStream standardContent;
    private ByteArrayOutputStream errorContent;
    private PrintStream originalOut;
    private PrintStream originalErr;

    @BeforeEach
    void setUp() {
        standardContent = new ByteArrayOutputStream();
        errorContent = new ByteArrayOutputStream();
        originalOut = System.out;
        originalErr = System.err;
        Logger.setOutputStreams(new PrintStream(standardContent), new PrintStream(errorContent));
        Logger.setLogLevel(LogLevel.DEBUG);
    }

    @AfterEach
    void tearDown() {
        Logger.setOutputStreams(originalOut, originalErr);
        Logger.setLogLevel(LogLevel.INFO);
    }

    @Test
    void logsMessagesAtOrAboveCurrentLevel() {
        Logger.debug("Debug message");
        Logger.info("Info message");

        final String logOutput = standardContent.toString();
        assertThat(logOutput).contains("[DEBUG]").contains("Debug message");
        assertThat(logOutput).contains("[INFO]").contains("Info message");
    }

    @Test
    void respectsLogLevelThreshold() {
        Logger.setLogLevel(LogLevel.WARN);

        Logger.info("Info message suppressed");
        Logger.warn("Warn message");

        assertThat(standardContent.toString()).doesNotContain("Info message suppressed");
        assertThat(errorContent.toString()).contains("Warn message").contains("[WARN]");
    }

    @Test
    void directsWarningsToErrorStream() {
        final PrintStream standardMock = mock(PrintStream.class);
        final PrintStream errorMock = mock(PrintStream.class);
        Logger.setOutputStreams(standardMock, errorMock);

        Logger.warn("Stream routing");

        verify(errorMock).println(contains("[WARN]"));
        verifyNoInteractions(standardMock);
    }

    @Test
    void logsErrorsWithThrowable() {
        final RuntimeException runtimeException = new RuntimeException("Boom");
        Logger.error("Error occurred", runtimeException);

        final String errorOutput = errorContent.toString();
        assertThat(errorOutput).contains("[ERROR]").contains("Error occurred").contains("Boom");
    }
}
