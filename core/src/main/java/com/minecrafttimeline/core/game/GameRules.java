package com.minecrafttimeline.core.game;

import com.minecrafttimeline.core.card.Card;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Collection of static helper methods that embody the timeline placement rules.
 * <p>
 * Cards are always arranged in chronological order (oldest to newest). The
 * rules engine therefore focuses on validating chronological placement,
 * reporting the most appropriate target position and computing auxiliary data
 * such as win conditions or game progress.
 */
public final class GameRules {

    private GameRules() {
    }

    /**
     * Validates whether the provided card can be inserted into the timeline at
     * the specified position without breaking chronological order.
     *
     * @param card              card to validate; must not be {@code null}
     * @param timeline          current timeline (sorted chronologically); must not be {@code null}
     * @param positionInTimeline desired insertion position (0..timeline size)
     * @return {@code true} if the placement maintains chronological ordering, {@code false} otherwise
     */
    public static boolean validateCardPlacement(
            final Card card,
            final List<Card> timeline,
            final int positionInTimeline) {
        Objects.requireNonNull(card, "card must not be null");
        Objects.requireNonNull(timeline, "timeline must not be null");
        if (positionInTimeline < 0 || positionInTimeline > timeline.size()) {
            return false;
        }
        if (timeline.isEmpty()) {
            return true;
        }
        // Chronological order is enforced by comparing the candidate card's date with its immediate neighbours.
        if (positionInTimeline == 0) {
            return card.getDate().compareTo(timeline.get(0).getDate()) <= 0;
        }
        if (positionInTimeline == timeline.size()) {
            return card.getDate().compareTo(timeline.get(timeline.size() - 1).getDate()) >= 0;
        }
        final Card left = timeline.get(positionInTimeline - 1);
        final Card right = timeline.get(positionInTimeline);
        return card.getDate().compareTo(left.getDate()) >= 0 && card.getDate().compareTo(right.getDate()) <= 0;
    }

    /**
     * Computes all valid timeline indices for the provided card.
     *
     * @param card     card to inspect; must not be {@code null}
     * @param timeline current timeline; must not be {@code null}
     * @return immutable list of indices that represent valid insertion points
     */
    public static List<Integer> getValidPositionsForCard(final Card card, final List<Card> timeline) {
        Objects.requireNonNull(card, "card must not be null");
        Objects.requireNonNull(timeline, "timeline must not be null");
        final List<Integer> validPositions = new ArrayList<>();
        for (int position = 0; position <= timeline.size(); position++) {
            if (validateCardPlacement(card, timeline, position)) {
                validPositions.add(position);
            }
        }
        return List.copyOf(validPositions);
    }

    /**
     * Calculates the index where the card should be inserted to maintain a
     * perfectly sorted timeline.
     *
     * @param card     card to evaluate; must not be {@code null}
     * @param timeline current timeline; must not be {@code null}
     * @return zero-based index indicating the correct position
     */
    public static int getCorrectPosition(final Card card, final List<Card> timeline) {
        Objects.requireNonNull(card, "card must not be null");
        Objects.requireNonNull(timeline, "timeline must not be null");
        if (timeline.isEmpty()) {
            return 0;
        }
        for (int index = 0; index < timeline.size(); index++) {
            final Card current = timeline.get(index);
            if (card.getDate().compareTo(current.getDate()) <= 0) {
                return index;
            }
        }
        return timeline.size();
    }

    /**
     * Determines whether the attempted placement is considered correct when
     * accounting for the tolerance zone.
     *
     * @param card               card being evaluated; must not be {@code null}
     * @param timeline           current timeline; must not be {@code null}
     * @param positionInTimeline attempted insertion index
     * @return {@code true} if the position is within ±1 of the ideal index, {@code false} otherwise
     */
    public static boolean isCorrectPlacement(
            final Card card,
            final List<Card> timeline,
            final int positionInTimeline) {
        Objects.requireNonNull(card, "card must not be null");
        Objects.requireNonNull(timeline, "timeline must not be null");
        final int correctPosition = getCorrectPosition(card, timeline);
        return Math.abs(correctPosition - positionInTimeline) <= 1;
    }

    /**
     * Checks whether the player has achieved the win condition of emptying
     * their hand with a chronologically ordered timeline.
     *
     * @param hand     active player's hand; must not be {@code null}
     * @param timeline current timeline; must not be {@code null}
     * @return {@code true} if the hand is empty and the timeline is chronologically sorted
     */
    public static boolean hasPlayerWon(final List<Card> hand, final List<Card> timeline) {
        Objects.requireNonNull(hand, "hand must not be null");
        Objects.requireNonNull(timeline, "timeline must not be null");
        return hand.isEmpty() && isTimelineChronological(timeline);
    }

    /**
     * Computes the completion ratio of the game.
     *
     * @param timeline   current timeline; must not be {@code null}
     * @param totalCards total number of cards that will be played in the game
     * @return floating point value between 0.0 and 1.0 (inclusive)
     */
    public static float getGameProgress(final List<Card> timeline, final int totalCards) {
        Objects.requireNonNull(timeline, "timeline must not be null");
        if (totalCards <= 0) {
            return timeline.isEmpty() ? 0f : 1f;
        }
        return Math.min(1f, timeline.size() / (float) totalCards);
    }

    private static boolean isTimelineChronological(final List<Card> timeline) {
        if (timeline.size() <= 1) {
            return true;
        }
        LocalDate previous = timeline.get(0).getDate();
        for (int i = 1; i < timeline.size(); i++) {
            final LocalDate current = timeline.get(i).getDate();
            if (previous.compareTo(current) > 0) {
                return false;
            }
            previous = current;
        }
        return true;
    }
}
