package com.minecrafttimeline;

import com.badlogic.gdx.Game;
import com.minecrafttimeline.logging.Logger;
import com.minecrafttimeline.logging.Logger.LogLevel;
import com.minecrafttimeline.screen.BlackScreen;

/**
 * Main libGDX {@link Game} implementation for the Minecraft Timeline card game.
 * <p>
 * The class bootstraps the logging system and presents a placeholder black
 * screen until gameplay systems are introduced.
 */
public class MinecraftTimelineGame extends Game {

    /**
     * Creates the game instance, configures logging, and sets the initial screen.
     */
    @Override
    public void create() {
        Logger.setLogLevel(LogLevel.INFO);
        Logger.info("MinecraftTimelineGame initialising.");
        setScreen(new BlackScreen());
    }

    /**
     * Delegates rendering to the active {@link com.badlogic.gdx.Screen}.
     */
    @Override
    public void render() {
        super.render();
    }

    /**
     * Handles window resizing events.
     *
     * @param width  the new window width in pixels
     * @param height the new window height in pixels
     */
    @Override
    public void resize(final int width, final int height) {
        super.resize(width, height);
        Logger.debug("Resized to {}x{} pixels.", width, height);
    }

    /**
     * Called when the game is paused.
     */
    @Override
    public void pause() {
        super.pause();
        Logger.info("Game paused.");
    }

    /**
     * Called when the game resumes from a paused state.
     */
    @Override
    public void resume() {
        super.resume();
        Logger.info("Game resumed.");
    }

    /**
     * Releases managed resources before shutdown.
     */
    @Override
    public void dispose() {
        Logger.info("Disposing MinecraftTimelineGame.");
        super.dispose();
        if (getScreen() != null) {
            getScreen().dispose();
        }
    }
}
