package com.minecrafttimeline.components;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Horizontal slider component for continuous values such as volume and animation speed.
 */
public class Slider {

    private static final Color COLOR_TRACK = new Color(0.2f, 0.2f, 0.25f, 1f);
    private static final Color COLOR_FILL = new Color(0.9f, 0.7f, 0.2f, 1f);
    private static final Color COLOR_KNOB = new Color(1f, 1f, 1f, 1f);
    private static final float KNOB_WIDTH = 16f;

    private static Texture pixelTexture;

    private final float x;
    private final float y;
    private final float width;
    private final float height;
    private final float minValue;
    private final float maxValue;
    private float currentValue;
    private Consumer<Float> onChange;

    /**
     * Creates a new slider.
     *
     * @param x          track x coordinate
     * @param y          track y coordinate
     * @param width      slider width; must be positive
     * @param height     slider height; must be positive
     * @param minValue   minimum value
     * @param maxValue   maximum value
     * @param value      initial value
     */
    public Slider(
            final float x,
            final float y,
            final float width,
            final float height,
            final float minValue,
            final float maxValue,
            final float value) {
        if (width <= 0f || height <= 0f) {
            throw new IllegalArgumentException("width/height must be positive");
        }
        if (maxValue <= minValue) {
            throw new IllegalArgumentException("maxValue must be greater than minValue");
        }
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.minValue = minValue;
        this.maxValue = maxValue;
        setValue(value);
    }

    private Texture obtainPixel() {
        if (pixelTexture == null) {
            final Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
            pixmap.setColor(Color.WHITE);
            pixmap.fill();
            pixelTexture = new Texture(pixmap);
            pixmap.dispose();
        }
        return pixelTexture;
    }

    /**
     * Renders the slider track, fill and knob.
     *
     * @param batch sprite batch; must not be {@code null}
     */
    public void render(final SpriteBatch batch) {
        Objects.requireNonNull(batch, "batch must not be null");
        final Texture pixel = obtainPixel();
        final float progress = getProgress();

        batch.setColor(COLOR_TRACK);
        batch.draw(pixel, x, y + (height / 2f) - 2f, width, 4f);

        batch.setColor(COLOR_FILL);
        batch.draw(pixel, x, y + (height / 2f) - 2f, width * progress, 4f);

        batch.setColor(COLOR_KNOB);
        final float knobX = x + (width * progress) - (KNOB_WIDTH / 2f);
        batch.draw(pixel, knobX, y, KNOB_WIDTH, height);
        batch.setColor(Color.WHITE);
    }

    /**
     * Updates the slider value based on the provided cursor x coordinate.
     *
     * @param cursorX cursor world x coordinate
     */
    public void onMouseDragged(final float cursorX) {
        final float clampedX = Math.max(x, Math.min(x + width, cursorX));
        final float progress = (clampedX - x) / width;
        setValue(minValue + progress * (maxValue - minValue));
    }

    /**
     * Returns the current slider value.
     *
     * @return current value
     */
    public float getValue() {
        return currentValue;
    }

    /**
     * Sets the slider value and notifies listeners.
     *
     * @param value new value
     */
    public void setValue(final float value) {
        final float clamped = Math.max(minValue, Math.min(maxValue, value));
        if (clamped != currentValue) {
            currentValue = clamped;
            if (onChange != null) {
                onChange.accept(currentValue);
            }
        } else {
            currentValue = clamped;
        }
    }

    /**
     * Assigns a callback invoked when the value changes.
     *
     * @param callback change callback; may be {@code null}
     */
    public void setOnChange(final Consumer<Float> callback) {
        onChange = callback;
    }

    public float getMinValue() {
        return minValue;
    }

    public float getMaxValue() {
        return maxValue;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getWidth() {
        return width;
    }

    private float getProgress() {
        return (currentValue - minValue) / (maxValue - minValue);
    }

    /**
     * Determines whether the slider bounds contain the specified point.
     *
     * @param pointX world x coordinate
     * @param pointY world y coordinate
     * @return {@code true} if the point lies within the slider rectangle
     */
    public boolean contains(final float pointX, final float pointY) {
        return pointX >= x && pointX <= (x + width) && pointY >= y && pointY <= (y + height);
    }
}
