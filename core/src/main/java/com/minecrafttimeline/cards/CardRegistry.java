package com.minecrafttimeline.cards;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Placeholder registry that maintains a collection of card identifiers.
 */
public class CardRegistry {

    private final Set<String> registeredCards = new HashSet<>();

    /**
     * Registers a card identifier with the registry.
     *
     * @param cardId the identifier to store
     */
    public void registerCard(final String cardId) {
        if (cardId == null || cardId.isEmpty()) {
            return;
        }
        registeredCards.add(cardId);
    }

    /**
     * Provides a read-only view of the registered card identifiers.
     *
     * @return an immutable view of the registry contents
     */
    public Set<String> getRegisteredCards() {
        return Collections.unmodifiableSet(registeredCards);
    }

    /**
     * Clears all registered card identifiers.
     */
    public void clear() {
        registeredCards.clear();
    }
}
