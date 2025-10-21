package com.minecrafttimeline.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.minecrafttimeline.assets.AssetLoader;
import com.minecrafttimeline.cards.Card;
import java.util.Objects;

/**
 * Handles drawing and animated transitions for a single {@link Card} instance.
 */
public class CardRenderer {

    private static final float DEFAULT_INTERPOLATION_SPEED = 8f;

    private final AssetLoader assetLoader;
    private final Card card;

    private final Vector2 currentPosition = new Vector2();
    private final Vector2 currentSize = new Vector2(1f, 1f);
    private final Vector2 targetPosition = new Vector2();
    private final Vector2 targetSize = new Vector2(1f, 1f);

    private float currentRotation;
    private float targetRotation;
    private float currentOpacity = 1f;
    private float targetOpacity = 1f;
    private float interpolationSpeed = DEFAULT_INTERPOLATION_SPEED;
    private boolean debugEnabled;

    /**
     * Creates a renderer for the supplied {@link Card} using the shared {@link AssetLoader} instance.
     *
     * @param card the card to render
     */
    public CardRenderer(final Card card) {
        this(card, AssetLoader.getInstance());
    }

    /**
     * Creates a renderer for the supplied {@link Card} and {@link AssetLoader}.
     *
     * @param card        the card to render
     * @param assetLoader the loader providing graphical assets
     */
    public CardRenderer(final Card card, final AssetLoader assetLoader) {
        this.card = Objects.requireNonNull(card, "card");
        this.assetLoader = Objects.requireNonNull(assetLoader, "assetLoader");
    }

    /**
     * Updates the current state moving towards the target layout using interpolation.
     *
     * @param delta time elapsed since the previous frame in seconds
     */
    public void update(final float delta) {
        final float alpha = MathUtils.clamp(delta * interpolationSpeed, 0f, 1f);
        currentPosition.lerp(targetPosition, alpha);
        currentSize.lerp(targetSize, alpha);
        currentRotation = MathUtils.lerpAngleDeg(currentRotation, targetRotation, alpha);
        currentOpacity = MathUtils.lerp(currentOpacity, targetOpacity, alpha);
    }

    /**
     * Draws the card using the provided {@link SpriteBatch}. The caller is responsible for batch lifecycle.
     *
     * @param spriteBatch the batch used for rendering
     */
    public void render(final SpriteBatch spriteBatch) {
        final Texture texture = assetLoader.getTexture(card.imageAssetPath());
        final Color previousColor = spriteBatch.getColor();
        spriteBatch.setColor(previousColor.r, previousColor.g, previousColor.b, currentOpacity);
        spriteBatch.draw(
                texture,
                currentPosition.x,
                currentPosition.y,
                currentSize.x / 2f,
                currentSize.y / 2f,
                currentSize.x,
                currentSize.y,
                1f,
                1f,
                currentRotation,
                0,
                0,
                texture.getWidth(),
                texture.getHeight(),
                false,
                false);
        spriteBatch.setColor(previousColor);
    }

    /**
     * Renders the debug bounds using the supplied {@link ShapeRenderer} when debug mode is active.
     *
     * @param shapeRenderer the renderer used to display bounding outlines
     */
    public void renderDebug(final ShapeRenderer shapeRenderer) {
        if (!debugEnabled) {
            return;
        }
        shapeRenderer.setColor(Color.YELLOW);
        shapeRenderer.rect(
                currentPosition.x,
                currentPosition.y,
                currentSize.x / 2f,
                currentSize.y / 2f,
                currentSize.x,
                currentSize.y,
                1f,
                1f,
                currentRotation);
    }

    /**
     * Sets the target layout for the card.
     *
     * @param x      target x coordinate
     * @param y      target y coordinate
     * @param width  desired width
     * @param height desired height
     */
    public void setTargetLayout(final float x, final float y, final float width, final float height) {
        targetPosition.set(x, y);
        targetSize.set(Math.max(1f, width), Math.max(1f, height));
    }

    /**
     * Sets the target rotation for the rendered card in degrees.
     *
     * @param rotationDegrees the target rotation angle
     */
    public void setTargetRotation(final float rotationDegrees) {
        targetRotation = rotationDegrees;
    }

    /**
     * Sets the target opacity for the renderer.
     *
     * @param opacity desired opacity between 0 and 1
     */
    public void setTargetOpacity(final float opacity) {
        targetOpacity = MathUtils.clamp(opacity, 0f, 1f);
    }

    /**
     * Immediately aligns the current state with the target values.
     */
    public void snapToTarget() {
        currentPosition.set(targetPosition);
        currentSize.set(targetSize);
        currentRotation = targetRotation;
        currentOpacity = targetOpacity;
    }

    /**
     * Enables or disables debug rendering of card bounds.
     *
     * @param enabled {@code true} to enable debug rendering
     */
    public void setDebugEnabled(final boolean enabled) {
        debugEnabled = enabled;
    }

    /**
     * Indicates whether debug rendering is currently active.
     *
     * @return {@code true} when debug is enabled
     */
    public boolean isDebugEnabled() {
        return debugEnabled;
    }

    /**
     * Provides a snapshot of the renderer's current bounds in world space.
     *
     * @return the current {@link Rectangle}
     */
    public Rectangle getCurrentBounds() {
        return new Rectangle(currentPosition.x, currentPosition.y, currentSize.x, currentSize.y);
    }

    /**
     * Returns the card associated with this renderer.
     *
     * @return the rendered {@link Card}
     */
    public Card getCard() {
        return card;
    }

    /**
     * Obtains the target layout bounds for the renderer.
     *
     * @return a {@link Rectangle} describing the target bounds
     */
    public Rectangle getTargetBounds() {
        return new Rectangle(targetPosition.x, targetPosition.y, targetSize.x, targetSize.y);
    }

    /**
     * Adjusts the interpolation speed controlling transition smoothness.
     *
     * @param speed the interpolation multiplier, where higher values transition faster
     */
    public void setInterpolationSpeed(final float speed) {
        interpolationSpeed = Math.max(0f, speed);
    }

    /**
     * Retrieves the interpolation speed.
     *
     * @return the interpolation multiplier currently applied
     */
    public float getInterpolationSpeed() {
        return interpolationSpeed;
    }

    /**
     * Updates the target position while maintaining the existing width and height.
     *
     * @param x target x coordinate
     * @param y target y coordinate
     */
    public void setTargetPosition(final float x, final float y) {
        targetPosition.set(x, y);
    }

    /**
     * Retrieves the current position of the renderer.
     *
     * @return a {@link Vector2} containing the current x and y coordinates
     */
    public Vector2 getCurrentPosition() {
        return new Vector2(currentPosition);
    }

    /**
     * Retrieves the target position for the renderer.
     *
     * @return a {@link Vector2} copy of the target coordinates
     */
    public Vector2 getTargetPosition() {
        return new Vector2(targetPosition);
    }

    /**
     * Retrieves the current opacity applied to the sprite.
     *
     * @return the current alpha value
     */
    public float getCurrentOpacity() {
        return currentOpacity;
    }

    /**
     * Retrieves the target opacity used for interpolation.
     *
     * @return the target opacity value
     */
    public float getTargetOpacity() {
        return targetOpacity;
    }

    /**
     * Retrieves the current rotation in degrees.
     *
     * @return the interpolated rotation angle
     */
    public float getCurrentRotation() {
        return currentRotation;
    }

    /**
     * Retrieves the target rotation in degrees.
     *
     * @return the target rotation angle
     */
    public float getTargetRotation() {
        return targetRotation;
    }
}
