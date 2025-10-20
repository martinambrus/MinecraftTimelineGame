package com.minecrafttimeline.state;

import java.util.Objects;

/**
 * Placeholder controller for the overall high-level game state.
 */
public class GameStateManager {

    private GamePhase currentPhase = GamePhase.INITIALISING;

    /**
     * Retrieves the current {@link GamePhase}.
     *
     * @return the active phase
     */
    public GamePhase getCurrentPhase() {
        return currentPhase;
    }

    /**
     * Sets the active {@link GamePhase}.
     *
     * @param phase the phase to apply
     */
    public void setCurrentPhase(final GamePhase phase) {
        currentPhase = Objects.requireNonNull(phase, "phase");
    }

    /**
     * Placeholder game phases representing the expected lifecycle of gameplay.
     */
    public enum GamePhase {
        /** Initial loading state. */
        INITIALISING,
        /** Active gameplay state. */
        RUNNING,
        /** Paused gameplay state. */
        PAUSED
    }
}
