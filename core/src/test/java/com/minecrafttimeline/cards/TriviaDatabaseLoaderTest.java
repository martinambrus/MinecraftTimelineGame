package com.minecrafttimeline.cards;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Unit tests for {@link TriviaDatabaseLoader}.
 */
class TriviaDatabaseLoaderTest {

    @TempDir
    Path tempDir;

    @Test
    void loadValidJsonProducesCards() throws IOException {
        Path json = tempDir.resolve("cards.json");
        Files.writeString(json, "[{" +
                "\"id\":\"card-1\"," +
                "\"title\":\"Title\"," +
                "\"date\":\"2020-01-01\"," +
                "\"trivia\":\"Trivia\"," +
                "\"imageAssetPath\":\"images/one.png\"," +
                "\"version\":\"1.0\"}]");

        TriviaDatabaseLoader loader = new TriviaDatabaseLoader();
        List<Card> cards = loader.load(json);

        assertThat(cards).hasSize(1);
        Card card = cards.get(0);
        assertThat(card.id()).isEqualTo("card-1");
        assertThat(card.date()).isEqualTo(LocalDate.of(2020, 1, 1));
    }

    @Test
    void missingFieldThrowsException() throws IOException {
        Path json = tempDir.resolve("missing.json");
        Files.writeString(json, "[{" +
                "\"id\":\"card-1\"," +
                "\"title\":\"Title\"}]");

        TriviaDatabaseLoader loader = new TriviaDatabaseLoader();
        assertThatThrownBy(() -> loader.load(json))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Missing required field");
    }

    @Test
    void invalidDateThrowsException() {
        TriviaDatabaseLoader loader = new TriviaDatabaseLoader();
        String json = "[{" +
                "\"id\":\"card-1\"," +
                "\"title\":\"Title\"," +
                "\"date\":\"bad-date\"," +
                "\"trivia\":\"Trivia\"," +
                "\"imageAssetPath\":\"images/one.png\"," +
                "\"version\":\"1.0\"}]";

        assertThatThrownBy(() -> loader.load(toStream(json)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid date format");
    }

    @Test
    void duplicateIdentifiersAreRejected() {
        TriviaDatabaseLoader loader = new TriviaDatabaseLoader();
        String json = "[{" +
                "\"id\":\"card-1\"," +
                "\"title\":\"Title\"," +
                "\"date\":\"2020-01-01\"," +
                "\"trivia\":\"Trivia\"," +
                "\"imageAssetPath\":\"images/one.png\"," +
                "\"version\":\"1.0\"},{" +
                "\"id\":\"card-1\"," +
                "\"title\":\"Another\"," +
                "\"date\":\"2021-01-01\"," +
                "\"trivia\":\"Trivia\"," +
                "\"imageAssetPath\":\"images/two.png\"," +
                "\"version\":\"1.1\"}]";

        assertThatThrownBy(() -> loader.load(toStream(json)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Duplicate card identifier");
    }

    private InputStream toStream(String json) {
        return new java.io.ByteArrayInputStream(json.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }
}
