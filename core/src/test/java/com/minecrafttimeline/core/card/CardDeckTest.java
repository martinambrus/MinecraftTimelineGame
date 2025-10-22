package com.minecrafttimeline.core.card;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CardDeckTest {

    private List<Card> createCards(final int count) {
        final List<Card> cards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            cards.add(new Card(
                    "id_" + i,
                    "Title " + i,
                    LocalDate.of(2020, 1, i + 1),
                    "Trivia " + i,
                    "images/cards/" + i + ".png",
                    "1." + i));
        }
        return cards;
    }

    private CardDeck deck;

    @BeforeEach
    void setUp() {
        deck = new CardDeck(createCards(10));
        deck.setRandom(new Random(653L));
    }

    @Test
    void shuffleRandomizesOrder() {
        final List<Card> before = deck.snapshot();
        boolean changed = false;
        for (int i = 0; i < 5; i++) {
            deck.shuffle();
            if (!deck.snapshot().equals(before)) {
                changed = true;
                break;
            }
        }
        assertThat(changed).isTrue();
    }

    @Test
    void dealCardsRemovesFromDeck() {
        final List<Card> dealt = deck.dealCards(3);
        assertThat(dealt).hasSize(3);
        assertThat(deck.size()).isEqualTo(7);
        assertThat(deck.isEmpty()).isFalse();
    }

    @Test
    void dealCardsThrowsWhenCountTooLarge() {
        assertThatThrownBy(() -> deck.dealCards(11))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cannot deal 11 cards");
    }

    @Test
    void isEmptyReturnsTrueWhenDeckExhausted() {
        deck.dealCards(10);
        assertThat(deck.isEmpty()).isTrue();
    }

    @Test
    void resetRestoresDeck() {
        deck.dealCards(5);
        final List<Card> fresh = createCards(4);
        deck.reset(fresh);
        assertThat(deck.size()).isEqualTo(4);
        assertThat(deck.peek().getId()).isEqualTo("id_0");
    }

    @Test
    void peekReturnsNextCardWithoutRemoving() {
        final Card next = deck.peek();
        assertThat(next).isEqualTo(deck.snapshot().get(0));
        assertThat(deck.size()).isEqualTo(10);
    }

    @Test
    void peekThrowsWhenEmpty() {
        deck.dealCards(10);
        assertThatThrownBy(deck::peek)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("empty deck");
    }

    @Test
    void shuffleProducesUniformDistribution() {
        final int iterations = 2000;
        final List<Card> cards = createCards(5);
        final CardDeck localDeck = new CardDeck(cards);
        localDeck.setRandom(new Random(653L));
        final Map<String, Integer> firstPositionCounts = new HashMap<>();
        for (Card card : cards) {
            firstPositionCounts.put(card.getId(), 0);
        }

        for (int i = 0; i < iterations; i++) {
            localDeck.reset(cards);
            localDeck.shuffle();
            final Card first = localDeck.peek();
            firstPositionCounts.computeIfPresent(first.getId(), (k, v) -> v + 1);
        }

        final double expected = (double) iterations / cards.size();
        double chiSquare = 0.0;
        for (int count : firstPositionCounts.values()) {
            final double diff = count - expected;
            chiSquare += (diff * diff) / expected;
        }

        // For degrees of freedom 4, chi-square critical value at 0.05 significance is ~9.488
        assertThat(chiSquare).isLessThan(9.488);
    }
}
