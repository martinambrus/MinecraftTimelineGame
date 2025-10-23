package com.minecrafttimeline.screens;

/**
 * Marks a {@link com.badlogic.gdx.Game} implementation that exposes a {@link ScreenManager}.
 */
public interface ScreenManagedGame {

    /**
     * Retrieves the global screen manager responsible for transitions and caching.
     *
     * @return screen manager instance
     */
    ScreenManager getScreenManager();
}
