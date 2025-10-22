package com.minecrafttimeline.core.card;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CardManagerTest {

    @TempDir
    Path tempDir;

    private Path databasePath;

    @BeforeEach
    void setUp() throws IOException {
        databasePath = tempDir.resolve("cards.json");
        final String json = "[" +
                createCardJson("first", "First Card") + "," +
                createCardJson("second", "Second Card") + "]";
        Files.writeString(databasePath, json);
        CardManager.getInstance().initialize(databasePath.toString());
    }

    @Test
    void getInstanceReturnsSingleton() {
        final CardManager first = CardManager.getInstance();
        final CardManager second = CardManager.getInstance();
        assertThat(first).isSameAs(second);
    }

    @Test
    void initializeLoadsAllCards() {
        assertThat(CardManager.getInstance().getTotalCardCount()).isEqualTo(2);
    }

    @Test
    void initializeFromFileHandleLoadsCards() {
        CardManager.getInstance().initialize(new com.badlogic.gdx.files.FileHandle(databasePath.toFile()));

        assertThat(CardManager.getInstance().getTotalCardCount()).isEqualTo(2);
    }

    @Test
    void getCardByIdReturnsCorrectCard() {
        final Card card = CardManager.getInstance().getCardById("first");
        assertThat(card.getTitle()).isEqualTo("First Card");
    }

    @Test
    void getCardByIdThrowsWhenIdUnknown() {
        assertThatThrownBy(() -> CardManager.getInstance().getCardById("missing"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("missing");
    }

    @Test
    void getAllCardsReturnsCompleteList() {
        final List<Card> cards = CardManager.getInstance().getAllCards();
        assertThat(cards).extracting(Card::getId).containsExactlyInAnyOrder("first", "second");
    }

    @Test
    void validateReturnsTrueForValidDatabase() {
        assertThat(CardManager.getInstance().validate()).isTrue();
    }

    private String createCardJson(final String id, final String title) {
        return "{" +
                "\"id\":\"" + id + "\"," +
                "\"title\":\"" + title + "\"," +
                "\"date\":\"2020-01-01\"," +
                "\"trivia\":\"Trivia\"," +
                "\"imageAssetPath\":\"images/cards/" + id + ".png\"," +
                "\"version\":\"1.0\"" +
                "}";
    }
}
