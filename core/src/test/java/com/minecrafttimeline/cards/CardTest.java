package com.minecrafttimeline.cards;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link Card}.
 */
class CardTest {

    @Test
    void recordIsImmutable() throws Exception {
        assertThat(Modifier.isFinal(Card.class.getModifiers())).isTrue();
        for (Field field : Card.class.getDeclaredFields()) {
            if (field.isSynthetic()) {
                continue;
            }
            assertThat(Modifier.isFinal(field.getModifiers()))
                    .withFailMessage("Field %s should be final", field.getName())
                    .isTrue();
        }
    }

    @Test
    void gettersReturnExpectedValues() {
        Card card = new Card(
                "test-id",
                "Test Title",
                LocalDate.of(2020, 1, 1),
                "Some trivia",
                "images/test.png",
                "1.0");

        assertThat(card.id()).isEqualTo("test-id");
        assertThat(card.title()).isEqualTo("Test Title");
        assertThat(card.date()).isEqualTo(LocalDate.of(2020, 1, 1));
        assertThat(card.trivia()).isEqualTo("Some trivia");
        assertThat(card.imageAssetPath()).isEqualTo("images/test.png");
        assertThat(card.minecraftVersion()).isEqualTo("1.0");
        assertThat(card.toSerializableMap()).containsEntry("id", "test-id");
    }

    @Test
    void equalsAndHashCodeDependOnAllFields() {
        Card card1 = new Card(
                "test-id",
                "Test Title",
                LocalDate.of(2020, 1, 1),
                "Some trivia",
                "images/test.png",
                "1.0");
        Card card2 = new Card(
                "test-id",
                "Test Title",
                LocalDate.of(2020, 1, 1),
                "Some trivia",
                "images/test.png",
                "1.0");
        Card card3 = new Card(
                "test-id",
                "Different Title",
                LocalDate.of(2020, 1, 1),
                "Some trivia",
                "images/test.png",
                "1.0");

        assertThat(card1).isEqualTo(card2);
        assertThat(card1).hasSameHashCodeAs(card2);
        assertThat(card1).isNotEqualTo(card3);
    }
}
