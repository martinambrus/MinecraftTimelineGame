package com.minecrafttimeline.core.card;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TriviaDatabaseLoaderTest {

    @TempDir
    Path tempDir;

    @Test
    void loadFromJsonParsesValidFile() throws IOException {
        final Path file = createTriviaFile("valid.json", "2011-11-18");
        final List<Card> cards = TriviaDatabaseLoader.loadFromJson(file.toString());
        assertThat(cards).hasSize(1);
        final Card card = cards.get(0);
        assertThat(card.getId()).isEqualTo("test");
        assertThat(card.getDate()).isEqualTo(LocalDate.parse("2011-11-18"));
    }

    @Test
    void loadFromJsonThrowsForInvalidDate() throws IOException {
        final Path file = createTriviaFile("invalid-date.json", "2011/11/18");
        assertThatThrownBy(() -> TriviaDatabaseLoader.loadFromJson(file.toString()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid date format");
    }

    @Test
    void loadFromJsonThrowsForMissingField() throws IOException {
        final String content = "[{" +
                "\"id\":\"missing\"," +
                "\"title\":\"Title\"," +
                "\"date\":\"2011-11-18\"," +
                "\"trivia\":\"Trivia\"," +
                "\"version\":\"1.0\"}]";
        final Path file = tempDir.resolve("missing-field.json");
        Files.writeString(file, content);
        assertThatThrownBy(() -> TriviaDatabaseLoader.loadFromJson(file.toString()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Missing required field 'imageAssetPath'");
    }

    @Test
    void loadFromJsonThrowsWhenFileMissing() {
        assertThatThrownBy(() -> TriviaDatabaseLoader.loadFromJson(tempDir.resolve("nope.json").toString()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void loadFromJsonProducesCorrectCardCount() throws IOException {
        final Path file = tempDir.resolve("multi.json");
        final String content = "[" +
                createCardJson("a", "2010-01-01") + "," +
                createCardJson("b", "2010-01-02") + "]";
        Files.writeString(file, content);
        final List<Card> cards = TriviaDatabaseLoader.loadFromJson(file.toString());
        assertThat(cards).hasSize(2);
        assertThat(cards).extracting(Card::getId).containsExactlyInAnyOrder("a", "b");
    }

    private Path createTriviaFile(final String fileName, final String date) throws IOException {
        final Path file = tempDir.resolve(fileName);
        Files.writeString(file, "[" + createCardJson("test", date) + "]");
        return file;
    }

    private String createCardJson(final String id, final String date) {
        return "{" +
                "\"id\":\"" + id + "\"," +
                "\"title\":\"Title\"," +
                "\"date\":\"" + date + "\"," +
                "\"trivia\":\"Trivia\"," +
                "\"imageAssetPath\":\"images/cards/" + id + ".png\"," +
                "\"version\":\"1.0\"" +
                "}";
    }
}
