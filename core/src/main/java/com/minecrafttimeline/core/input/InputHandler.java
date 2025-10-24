package com.minecrafttimeline.core.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.math.Vector2;
import com.minecrafttimeline.core.rendering.CardRenderer;
import com.minecrafttimeline.core.rendering.ViewportConfig;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Input processor responsible for translating screen touches/mouse movement to world interactions with cards.
 */
public class InputHandler implements InputProcessor {

    private final ViewportConfig viewportConfig;
    private final List<CardRenderer> cardRenderers;

    private final Vector2 worldTouch = new Vector2();
    private final Vector2 dragOffset = new Vector2();
    private final Vector2 selectedCardPosition = new Vector2();

    private CardRenderer selectedCard;
    private boolean dragging;
    private boolean debugLogging;

    /**
     * Creates a new handler instance for the supplied viewport and renderers.
     *
     * @param viewportConfig viewport used for world/screen transformations; must not be {@code null}
     * @param renderers      card renderers participating in hit detection; must not be {@code null}
     */
    public InputHandler(final ViewportConfig viewportConfig, final List<CardRenderer> renderers) {
        this.viewportConfig = Objects.requireNonNull(viewportConfig, "viewportConfig must not be null");
        cardRenderers = Objects.requireNonNull(renderers, "renderers must not be null");
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
    public boolean touchDown(final int screenX, final int screenY, final int pointer, final int button) {
        // Always log viewport state and screen dimensions on first click (only if Gdx is initialized)
        if (!debugLogging && Gdx.graphics != null) {
            Gdx.app.log("InputHandler", String.format(
                    "Screen size: %dx%d, Viewport screen: %dx%d, World: %.0fx%.0f, Camera: (%.1f,%.1f,%.1f)",
                    Gdx.graphics.getWidth(), Gdx.graphics.getHeight(),
                    viewportConfig.getViewport().getScreenWidth(), viewportConfig.getViewport().getScreenHeight(),
                    viewportConfig.getViewport().getWorldWidth(), viewportConfig.getViewport().getWorldHeight(),
                    viewportConfig.getCamera().position.x, viewportConfig.getCamera().position.y, viewportConfig.getCamera().position.z));
        }

        viewportConfig.screenToWorldCoordinates(screenX, screenY, worldTouch);
        selectedCard = findTopMostCard(worldTouch.x, worldTouch.y);

        // Always log touch events for debugging (only if Gdx is initialized)
        if (Gdx.app != null) {
            Gdx.app.log("InputHandler", String.format(
                    "touchDown screen=(%d,%d) world=(%.2f, %.2f) selected=%s cardCount=%d",
                    screenX, screenY, worldTouch.x, worldTouch.y, selectedCard, cardRenderers.size()));

            // Log all card bounds when nothing is selected to help debug
            if (selectedCard == null && cardRenderers.size() > 0) {
                Gdx.app.log("InputHandler", "=== Card Bounds Debug ===");
                for (int i = 0; i < cardRenderers.size(); i++) {
                    final CardRenderer r = cardRenderers.get(i);
                    if (r != null) {
                        final com.badlogic.gdx.math.Rectangle bounds = r.getBounds();
                        Gdx.app.log("InputHandler", String.format(
                                "  Card[%d]: bounds=(%.1f,%.1f,%.1fx%.1f) contains=(%.2f,%.2f)? %b",
                                i, bounds.x, bounds.y, bounds.width, bounds.height,
                                worldTouch.x, worldTouch.y,
                                bounds.contains(worldTouch.x, worldTouch.y)));
                    }
                }
            }
        }

        if (selectedCard != null) {
            dragging = true;
            selectedCardPosition.set(selectedCard.getPosition());
            dragOffset.set(worldTouch).sub(selectedCardPosition);
            return true;
        }
        return false;
    }

    @Override
    public boolean touchUp(final int screenX, final int screenY, final int pointer, final int button) {
        if (selectedCard == null) {
            return false;
        }
        dragging = false;
        if (debugLogging) {
            Gdx.app.log("InputHandler", String.format(
                    "touchUp screen=(%d,%d) selected=%s position=(%.2f, %.2f)",
                    screenX, screenY, selectedCard, selectedCard != null ? selectedCard.getPosition().x : 0f,
                    selectedCard != null ? selectedCard.getPosition().y : 0f));
        }
        return true;
    }

    @Override
    public boolean touchDragged(final int screenX, final int screenY, final int pointer) {
        if (!dragging || selectedCard == null) {
            return false;
        }
        viewportConfig.screenToWorldCoordinates(screenX, screenY, worldTouch);
        selectedCardPosition.set(worldTouch).sub(dragOffset);
        selectedCard.setPosition(selectedCardPosition.x, selectedCardPosition.y);
        if (debugLogging) {
            Gdx.app.log("InputHandler", String.format(
                    "touchDragged screen=(%d,%d) world=(%.2f, %.2f) cardPos=(%.2f, %.2f)",
                    screenX, screenY, worldTouch.x, worldTouch.y,
                    selectedCardPosition.x, selectedCardPosition.y));
        }
        return true;
    }

    @Override
    public boolean mouseMoved(final int screenX, final int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(final float amountX, final float amountY) {
        return false;
    }

    @Override
    public boolean touchCancelled(final int screenX, final int screenY, final int pointer, final int button) {
        return false;
    }

    /**
     * Retrieves the currently selected {@link CardRenderer}, if any.
     *
     * @return selected renderer or {@code null}
     */
    public CardRenderer getSelectedCard() {
        return selectedCard;
    }

    /**
     * Provides the mutable position used during card dragging.
     *
     * @return mutable position vector representing the bottom-left corner while dragging
     */
    public Vector2 getSelectedCardPosition() {
        return selectedCardPosition;
    }

    /**
     * Indicates whether a drag gesture is currently active.
     *
     * @return {@code true} when dragging a card
     */
    public boolean isCardDragging() {
        return dragging;
    }

    private CardRenderer findTopMostCard(final float worldX, final float worldY) {
        for (int i = cardRenderers.size() - 1; i >= 0; i--) {
            final CardRenderer renderer = cardRenderers.get(i);
            if (renderer != null && renderer.getBounds().contains(worldX, worldY)) {
                return renderer;
            }
        }
        return null;
    }

    /**
     * Exposes the tracked card renderers to facilitate testing.
     *
     * @return immutable list of renderers monitored by the handler
     */
    public List<CardRenderer> getCardRenderers() {
        return Collections.unmodifiableList(cardRenderers);
    }

    /**
     * Toggles verbose logging of input events.
     */
    public void toggleDebugLogging() {
        debugLogging = !debugLogging;
        Gdx.app.log("InputHandler", "Debug logging " + (debugLogging ? "enabled" : "disabled"));
    }

    /**
     * Indicates whether verbose logging is currently active.
     *
     * @return {@code true} when logging each touch event
     */
    public boolean isDebugLogging() {
        return debugLogging;
    }
}
