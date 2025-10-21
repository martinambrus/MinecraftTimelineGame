package com.minecrafttimeline.core.card;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Singleton responsible for loading and providing access to {@link Card} instances.
 */
public final class CardManager {

    private static final CardManager INSTANCE = new CardManager();

    private final Lock lock = new ReentrantLock();
    private volatile boolean initialized;
    private List<Card> cards = List.of();
    private Map<String, Card> cardsById = Map.of();

    private CardManager() {
        // Singleton
    }

    /**
     * Retrieves the single {@link CardManager} instance.
     *
     * @return global manager instance
     */
    public static CardManager getInstance() {
        return INSTANCE;
    }

    /**
     * Initializes the manager by loading cards from the specified trivia database.
     * Subsequent calls replace the currently loaded data.
     *
     * @param triviaDatabasePath path to the trivia database JSON file; must not be {@code null}
     */
    public void initialize(final String triviaDatabasePath) {
        Objects.requireNonNull(triviaDatabasePath, "triviaDatabasePath must not be null");

        final List<Card> loadedCards = TriviaDatabaseLoader.loadFromJson(triviaDatabasePath);

        lock.lock();
        try {
            cards = List.copyOf(loadedCards);
            final Map<String, Card> map = new HashMap<>();
            for (Card card : cards) {
                map.put(card.getId(), card);
            }
            cardsById = Collections.unmodifiableMap(map);
            initialized = true;
        } finally {
            lock.unlock();
        }
    }

    private void ensureInitialized() {
        if (!initialized) {
            throw new IllegalStateException("CardManager has not been initialized. Call initialize() first.");
        }
    }

    /**
     * Retrieves a card by its identifier.
     *
     * @param id identifier of the card; must not be {@code null}
     * @return matching card
     * @throws IllegalArgumentException if no card with the provided identifier exists
     */
    public Card getCardById(final String id) {
        Objects.requireNonNull(id, "id must not be null");
        ensureInitialized();
        final Card card = cardsById.get(id);
        if (card == null) {
            throw new IllegalArgumentException("No card found with id: " + id);
        }
        return card;
    }

    /**
     * Provides an immutable view of all loaded cards.
     *
     * @return immutable list of cards
     */
    public List<Card> getAllCards() {
        ensureInitialized();
        return cards;
    }

    /**
     * Validates the integrity of the loaded card database.
     *
     * @return {@code true} if the database is consistent; {@code false} otherwise
     */
    public boolean validate() {
        ensureInitialized();
        lock.lock();
        try {
            if (cards.isEmpty()) {
                return false;
            }
            if (cards.size() != cardsById.size()) {
                return false;
            }
            for (Card card : cards) {
                if (!cardsById.containsKey(card.getId())) {
                    return false;
                }
            }
            return true;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Returns the total number of cards currently managed.
     *
     * @return total card count
     */
    public int getTotalCardCount() {
        ensureInitialized();
        return cards.size();
    }
}
