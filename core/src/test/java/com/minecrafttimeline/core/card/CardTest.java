package com.minecrafttimeline.core.card;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

class CardTest {

    private Card createSampleCard() {
        return new Card(
                "test_id",
                "Test Title",
                LocalDate.of(2020, 1, 1),
                "Some trivia",
                "images/cards/test.png",
                "1.0");
    }

    @Test
    void constructorRejectsNullArguments() {
        assertThatThrownBy(() -> new Card(null, "Title", LocalDate.now(), "Trivia", "asset", "1.0"))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new Card("id", null, LocalDate.now(), "Trivia", "asset", "1.0"))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new Card("id", "Title", null, "Trivia", "asset", "1.0"))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new Card("id", "Title", LocalDate.now(), null, "asset", "1.0"))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new Card("id", "Title", LocalDate.now(), "Trivia", null, "1.0"))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new Card("id", "Title", LocalDate.now(), "Trivia", "asset", null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void gettersReturnExpectedValues() {
        final Card card = createSampleCard();
        assertThat(card.getId()).isEqualTo("test_id");
        assertThat(card.getTitle()).isEqualTo("Test Title");
        assertThat(card.getDate()).isEqualTo(LocalDate.of(2020, 1, 1));
        assertThat(card.getTrivia()).isEqualTo("Some trivia");
        assertThat(card.getImageAssetPath()).isEqualTo("images/cards/test.png");
        assertThat(card.getMinecraftVersion()).isEqualTo("1.0");
    }

    @Test
    void cardIsImmutable() {
        final Card card = createSampleCard();
        final LocalDate originalDate = card.getDate();
        final LocalDate mutatedDate = originalDate.plusDays(10);
        assertThat(card.getDate()).isEqualTo(LocalDate.of(2020, 1, 1));
        assertThat(mutatedDate).isNotEqualTo(card.getDate());
    }

    @Test
    void equalsAndHashCodeUseIdOnly() {
        final Card first = createSampleCard();
        final Card second = new Card(
                "test_id",
                "Another Title",
                LocalDate.of(1990, 5, 5),
                "Different trivia",
                "images/cards/another.png",
                "Beta");
        final Card different = new Card(
                "other_id",
                "Another Title",
                LocalDate.of(2020, 1, 1),
                "Some trivia",
                "images/cards/test.png",
                "1.0");

        assertThat(first).isEqualTo(second);
        assertThat(first.hashCode()).isEqualTo(second.hashCode());
        assertThat(first).isNotEqualTo(different);
    }

    @Test
    void toStringContainsHelpfulInformation() {
        final Card card = createSampleCard();
        assertThat(card.toString())
                .contains("test_id")
                .contains("Test Title")
                .contains("2020-01-01");
    }
}
