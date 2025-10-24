package com.minecrafttimeline.components;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import java.util.Objects;

/**
 * Simple rectangular button used across multiple screens with hover/press feedback.
 */
public class Button {

    private static final float HOVER_SCALE = 1.05f;
    private static final float PRESSED_SCALE = 0.97f;
    private static final Color COLOR_NORMAL = new Color(0.2f, 0.2f, 0.22f, 0.85f);
    private static final Color COLOR_HOVER = new Color(0.4f, 0.35f, 0.2f, 0.9f);
    private static final Color COLOR_PRESSED = new Color(0.1f, 0.1f, 0.1f, 0.95f);
    private static final Color COLOR_TEXT_NORMAL = Color.WHITE;
    private static final Color COLOR_TEXT_HOVER = new Color(1f, 0.9f, 0.5f, 1f);

    private static Texture pixelTexture;

    private final GlyphLayout layout = new GlyphLayout();

    private float x;
    private float y;
    private float width;
    private float height;
    private String text;
    private Runnable onClick;
    private boolean hovered;
    private boolean pressed;

    /**
     * Creates a new button at the specified position.
     *
     * @param x      left coordinate
     * @param y      bottom coordinate
     * @param width  button width; must be positive
     * @param height button height; must be positive
     * @param text   label text; must not be {@code null}
     */
    public Button(final float x, final float y, final float width, final float height, final String text) {
        if (width <= 0f || height <= 0f) {
            throw new IllegalArgumentException("width/height must be positive");
        }
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.text = Objects.requireNonNull(text, "text must not be null");
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
     * Renders the button using the provided sprite batch and font.
     *
     * @param batch sprite batch; must not be {@code null}
     * @param font  font used to draw the label; must not be {@code null}
     */
    public void render(final SpriteBatch batch, final BitmapFont font) {
        Objects.requireNonNull(batch, "batch must not be null");
        Objects.requireNonNull(font, "font must not be null");
        final Texture pixel = obtainPixel();
        final float scale = pressed ? PRESSED_SCALE : (hovered ? HOVER_SCALE : 1f);
        final float scaledWidth = width * scale;
        final float scaledHeight = height * scale;
        final float offsetX = x + ((width - scaledWidth) / 2f);
        final float offsetY = y + ((height - scaledHeight) / 2f);

        final Color backgroundColor = pressed ? COLOR_PRESSED : (hovered ? COLOR_HOVER : COLOR_NORMAL);
        batch.setColor(backgroundColor);
        batch.draw(pixel, offsetX, offsetY, scaledWidth, scaledHeight);
        batch.setColor(Color.WHITE);

        final Color textColor = hovered ? COLOR_TEXT_HOVER : COLOR_TEXT_NORMAL;
        font.setColor(textColor);
        layout.setText(font, text);
        final float textX = offsetX + (scaledWidth - layout.width) / 2f;
        final float textY = offsetY + (scaledHeight + layout.height) / 2f;
        font.draw(batch, layout, textX, textY);
    }

    /**
     * Determines whether the provided world coordinates are inside the button bounds.
     *
     * @param pointX world x coordinate
     * @param pointY world y coordinate
     * @return {@code true} if inside, {@code false} otherwise
     */
    public boolean contains(final float pointX, final float pointY) {
        return pointX >= x && pointX <= (x + width) && pointY >= y && pointY <= (y + height);
    }

    /**
     * Notifies the button that a mouse/touch press occurred.
     *
     * @param pointX press x coordinate
     * @param pointY press y coordinate
     * @return {@code true} if the press started on the button
     */
    public boolean onMouseDown(final float pointX, final float pointY) {
        if (contains(pointX, pointY)) {
            pressed = true;
            return true;
        }
        return false;
    }

    /**
     * Signals that the mouse/touch interaction has ended. Executes the callback when appropriate.
     *
     * @return {@code true} if a click action was triggered
     */
    public boolean onMouseUp() {
        final boolean wasPressed = pressed && hovered;
        if (pressed) {
            pressed = false;
        }
        if (wasPressed && onClick != null) {
            onClick.run();
            return true;
        }
        return false;
    }

    /**
     * Updates hover state based on pointer movement.
     *
     * @param pointX pointer x coordinate
     * @param pointY pointer y coordinate
     */
    public void onMouseMoved(final float pointX, final float pointY) {
        hovered = contains(pointX, pointY);
        if (!hovered) {
            pressed = false;
        }
    }

    /**
     * Retrieves the hover-based scale multiplier.
     *
     * @return scale factor
     */
    public float getHoverScale() {
        return hovered ? HOVER_SCALE : 1f;
    }

    /**
     * Assigns a click callback.
     *
     * @param callback callback to execute when clicked; may be {@code null}
     */
    public void setOnClick(final Runnable callback) {
        onClick = callback;
    }

    public String getText() {
        return text;
    }

    public void setText(final String newText) {
        text = Objects.requireNonNull(newText, "newText must not be null");
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

    public boolean isHovered() {
        return hovered;
    }

    public boolean isPressed() {
        return pressed;
    }
}
