package com.minecrafttimeline.components;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import java.util.Objects;

/**
 * Basic text input component used for player names and chat entries.
 */
public class TextInput {

    private static final Color COLOR_BACKGROUND = new Color(0.12f, 0.12f, 0.16f, 0.85f);
    private static final Color COLOR_BACKGROUND_FOCUSED = new Color(0.2f, 0.2f, 0.3f, 0.9f);
    private static final Color COLOR_PLACEHOLDER = new Color(0.6f, 0.6f, 0.6f, 0.9f);
    private static final Color COLOR_TEXT = Color.WHITE;

    private static Texture pixelTexture;

    private final GlyphLayout layout = new GlyphLayout();

    private float x;
    private float y;
    private float width;
    private float height;
    private String placeholder;
    private int maxLength;
    private StringBuilder currentText = new StringBuilder();
    private boolean focused;

    /**
     * Creates a text input component.
     *
     * @param x           left coordinate
     * @param y           bottom coordinate
     * @param width       width in world units; must be positive
     * @param height      height in world units; must be positive
     * @param placeholder placeholder text displayed when empty; must not be {@code null}
     * @param maxLength   maximum characters allowed; must be positive
     */
    public TextInput(
            final float x,
            final float y,
            final float width,
            final float height,
            final String placeholder,
            final int maxLength) {
        if (width <= 0f || height <= 0f) {
            throw new IllegalArgumentException("width/height must be positive");
        }
        if (maxLength <= 0) {
            throw new IllegalArgumentException("maxLength must be positive");
        }
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.placeholder = Objects.requireNonNull(placeholder, "placeholder must not be null");
        this.maxLength = maxLength;
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
     * Renders the text input field.
     *
     * @param batch sprite batch; must not be {@code null}
     * @param font  font used for text; must not be {@code null}
     */
    public void render(final SpriteBatch batch, final BitmapFont font) {
        Objects.requireNonNull(batch, "batch must not be null");
        Objects.requireNonNull(font, "font must not be null");
        final Texture pixel = obtainPixel();
        batch.setColor(focused ? COLOR_BACKGROUND_FOCUSED : COLOR_BACKGROUND);
        batch.draw(pixel, x, y, width, height);
        batch.setColor(Color.WHITE);

        final String textToDraw = currentText.isEmpty() ? placeholder : currentText.toString();
        final Color color = currentText.isEmpty() ? COLOR_PLACEHOLDER : COLOR_TEXT;
        font.setColor(color);
        layout.setText(font, textToDraw);
        final float textX = x + 10f;
        final float textY = y + (height + layout.height) / 2f;
        font.draw(batch, layout, textX, textY);
    }

    /**
     * Appends the provided character when focused and within limits.
     *
     * @param character typed character
     */
    public void onKeyPressed(final char character) {
        if (!focused) {
            return;
        }
        if (Character.isISOControl(character)) {
            return;
        }
        if (currentText.length() >= maxLength) {
            return;
        }
        currentText.append(character);
    }

    /**
     * Removes the last character when focused.
     */
    public void onBackspace() {
        if (!focused || currentText.isEmpty()) {
            return;
        }
        currentText.deleteCharAt(currentText.length() - 1);
    }

    /**
     * Grants focus to the input field.
     */
    public void focus() {
        focused = true;
    }

    /**
     * Removes focus from the input field.
     */
    public void unfocus() {
        focused = false;
    }

    /**
     * Returns the current text value.
     *
     * @return input contents
     */
    public String getText() {
        return currentText.toString();
    }

    /**
     * Replaces the current value with the provided string.
     *
     * @param text new value; must not be {@code null}
     */
    public void setText(final String text) {
        final String value = Objects.requireNonNull(text, "text must not be null");
        currentText = new StringBuilder(value.substring(0, Math.min(value.length(), maxLength)));
    }

    public boolean isFocused() {
        return focused;
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

    public float getHeight() {
        return height;
    }

    /**
     * Determines whether the provided coordinates fall within the input bounds.
     *
     * @param pointX world x coordinate
     * @param pointY world y coordinate
     * @return {@code true} if the point lies inside the input
     */
    public boolean contains(final float pointX, final float pointY) {
        return pointX >= x && pointX <= (x + width) && pointY >= y && pointY <= (y + height);
    }
}
