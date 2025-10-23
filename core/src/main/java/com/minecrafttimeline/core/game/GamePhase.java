package com.minecrafttimeline.core.game;

/**
 * Represents the discrete phases a Minecraft Timeline game session can be in.
 * <p>
 * The phase communicates the high-level flow of the game to both the rules
 * engine and any user interface consuming the game state.
 */
public enum GamePhase {

    /**
     * The game is being initialised. Players are created, the deck is prepared
     * and cards are dealt to each player.
     */
    SETUP,

    /**
     * Waiting for the currently active player to choose an action.
     */
    PLAYER_TURN,

    /**
     * A move has been submitted and is currently being validated by the rules
     * engine.
     */
    VALIDATING,

    /**
     * The previously submitted move was correct. Feedback should be shown to
     * the player and scoring updates may occur.
     */
    CORRECT_PLACEMENT,

    /**
     * The previously submitted move was incorrect. Feedback should be shown and
     * corrective actions performed (e.g. returning the card to the player's
     * hand).
     */
    INCORRECT_PLACEMENT,

    /**
     * The game has concluded. No further interaction is permitted and the
     * winner can be determined.
     */
    GAME_OVER,

    /**
     * Post-game state where final scores and statistics are presented.
     */
    RESULTS
}
