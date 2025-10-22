package com.minecrafttimeline.core.input;

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

    /**
     * Creates a new handler instance for the supplied viewport and renderers.
     *
     * @param viewportConfig viewport used for world/screen transformations; must not be {@code null}
     * @param renderers      card renderers participating in hit detection; must not be {@code null}
     */
    public InputHandler(final ViewportConfig viewportConfig, final List<CardRenderer> renderers) {
        this.viewportConfig = Objects.requireNonNull(viewportConfig, "viewportConfig must not be null");
        cardRenderers = List.copyOf(Objects.requireNonNull(renderers, "renderers must not be null"));
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
        viewportConfig.screenToWorldCoordinates(screenX, screenY, worldTouch);
        selectedCard = findTopMostCard(worldTouch.x, worldTouch.y);
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
}
