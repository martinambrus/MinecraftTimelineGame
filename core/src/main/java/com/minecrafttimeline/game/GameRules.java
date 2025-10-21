package com.minecrafttimeline.game;

import com.minecrafttimeline.cards.Card;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

/**
 * Encapsulates the validation logic for card placement in Minecraft Timeline.
 * <p>
 * The rules are intentionally deterministic: validation depends solely on the chronological order of
 * the cards currently placed on the timeline. The engine reads data from the associated
 * {@link GameState} but never mutates it.
 */
public final class GameRules {

    private GameState gameState;

    /**
     * Creates a rules engine bound to an initial {@link GameState}.
     *
     * @param initialState the starting state
     */
    public GameRules(final GameState initialState) {
        this.gameState = Objects.requireNonNull(initialState, "initialState");
    }

    /**
     * Updates the internal reference to the authoritative {@link GameState}.
     *
     * @param state the latest state snapshot
     */
    public void updateState(final GameState state) {
        this.gameState = Objects.requireNonNull(state, "state");
    }

    /**
     * Validates whether the provided {@link Card} can be inserted at the specified position within
     * the current timeline.
     *
     * @param card     the card to place
     * @param position the target position within the timeline (0 based index)
     * @return {@code true} when the card can legally occupy the slot, otherwise {@code false}
     */
    public boolean validateCardPlacement(final Card card, final int position) {
        Objects.requireNonNull(card, "card");
        final List<Card> timeline = gameState.getTimeline();
        if (position < 0 || position > timeline.size()) {
            return false;
        }
        if (timeline.contains(card)) {
            return false;
        }
        if (timeline.isEmpty()) {
            return position == 0;
        }
        final LocalDate cardDate = card.date();
        if (position == 0) {
            return !cardDate.isAfter(timeline.get(0).date());
        }
        if (position == timeline.size()) {
            return !cardDate.isBefore(timeline.get(timeline.size() - 1).date());
        }
        final LocalDate before = timeline.get(position - 1).date();
        final LocalDate after = timeline.get(position).date();
        return !cardDate.isBefore(before) && !cardDate.isAfter(after);
    }

    /**
     * Computes the list of valid insertion points for the provided {@link Card} using the current
     * timeline.
     *
     * @param card the card to evaluate
     * @return an immutable list containing the zero-based indices at which the card can be placed
     */
    public List<Integer> getValidPlacementsForCard(final Card card) {
        Objects.requireNonNull(card, "card");
        final List<Integer> validPositions = new ArrayList<>();
        for (int position = 0; position <= gameState.getTimeline().size(); position++) {
            if (validateCardPlacement(card, position)) {
                validPositions.add(position);
            }
        }
        return Collections.unmodifiableList(validPositions);
    }

    /**
     * Determines whether the current state represents a winning configuration.
     *
     * @return {@code true} when all player hands are empty, otherwise {@code false}
     */
    public boolean hasPlayerWon() {
        return hasPlayerWon(gameState);
    }

    /**
     * Predicts whether the supplied {@link GameState} represents a completed match.
     *
     * @param state the state to evaluate
     * @return {@code true} when all player hands are empty, otherwise {@code false}
     */
    public boolean hasPlayerWon(final GameState state) {
        Objects.requireNonNull(state, "state");
        return state.getHands().values().stream().allMatch(List::isEmpty);
    }

    /**
     * Calculates the correct chronological position for the supplied {@link Card} relative to the
     * current timeline.
     *
     * @param card the card to evaluate
     * @return the index where the card should be inserted to maintain chronological order
     */
    public int getCorrectPosition(final Card card) {
        Objects.requireNonNull(card, "card");
        final List<Card> timeline = gameState.getTimeline();
        if (timeline.isEmpty()) {
            return 0;
        }
        final LocalDate cardDate = card.date();
        return (int) IntStream.range(0, timeline.size())
                .filter(i -> !timeline.get(i).date().isBefore(cardDate))
                .findFirst()
                .orElse(timeline.size());
    }
}
