package com.minecrafttimeline.models;

import com.minecrafttimeline.core.card.Card;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Represents a participant in the Minecraft Timeline game.
 */
public final class Player {

    private final String id;
    private final String name;
    private int score;
    private final List<Card> hand;

    /**
     * Creates a new player instance.
     *
     * @param id   unique identifier; must not be {@code null}
     * @param name display name; must not be {@code null}
     */
    public Player(final String id, final String name) {
        this(id, name, 0, List.of());
    }

    /**
     * Creates a player with a pre-populated score and hand. Primarily used for testing.
     *
     * @param id    player identifier; must not be {@code null}
     * @param name  player display name; must not be {@code null}
     * @param score initial score
     * @param hand  initial hand; must not be {@code null}
     */
    public Player(final String id, final String name, final int score, final List<Card> hand) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.name = Objects.requireNonNull(name, "name must not be null");
        this.score = score;
        this.hand = new ArrayList<>(Objects.requireNonNull(hand, "hand must not be null"));
    }

    /**
     * Returns the unique identifier of the player.
     *
     * @return player id
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the display name of the player.
     *
     * @return player name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the current score of the player.
     *
     * @return score value
     */
    public int getScore() {
        return score;
    }

    /**
     * Adds the specified delta to the player's score.
     *
     * @param delta value to add (may be negative)
     */
    public void addToScore(final int delta) {
        score += delta;
    }

    /**
     * Assigns a new absolute score. Intended for persistence and testing scenarios.
     *
     * @param newScore the score value to assign
     */
    public void setScore(final int newScore) {
        score = newScore;
    }

    /**
     * Returns an immutable view of the player's hand.
     *
     * @return immutable list of cards
     */
    public List<Card> getHand() {
        return Collections.unmodifiableList(hand);
    }

    /**
     * Adds a single card to the player's hand.
     *
     * @param card card to add; must not be {@code null}
     */
    public void addCardToHand(final Card card) {
        hand.add(Objects.requireNonNull(card, "card must not be null"));
    }

    /**
     * Adds all cards from the provided collection to the player's hand.
     *
     * @param cards cards to add; must not be {@code null}
     */
    public void addCardsToHand(final List<Card> cards) {
        Objects.requireNonNull(cards, "cards must not be null");
        cards.forEach(this::addCardToHand);
    }

    /**
     * Removes the specified card from the player's hand if present.
     *
     * @param card card to remove; must not be {@code null}
     * @return {@code true} if the card was removed, {@code false} otherwise
     */
    public boolean removeCardFromHand(final Card card) {
        return hand.remove(Objects.requireNonNull(card, "card must not be null"));
    }

    /**
     * Clears the player's hand.
     */
    public void clearHand() {
        hand.clear();
    }
}
