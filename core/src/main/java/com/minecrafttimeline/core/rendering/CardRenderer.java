package com.minecrafttimeline.core.rendering;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.minecrafttimeline.core.card.Card;
import com.minecrafttimeline.core.util.AssetLoader;
import com.minecrafttimeline.core.util.Logger;
import java.util.Objects;

/**
 * Responsible for drawing individual {@link Card} instances to the screen with batching-friendly operations.
 */
public class CardRenderer {

    private static final Vector2 DEBUG_SIZE = new Vector2();
    private static final Vector2 DEBUG_POSITION = new Vector2();

    private static boolean debugEnabled;

    private final Card card;
    private final Vector2 position;
    private final Vector2 size;
    private final Rectangle bounds;
    private final Sprite sprite;
    private final AssetLoader assetLoader;

    private float rotation;
    private float opacity;

    private final Vector2 center = new Vector2();

    /**
     * Creates a renderer for the supplied {@link Card} using the provided world coordinates.
     *
     * @param card   card reference; must not be {@code null}
     * @param x      world x-coordinate for the bottom-left corner
     * @param y      world y-coordinate for the bottom-left corner
     * @param width  width in world units
     * @param height height in world units
     */
    public CardRenderer(final Card card, final float x, final float y, final float width, final float height) {
        this(card, x, y, width, height, AssetLoader.getInstance());
    }

    CardRenderer(
            final Card card,
            final float x,
            final float y,
            final float width,
            final float height,
            final AssetLoader loader) {
        this.card = Objects.requireNonNull(card, "card must not be null");
        assetLoader = Objects.requireNonNull(loader, "loader must not be null");
        position = new Vector2(x, y);
        size = new Vector2(width, height);
        bounds = new Rectangle(x, y, width, height);
        rotation = 0f;
        opacity = 1f;
        sprite = assetLoader.getSprite(card.getImageAssetPath());
        sprite.setSize(width, height);
        sprite.setOrigin(width / 2f, height / 2f);
    }

    /**
     * Draws the card sprite to the provided {@link SpriteBatch}. No allocations occur within this method.
     *
     * @param batch shared batch used by the gameplay screen
     */
    public void render(final SpriteBatch batch) {
        if (batch == null) {
            Logger.error("SpriteBatch was null in CardRenderer.render");
            return;
        }
        if (card == null) {
            return;
        }
        // Position and size rely on bottom-left origin, matching libGDX world coordinates.
        sprite.setPosition(position.x, position.y);
        sprite.setSize(size.x, size.y);
        // Rotation occurs around the geometric center to keep drag interactions intuitive.
        sprite.setOrigin(size.x / 2f, size.y / 2f);
        sprite.setRotation(rotation);
        sprite.setColor(1f, 1f, 1f, opacity);
        sprite.draw(batch);
        if (debugEnabled) {
            renderDebugBounds(batch);
        }
    }

    private void renderDebugBounds(final SpriteBatch batch) {
        final Texture debugTexture = sprite.getTexture();
        if (debugTexture == null) {
            return;
        }
        final Color originalColor = batch.getColor();
        batch.setColor(1f, 0f, 0f, 0.25f);
        DEBUG_POSITION.set(position);
        DEBUG_SIZE.set(size.x, 2f);
        batch.draw(debugTexture, DEBUG_POSITION.x, DEBUG_POSITION.y, DEBUG_SIZE.x, DEBUG_SIZE.y);
        batch.draw(debugTexture, DEBUG_POSITION.x, DEBUG_POSITION.y + size.y - 2f, DEBUG_SIZE.x, DEBUG_SIZE.y);
        DEBUG_SIZE.set(2f, size.y);
        batch.draw(debugTexture, DEBUG_POSITION.x, DEBUG_POSITION.y, DEBUG_SIZE.x, DEBUG_SIZE.y);
        batch.draw(debugTexture, DEBUG_POSITION.x + size.x - 2f, DEBUG_POSITION.y, DEBUG_SIZE.x, DEBUG_SIZE.y);
        batch.setColor(originalColor);
    }

    /**
     * Updates the card's world position (bottom-left corner).
     *
     * @param x new world x-coordinate
     * @param y new world y-coordinate
     */
    public void setPosition(final float x, final float y) {
        position.set(x, y);
        bounds.setPosition(x, y);
    }

    /**
     * Retrieves the mutable position vector of the card (bottom-left corner).
     *
     * @return mutable position vector (do not cache outside rendering code)
     */
    public Vector2 getPosition() {
        return position;
    }

    /**
     * Updates the rendered size of the card.
     *
     * @param width  new width in world units
     * @param height new height in world units
     */
    public void setSize(final float width, final float height) {
        size.set(width, height);
        bounds.setSize(width, height);
        sprite.setSize(width, height);
        sprite.setOrigin(width / 2f, height / 2f);
    }

    /**
     * Retrieves the mutable size vector for the card.
     *
     * @return mutable size vector (width/height)
     */
    public Vector2 getSize() {
        return size;
    }

    /**
     * Updates the rotation of the card in degrees around its center.
     *
     * @param degrees rotation in degrees
     */
    public void setRotation(final float degrees) {
        rotation = degrees;
    }

    /**
     * Retrieves the currently stored rotation in degrees.
     *
     * @return rotation in degrees
     */
    public float getRotation() {
        return rotation;
    }

    /**
     * Adjusts the alpha modulation used during rendering.
     *
     * @param value opacity value from 0.0 to 1.0
     */
    public void setOpacity(final float value) {
        opacity = Math.max(0f, Math.min(1f, value));
    }

    /**
     * Retrieves the currently stored opacity factor.
     *
     * @return opacity factor ranging from 0.0 to 1.0
     */
    public float getOpacity() {
        return opacity;
    }

    /**
     * Computes the axis-aligned bounds used for hit-testing in world coordinates.
     *
     * @return mutable bounds rectangle
     */
    public Rectangle getBounds() {
        return bounds;
    }

    /**
     * Calculates the center point, storing it within a reusable vector to avoid allocations.
     *
     * @return mutable center vector
     */
    public Vector2 getCenter() {
        center.set(position.x + (size.x / 2f), position.y + (size.y / 2f));
        return center;
    }

    /**
     * Enables or disables debug rendering globally.
     *
     * @param enabled {@code true} to draw debug rectangles
     */
    public static void setDebugEnabled(final boolean enabled) {
        debugEnabled = enabled;
    }

    Card getCard() {
        return card;
    }
}
