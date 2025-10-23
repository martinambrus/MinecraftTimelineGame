package com.minecrafttimeline.core.game;

import com.minecrafttimeline.core.card.Card;

import java.util.Objects;

/**
 * Immutable representation of a single move performed during the game.
 */
public final class Move {

    private final Card card;
    private final int positionInTimeline;
    private final boolean correct;
    private final int playerIndex;
    private final long timestamp;

    /**
     * Creates a new immutable move instance.
     *
     * @param card              the card that was placed; must not be {@code null}
     * @param positionInTimeline zero-based position within the timeline where the card was placed
     * @param correct           whether the move was adjudicated as correct
     * @param playerIndex       index of the player who performed the move
     * @param timestamp         epoch millis when the move occurred
     */
    public Move(
            final Card card,
            final int positionInTimeline,
            final boolean correct,
            final int playerIndex,
            final long timestamp) {
        this.card = Objects.requireNonNull(card, "card must not be null");
        this.positionInTimeline = positionInTimeline;
        this.correct = correct;
        this.playerIndex = playerIndex;
        this.timestamp = timestamp;
    }

    /**
     * Returns the card associated with the move.
     *
     * @return the placed card
     */
    public Card getCard() {
        return card;
    }

    /**
     * Returns the position in the timeline where the card was placed.
     *
     * @return zero-based position
     */
    public int getPositionInTimeline() {
        return positionInTimeline;
    }

    /**
     * Indicates whether the move was judged as correct.
     *
     * @return {@code true} if correct, {@code false} otherwise
     */
    public boolean isCorrect() {
        return correct;
    }

    /**
     * Returns the index of the player who performed the move.
     *
     * @return player index
     */
    public int getPlayerIndex() {
        return playerIndex;
    }

    /**
     * Returns the timestamp representing when the move occurred.
     *
     * @return epoch millis timestamp
     */
    public long getTimestamp() {
        return timestamp;
    }
}
