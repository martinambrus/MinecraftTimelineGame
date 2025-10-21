package com.minecrafttimeline.game;

/**
 * Represents the high level progression of a Minecraft Timeline match.
 */
public enum GamePhase {
    /**
     * The session is distributing resources and preparing for play.
     */
    SETUP,
    /**
     * Waiting for the active player to perform an action.
     */
    PLAYER_TURN,
    /**
     * The game is checking whether a proposed placement obeys the rules.
     */
    VALIDATING,
    /**
     * The most recent placement was correct and has been applied.
     */
    CORRECT_PLACEMENT,
    /**
     * The most recent placement was incorrect and the player must try again.
     */
    INCORRECT_PLACEMENT,
    /**
     * All goals have been met and play has concluded.
     */
    GAME_OVER,
    /**
     * Final scores are being displayed to the players.
     */
    RESULTS
}
