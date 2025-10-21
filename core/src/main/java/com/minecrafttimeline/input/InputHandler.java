package com.minecrafttimeline.input;

import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.minecrafttimeline.render.CardRenderer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Thread-safe {@link InputProcessor} that tracks card selection and drag operations.
 */
public class InputHandler implements InputProcessor {

    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final Map<CardRenderer, Rectangle> rendererBounds = new LinkedHashMap<>();
    private final List<CardRenderer> drawOrder = new ArrayList<>();

    private final Vector2 pointerPosition = new Vector2();
    private final Vector2 dragOffset = new Vector2();
    private final Vector2 lastReleasePosition = new Vector2(Float.NaN, Float.NaN);

    private volatile Viewport viewport;
    private CardRenderer selectedRenderer;
    private boolean pointerActive;

    /**
     * Associates the handler with the viewport used for coordinate conversion.
     *
     * @param viewport the active viewport
     */
    public void setViewport(final Viewport viewport) {
        this.viewport = viewport;
    }

    /**
     * Updates the stored bounds for a {@link CardRenderer}.
     *
     * @param renderer the renderer to track
     * @param bounds   the current bounds for the renderer
     */
    public void updateCardBounds(final CardRenderer renderer, final Rectangle bounds) {
        Objects.requireNonNull(renderer, "renderer");
        Objects.requireNonNull(bounds, "bounds");
        lock.writeLock().lock();
        try {
            rendererBounds.put(renderer, new Rectangle(bounds));
            drawOrder.remove(renderer);
            drawOrder.add(renderer);
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Retrieves the {@link Rectangle} bounds associated with the renderer, if present.
     *
     * @param renderer the renderer to query
     * @return an optional containing the bounds snapshot
     */
    public Optional<Rectangle> getBounds(final CardRenderer renderer) {
        Objects.requireNonNull(renderer, "renderer");
        lock.readLock().lock();
        try {
            final Rectangle rectangle = rendererBounds.get(renderer);
            return rectangle == null ? Optional.empty() : Optional.of(new Rectangle(rectangle));
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Returns the currently selected card renderer.
     *
     * @return the selected renderer, or {@code null} when none are selected
     */
    public CardRenderer getSelectedRenderer() {
        lock.readLock().lock();
        try {
            return selectedRenderer;
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Retrieves the card associated with the current selection.
     *
     * @return the selected card, or {@code null} when nothing is selected
     */
    public com.minecrafttimeline.cards.Card getSelectedCard() {
        final CardRenderer renderer = getSelectedRenderer();
        return renderer == null ? null : renderer.getCard();
    }

    /**
     * Provides the latest pointer position in world coordinates.
     *
     * @return the pointer coordinates
     */
    public Vector2 getPointerPosition() {
        lock.readLock().lock();
        try {
            return new Vector2(pointerPosition);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Provides the offset between the pointer position and the selected card origin.
     *
     * @return the drag offset vector
     */
    public Vector2 getDragOffset() {
        lock.readLock().lock();
        try {
            return new Vector2(dragOffset);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Indicates whether a pointer is currently active.
     *
     * @return {@code true} if a pointer is down
     */
    public boolean isPointerActive() {
        lock.readLock().lock();
        try {
            return pointerActive;
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Retrieves the last pointer release position.
     *
     * @return the release coordinates or {@link Float#NaN} when no release occurred
     */
    public Vector2 getLastReleasePosition() {
        lock.readLock().lock();
        try {
            return new Vector2(lastReleasePosition);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Clears the tracked state, deselecting any active card.
     */
    public void clearSelection() {
        lock.writeLock().lock();
        try {
            selectedRenderer = null;
            pointerActive = false;
            dragOffset.setZero();
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public boolean touchDown(final int screenX, final int screenY, final int pointer, final int button) {
        final Vector2 world = convertToWorldCoordinates(screenX, screenY);
        return trackPointerDown(world.x, world.y);
    }

    @Override
    public boolean touchDragged(final int screenX, final int screenY, final int pointer) {
        final Vector2 world = convertToWorldCoordinates(screenX, screenY);
        return trackPointerDrag(world.x, world.y);
    }

    @Override
    public boolean touchUp(final int screenX, final int screenY, final int pointer, final int button) {
        final Vector2 world = convertToWorldCoordinates(screenX, screenY);
        trackPointerUp(world.x, world.y);
        return true;
    }

    /**
     * Handles pointer down events at the specified coordinates.
     *
     * @param x the x coordinate in world space
     * @param y the y coordinate in world space
     * @return {@code true} when a card is selected
     */
    public boolean trackPointerDown(final float x, final float y) {
        lock.writeLock().lock();
        try {
            pointerActive = true;
            pointerPosition.set(x, y);
            selectedRenderer = null;
            dragOffset.setZero();
            for (int i = drawOrder.size() - 1; i >= 0; i--) {
                final CardRenderer renderer = drawOrder.get(i);
                final Rectangle bounds = rendererBounds.get(renderer);
                if (bounds != null && bounds.contains(x, y)) {
                    selectedRenderer = renderer;
                    dragOffset.set(x - bounds.x, y - bounds.y);
                    break;
                }
            }
            return selectedRenderer != null;
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Handles pointer drag updates, tracking the latest pointer position.
     *
     * @param x the x coordinate in world space
     * @param y the y coordinate in world space
     */
    public boolean trackPointerDrag(final float x, final float y) {
        lock.writeLock().lock();
        try {
            if (!pointerActive) {
                return false;
            }
            pointerPosition.set(x, y);
            return selectedRenderer != null;
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Handles pointer release events, storing the final position and clearing the selection.
     *
     * @param x the x coordinate in world space
     * @param y the y coordinate in world space
     */
    public void trackPointerUp(final float x, final float y) {
        lock.writeLock().lock();
        try {
            pointerPosition.set(x, y);
            lastReleasePosition.set(x, y);
            pointerActive = false;
            selectedRenderer = null;
            dragOffset.setZero();
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public boolean keyDown(final int keycode) {
        return false;
    }

    @Override
    public boolean keyUp(final int keycode) {
        return false;
    }

    @Override
    public boolean keyTyped(final char character) {
        return false;
    }

    @Override
    public boolean mouseMoved(final int screenX, final int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(final float amountX, final float amountY) {
        return false;
    }

    private Vector2 convertToWorldCoordinates(final int screenX, final int screenY) {
        final Viewport activeViewport = viewport;
        if (activeViewport == null) {
            return new Vector2(screenX, screenY);
        }
        final Vector3 vector = new Vector3(screenX, screenY, 0f);
        activeViewport.unproject(vector);
        return new Vector2(vector.x, vector.y);
    }

    /**
     * Provides a thread-safe snapshot of the renderers currently tracked by the handler.
     *
     * @return an immutable list of tracked renderers
     */
    public List<CardRenderer> getTrackedRenderers() {
        lock.readLock().lock();
        try {
            return Collections.unmodifiableList(new ArrayList<>(drawOrder));
        } finally {
            lock.readLock().unlock();
        }
    }
}
