package com.minecrafttimeline.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.minecrafttimeline.logging.Logger;

/**
 * Basic {@link ScreenAdapter} implementation that clears the frame buffer to black.
 */
public class BlackScreen extends ScreenAdapter {

    /**
     * Invoked when the screen becomes active, logging the state change.
     */
    @Override
    public void show() {
        Logger.info("BlackScreen active.");
    }

    /**
     * Clears the screen to black every frame.
     *
     * @param delta time elapsed since the last frame in seconds
     */
    @Override
    public void render(final float delta) {
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    }

    /**
     * Logs when the screen is disposed.
     */
    @Override
    public void dispose() {
        Logger.info("BlackScreen disposed.");
    }
}
