package com.minecrafttimeline.core.card;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Represents a deck of {@link Card} instances with support for Fisher-Yates shuffling.
 * <p>
 * All mutating operations are guarded by an internal lock to provide basic thread-safety.
 */
public final class CardDeck {

    private final Lock lock = new ReentrantLock();
    private Random random = new Random();
    private final List<Card> cards;

    /**
     * Creates a new deck with the provided cards.
     *
     * @param cards collection of cards that will populate the deck; must not be {@code null}
     */
    public CardDeck(final List<Card> cards) {
        Objects.requireNonNull(cards, "cards must not be null");
        this.cards = new ArrayList<>(cards);
    }

    /**
     * Randomizes the deck order using the Fisher-Yates shuffle algorithm.
     */
    public void shuffle() {
        lock.lock();
        try {
            // Fisher-Yates algorithm: iterate from the end towards the beginning swapping elements
            for (int i = cards.size() - 1; i > 0; i--) {
                final int j = random.nextInt(i + 1);
                Collections.swap(cards, i, j);
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Deals the requested number of cards from the top of the deck.
     *
     * @param count number of cards to remove; must be non-negative and not exceed {@link #size()}
     * @return immutable list of dealt cards
     */
    public List<Card> dealCards(final int count) {
        if (count < 0) {
            throw new IllegalArgumentException("count must be non-negative");
        }

        lock.lock();
        try {
            if (count > cards.size()) {
                throw new IllegalArgumentException("Cannot deal " + count + " cards from deck of size " + cards.size());
            }
            final List<Card> dealt = new ArrayList<>(cards.subList(0, count));
            cards.subList(0, count).clear();
            return List.copyOf(dealt);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Restores the deck to contain the provided list of cards.
     *
     * @param newCards collection of cards to populate the deck; must not be {@code null}
     */
    public void reset(final List<Card> newCards) {
        Objects.requireNonNull(newCards, "newCards must not be null");
        lock.lock();
        try {
            cards.clear();
            cards.addAll(newCards);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Indicates whether the deck contains no cards.
     *
     * @return {@code true} if the deck is empty; {@code false} otherwise
     */
    public boolean isEmpty() {
        lock.lock();
        try {
            return cards.isEmpty();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Returns the current number of cards in the deck.
     *
     * @return deck size
     */
    public int size() {
        lock.lock();
        try {
            return cards.size();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Peeks at the next card to be dealt without removing it from the deck.
     *
     * @return the next card
     * @throws IllegalStateException if the deck is empty
     */
    public Card peek() {
        lock.lock();
        try {
            if (cards.isEmpty()) {
                throw new IllegalStateException("Cannot peek from an empty deck");
            }
            return cards.get(0);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Returns an immutable snapshot of the deck order. Primarily intended for testing.
     *
     * @return immutable list of cards currently in the deck
     */
    List<Card> snapshot() {
        lock.lock();
        try {
            return List.copyOf(cards);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Replaces the random source used for shuffling. Primarily intended for deterministic testing.
     *
     * @param randomSource random instance to use for subsequent shuffles; must not be {@code null}
     */
    void setRandom(final Random randomSource) {
        lock.lock();
        try {
            random = Objects.requireNonNull(randomSource, "randomSource must not be null");
        } finally {
            lock.unlock();
        }
    }
}
