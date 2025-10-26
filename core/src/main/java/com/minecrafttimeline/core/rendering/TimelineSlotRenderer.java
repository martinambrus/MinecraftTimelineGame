package com.minecrafttimeline.core.rendering;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.minecrafttimeline.core.util.AssetLoader;
import java.util.Objects;

/**
 * Renders the visual slots that indicate valid drop locations on the timeline.
 */
public class TimelineSlotRenderer implements PlacementZone {

    private static final float BORDER_THICKNESS = 3f;
    private static final Color FILL_COLOR = new Color(1f, 1f, 1f, 0.12f);
    private static final Color BORDER_COLOR = new Color(1f, 1f, 1f, 0.35f);

    private final Vector2 position = new Vector2();
    private final Vector2 size = new Vector2();
    private final Rectangle bounds = new Rectangle();
    private final Vector2 center = new Vector2();
    private final Texture texture;

    /**
     * Creates a renderer positioned at the supplied coordinates.
     *
     * @param x      bottom-left world X coordinate
     * @param y      bottom-left world Y coordinate
     * @param width  slot width in world units
     * @param height slot height in world units
     */
    public TimelineSlotRenderer(final float x, final float y, final float width, final float height) {
        this(x, y, width, height, AssetLoader.getInstance());
    }

    TimelineSlotRenderer(
            final float x,
            final float y,
            final float width,
            final float height,
            final AssetLoader loader) {
        texture = Objects.requireNonNull(loader, "loader must not be null").getTexture("images/white_pixel.png");
        position.set(x, y);
        size.set(width, height);
        bounds.set(x, y, width, height);
    }

    /**
     * Draws the slot using a translucent fill and an opaque border.
     *
     * @param batch shared sprite batch
     */
    public void render(final SpriteBatch batch) {
        if (batch == null) {
            return;
        }
        final Color previous = batch.getColor();
        batch.setColor(FILL_COLOR);
        batch.draw(texture, position.x, position.y, size.x, size.y);
        batch.setColor(BORDER_COLOR);
        final float border = Math.min(BORDER_THICKNESS, Math.min(size.x, size.y) / 6f);
        batch.draw(texture, position.x, position.y, size.x, border);
        batch.draw(texture, position.x, position.y + size.y - border, size.x, border);
        batch.draw(texture, position.x, position.y, border, size.y);
        batch.draw(texture, position.x + size.x - border, position.y, border, size.y);
        batch.setColor(previous);
    }

    @Override
    public Rectangle getBounds() {
        return bounds;
    }

    @Override
    public Vector2 getCenter() {
        center.set(position.x + (size.x / 2f), position.y + (size.y / 2f));
        return center;
    }

    @Override
    public Vector2 getPosition() {
        return position;
    }

    @Override
    public Vector2 getSize() {
        return size;
    }
}
