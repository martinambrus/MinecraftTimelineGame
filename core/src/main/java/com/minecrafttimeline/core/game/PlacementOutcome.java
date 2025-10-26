package com.minecrafttimeline.core.game;

/**
 * Represents the result of attempting to place a card on the timeline.
 */
public enum PlacementOutcome {

    /**
     * The card was placed at the exact chronological position.
     */
    CORRECT,

    /**
     * The card could be inserted without breaking chronology, but the chosen
     * slot was not the precise chronological position.
     */
    INCORRECT,

    /**
     * The card cannot be inserted at the requested index (e.g. out of bounds or
     * violating chronological order).
     */
    INVALID
}
