package com.minecrafttimeline.cards;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Random;

/**
 * Represents a deck of {@link Card} instances that can be shuffled and dealt.
 */
public class CardDeck {

    private final List<Card> originalCards;
    private final List<Card> workingDeck;

    /**
     * Creates a deck using the provided cards sorted chronologically and then by identifier.
     *
     * @param cards the collection of cards to include in the deck
     */
    public CardDeck(final List<Card> cards) {
        Objects.requireNonNull(cards, "cards");
        final List<Card> sorted = new ArrayList<>(cards);
        sorted.sort(Comparator
                .comparing(Card::date)
                .thenComparing(Card::id));
        this.originalCards = List.copyOf(sorted);
        this.workingDeck = new ArrayList<>(this.originalCards);
    }

    /**
     * Shuffles the deck using the Fisher-Yates algorithm with a new {@link Random} instance.
     */
    public void shuffle() {
        shuffle(new Random());
    }

    /**
     * Shuffles the deck using the Fisher-Yates algorithm and the provided {@link Random} source.
     *
     * @param random the random source used to perform the shuffle
     */
    public void shuffle(final Random random) {
        Objects.requireNonNull(random, "random");
        for (int i = workingDeck.size() - 1; i > 0; i--) {
            final int j = random.nextInt(i + 1);
            Collections.swap(workingDeck, i, j);
        }
    }

    /**
     * Deals the requested number of cards from the top of the deck.
     *
     * @param count the number of cards to deal
     * @return an immutable list containing the dealt cards
     */
    public List<Card> dealCards(final int count) {
        if (count < 0) {
            throw new IllegalArgumentException("count must not be negative");
        }
        if (count > workingDeck.size()) {
            throw new IllegalArgumentException("Not enough cards remaining to deal " + count + " cards");
        }
        final List<Card> dealt = new ArrayList<>(workingDeck.subList(0, count));
        workingDeck.subList(0, count).clear();
        return List.copyOf(dealt);
    }

    /**
     * Resets the deck to its original ordered state.
     */
    public void reset() {
        workingDeck.clear();
        workingDeck.addAll(originalCards);
    }

    /**
     * Indicates whether the deck is empty.
     *
     * @return {@code true} when no cards remain, otherwise {@code false}
     */
    public boolean isEmpty() {
        return workingDeck.isEmpty();
    }

    /**
     * Returns the number of cards remaining in the deck.
     *
     * @return the remaining card count
     */
    public int size() {
        return workingDeck.size();
    }

    /**
     * Provides an immutable view of the current deck order.
     *
     * @return an immutable list containing the cards in their current order
     */
    public List<Card> viewCards() {
        return List.copyOf(workingDeck);
    }
}
