package com.minecrafttimeline;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.minecrafttimeline.core.util.AssetLoader;
import com.minecrafttimeline.core.util.Logger;
import com.minecrafttimeline.screens.ScreenManagedGame;
import com.minecrafttimeline.screens.ScreenManager;
import com.minecrafttimeline.screens.SpriteBatchProvider;

/**
 * Main libGDX game entry point for the Minecraft Timeline card game.
 */
public class MinecraftTimelineGame extends Game implements SpriteBatchProvider, ScreenManagedGame {

    private static final int FPS_LOG_INTERVAL = 60;
    private int frameCounter;
    private SpriteBatch spriteBatch;
    private ScreenManager screenManager;

    /** {@inheritDoc} */
    @Override
    public void create() {
        Logger.info("Game started");
        AssetLoader.getInstance().initialize();
        spriteBatch = new SpriteBatch();
        screenManager = new ScreenManager(this);
        screenManager.initialize();
        screenManager.showMainMenu();
    }

    /** {@inheritDoc} */
    @Override
    public void resize(final int width, final int height) {
        super.resize(width, height);
    }

    /** {@inheritDoc} */
    @Override
    public void render() {
        frameCounter++;
        if (frameCounter >= FPS_LOG_INTERVAL) {
            frameCounter = 0;
            Logger.info("FPS: " + Gdx.graphics.getFramesPerSecond());
        }
        super.render();
    }

    /** {@inheritDoc} */
    @Override
    public void dispose() {
        super.dispose();
        AssetLoader.getInstance().dispose();
        if (screenManager != null) {
            screenManager.dispose();
        }
        if (spriteBatch != null) {
            spriteBatch.dispose();
        }
    }

    @Override
    public SpriteBatch getSharedSpriteBatch() {
        return spriteBatch;
    }

    @Override
    public ScreenManager getScreenManager() {
        return screenManager;
    }
}
