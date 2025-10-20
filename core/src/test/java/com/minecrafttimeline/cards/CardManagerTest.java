package com.minecrafttimeline.cards;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link CardManager}.
 */
class CardManagerTest {

    @Test
    void singletonReturnsSameInstance() {
        CardManager first = CardManager.getInstance();
        CardManager second = CardManager.getInstance();

        assertThat(first).isSameAs(second);
    }

    @Test
    void managerLoadsSampleData() {
        CardManager manager = CardManager.getInstance();

        assertThat(manager.getAllCards()).isNotEmpty();
        assertThat(manager.getCardById("release-1-0")).isPresent();
    }

    @Test
    void constructorValidatesInput() {
        Card card = new Card(
                "duplicate",
                "Title",
                LocalDate.of(2020, 1, 1),
                "Trivia",
                "images/sample.png",
                "1.0");

        assertThatThrownBy(() -> new CardManager(List.of(card, card)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Duplicate card identifiers");
    }

    @Test
    void validateDetectsBlankFields() {
        Card card = new Card(
                "id",
                "",
                LocalDate.of(2020, 1, 1),
                "Trivia",
                "images/sample.png",
                "1.0");

        assertThatThrownBy(() -> new CardManager(List.of(card)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Card title must not be blank");
    }
}
