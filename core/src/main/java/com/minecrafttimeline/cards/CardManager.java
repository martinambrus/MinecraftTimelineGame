package com.minecrafttimeline.cards;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Singleton service that exposes access to all {@link Card} definitions.
 */
public final class CardManager {

    private static final String DEFAULT_RESOURCE = "/data/sample-trivia.json";
    private static final CardManager INSTANCE = new CardManager();

    private final List<Card> cards;
    private final Map<String, Card> cardsById;

    private CardManager() {
        this(loadDefaultCards());
    }

    CardManager(final List<Card> cards) {
        Objects.requireNonNull(cards, "cards");
        this.cards = List.copyOf(cards);
        this.cardsById = new ConcurrentHashMap<>();
        for (Card card : cards) {
            cardsById.put(card.id(), card);
        }
        validate();
    }

    private static List<Card> loadDefaultCards() {
        final TriviaDatabaseLoader loader = new TriviaDatabaseLoader();
        try (InputStream stream = CardManager.class.getResourceAsStream(DEFAULT_RESOURCE)) {
            if (stream == null) {
                throw new IllegalStateException("Unable to locate trivia database: " + DEFAULT_RESOURCE);
            }
            return loader.load(stream);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to load trivia database", exception);
        }
    }

    /**
     * Provides access to the shared {@link CardManager} instance.
     *
     * @return the singleton instance
     */
    public static CardManager getInstance() {
        return INSTANCE;
    }

    /**
     * Retrieves a card by its identifier.
     *
     * @param id the card identifier
     * @return an {@link Optional} containing the card when present
     */
    public Optional<Card> getCardById(final String id) {
        Objects.requireNonNull(id, "id");
        return Optional.ofNullable(cardsById.get(id));
    }

    /**
     * Provides all known cards in their stored order.
     *
     * @return an immutable list containing every card
     */
    public List<Card> getAllCards() {
        return Collections.unmodifiableList(cards);
    }

    /**
     * Validates the integrity of the loaded database.
     */
    public void validate() {
        if (cards.isEmpty()) {
            throw new IllegalStateException("Card database must contain at least one card");
        }
        if (cards.size() != cardsById.size()) {
            throw new IllegalStateException("Duplicate card identifiers detected in the database");
        }
        for (Card card : cards) {
            if (card.id().isBlank()) {
                throw new IllegalStateException("Card identifier must not be blank");
            }
            if (card.title().isBlank()) {
                throw new IllegalStateException("Card title must not be blank");
            }
            if (card.trivia().isBlank()) {
                throw new IllegalStateException("Card trivia must not be blank");
            }
            if (card.imageAssetPath().isBlank()) {
                throw new IllegalStateException("Card image asset path must not be blank");
            }
            if (card.minecraftVersion().isBlank()) {
                throw new IllegalStateException("Card Minecraft version must not be blank");
            }
        }
    }
}
