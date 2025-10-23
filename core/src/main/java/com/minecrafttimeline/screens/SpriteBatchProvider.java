package com.minecrafttimeline.screens;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * Provides access to a shared {@link SpriteBatch} instance for screens to reuse.
 */
public interface SpriteBatchProvider {

    /**
     * Retrieves the shared {@link SpriteBatch} used for rendering UI elements.
     *
     * @return sprite batch instance
     */
    SpriteBatch getSharedSpriteBatch();
}
