package com.minecrafttimeline;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.minecrafttimeline.core.util.Logger;
import com.minecrafttimeline.screens.BlackScreen;

/**
 * Main libGDX game entry point for the Minecraft Timeline card game.
 */
public class MinecraftTimelineGame extends Game {

    private static final int FPS_LOG_INTERVAL = 60;
    private int frameCounter;

    /** {@inheritDoc} */
    @Override
    public void create() {
        Logger.info("Game started");
        setScreen(new BlackScreen());
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
    }
}
