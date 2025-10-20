package com.minecrafttimeline.cards;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link CardDeck}.
 */
class CardDeckTest {

    @Test
    void shuffleUsesFisherYatesOrder() {
        List<Card> cards = buildCards(5);
        CardDeck deck = new CardDeck(cards);
        List<Integer> sequence = List.of(0, 0, 1, 1);
        DeterministicRandom random = new DeterministicRandom(sequence);

        List<Card> expected = new ArrayList<>(deck.viewCards());
        for (int i = expected.size() - 1, index = 0; i > 0; i--, index++) {
            int j = sequence.get(index);
            Card temp = expected.get(i);
            expected.set(i, expected.get(j));
            expected.set(j, temp);
        }

        deck.shuffle(random);

        assertThat(deck.viewCards()).containsExactlyElementsOf(expected);
    }

    @Test
    void dealCardsRemovesFromDeck() {
        List<Card> cards = buildCards(4);
        CardDeck deck = new CardDeck(cards);

        List<Card> dealt = deck.dealCards(2);
        assertThat(dealt).hasSize(2);
        assertThat(deck.size()).isEqualTo(2);
        assertThat(deck.isEmpty()).isFalse();
        assertThatThrownBy(() -> deck.dealCards(3)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void resetRestoresDeck() {
        List<Card> cards = buildCards(3);
        CardDeck deck = new CardDeck(cards);
        List<Card> expectedOrder = deck.viewCards();
        deck.shuffle(new Random(123));
        deck.dealCards(2);

        deck.reset();

        assertThat(deck.size()).isEqualTo(cards.size());
        assertThat(deck.viewCards()).containsExactlyElementsOf(expectedOrder);
    }

    @RepeatedTest(3)
    void shuffleDistributionAppearsUniform() {
        List<Card> cards = buildCards(3);
        CardDeck deck = new CardDeck(cards);
        int iterations = 5000;
        Map<String, int[]> positionCounts = new HashMap<>();
        for (Card card : cards) {
            positionCounts.put(card.id(), new int[cards.size()]);
        }

        Random random = new Random(42);
        for (int i = 0; i < iterations; i++) {
            deck.reset();
            deck.shuffle(random);
            List<Card> order = deck.viewCards();
            for (int pos = 0; pos < order.size(); pos++) {
                positionCounts.get(order.get(pos).id())[pos]++;
            }
        }

        double expected = iterations / (double) cards.size();
        double tolerance = expected * 0.15; // allow 15% deviation
        for (int[] counts : positionCounts.values()) {
            for (int count : counts) {
                assertThat(count).isBetween((int) Math.floor(expected - tolerance),
                        (int) Math.ceil(expected + tolerance));
            }
        }
    }

    private List<Card> buildCards(int count) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            cards.add(new Card(
                    "id-" + i,
                    "Title " + i,
                    LocalDate.of(2020, 1, 1).plusDays(i),
                    "Trivia " + i,
                    "images/image" + i + ".png",
                    "1." + i));
        }
        return cards;
    }

    private static final class DeterministicRandom extends Random {
        private final List<Integer> values;
        private int index;

        private DeterministicRandom(List<Integer> values) {
            this.values = values;
        }

        @Override
        public int nextInt(int bound) {
            if (index >= values.size()) {
                throw new IllegalStateException("No more predetermined values");
            }
            int value = values.get(index++);
            if (value < 0 || value >= bound) {
                throw new IllegalArgumentException("Value " + value + " outside of bound " + bound);
            }
            return value;
        }
    }
}
