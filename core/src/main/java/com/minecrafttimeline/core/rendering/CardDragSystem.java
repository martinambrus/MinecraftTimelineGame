package com.minecrafttimeline.core.rendering;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.minecrafttimeline.core.game.GameRules;
import com.minecrafttimeline.core.game.GameSession;
import com.minecrafttimeline.core.input.InputHandler;
import com.minecrafttimeline.core.util.AssetLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Provides smooth drag handling with placement indicators and snap-back animations.
 */
public class CardDragSystem {

    private static final float DRAG_SMOOTHING_SPEED = 12f;
    private static final float SNAP_DURATION = 0.22f;
    private static final float OUTLINE_THICKNESS = 4f;

    private final AnimationManager animationManager;
    private final VisualFeedback visualFeedback;
    private final InputHandler inputHandler;
    private final List<PlacementZone> placementZones;
    private final GameSession gameSession;
    private final List<PlacementZone> validZones = new ArrayList<>();
    private final List<PlacementZone> invalidZones = new ArrayList<>();
    private final List<AnimationBinding> bindings = new ArrayList<>();
    private final List<PlacementZone> validZoneBuffer = new ArrayList<>();
    private final Texture outlineTexture;

    private final Vector2 smoothPosition = new Vector2();
    private final Vector2 dropTarget = new Vector2();
    private final Vector2 dragStartPosition = new Vector2();

    private boolean wasDragging;
    private CardRenderer draggingCard;

    /**
     * Creates a drag system that coordinates input-driven movement and snap animations.
     *
     * @param animationManager shared animation manager
     * @param visualFeedback   feedback system for success/error cues
     * @param inputHandler     input handler supplying drag state
     * @param zones            placement zones used for highlighting
     * @param loader           asset loader supplying the outline texture
     */
    public CardDragSystem(
            final AnimationManager animationManager,
            final VisualFeedback visualFeedback,
            final InputHandler inputHandler,
            final List<?> zones,
            final AssetLoader loader,
            final GameSession gameSession) {
        this.animationManager = Objects.requireNonNull(animationManager, "animationManager must not be null");
        this.visualFeedback = Objects.requireNonNull(visualFeedback, "visualFeedback must not be null");
        this.inputHandler = Objects.requireNonNull(inputHandler, "inputHandler must not be null");
        placementZones = convertToPlacementZones(zones);
        outlineTexture = Objects.requireNonNull(loader, "loader must not be null").getTexture("images/white_pixel.png");
        this.gameSession = gameSession;
    }

    /**
     * Updates drag state and applies smooth interpolation toward the cursor/finger target. Must be called once per
     * frame before rendering.
     *
     * @param delta time delta in seconds
     */
    public void update(final float delta) {
        final CardRenderer selectedCard = inputHandler.getSelectedCard();
        final boolean dragging = inputHandler.isCardDragging();

        if (dragging && selectedCard != null) {
            handleActiveDrag(delta, selectedCard);
            recalculateZoneHighlights(selectedCard);
        } else {
            if (wasDragging && draggingCard != null) {
                handleDrop(draggingCard);
                draggingCard = null;
            }
            recalculateZoneHighlights(null);
        }

        wasDragging = dragging;
        updateBindings();
    }

    private void handleActiveDrag(final float delta, final CardRenderer selectedCard) {
        if (!wasDragging) {
            draggingCard = selectedCard;
            smoothPosition.set(selectedCard.getPosition());
            dragStartPosition.set(selectedCard.getPosition());
        }
        final Vector2 targetPosition = inputHandler.getSelectedCardPosition();
        final float smoothingFactor = Math.min(1f, delta * DRAG_SMOOTHING_SPEED);
        smoothPosition.lerp(targetPosition, smoothingFactor);
        selectedCard.setPosition(smoothPosition.x, smoothPosition.y);
    }

    private void handleDrop(final CardRenderer card) {
        final PlacementEvaluation evaluation = attemptPlacement(card);
        if (evaluation.result == DropResult.SUCCESS) {
            final PlacementZone targetZone = evaluation.zone;
            if (targetZone != null) {
                final Vector2 zoneCenter = targetZone.getCenter();
                final Vector2 cardSize = card.getSize();
                dropTarget.set(zoneCenter.x - (cardSize.x / 2f), zoneCenter.y - (cardSize.y / 2f));
                snapCardToPosition(card, dropTarget);
                visualFeedback.displayFeedback(VisualFeedback.FeedbackType.SUCCESS_PLACEMENT, card, dropTarget);
            } else {
                visualFeedback.displayFeedback(VisualFeedback.FeedbackType.SUCCESS_PLACEMENT, card, card.getPosition());
            }
            return;
        }
        dropTarget.set(dragStartPosition);
        snapCardToPosition(card, dropTarget);
        if (evaluation.result == DropResult.FAILURE || evaluation.result == DropResult.IGNORED) {
            visualFeedback.displayFeedback(VisualFeedback.FeedbackType.INVALID_PLACEMENT, card, dragStartPosition);
        }
    }

    private PlacementEvaluation attemptPlacement(final CardRenderer card) {
        if (gameSession == null || card == null) {
            return new PlacementEvaluation(DropResult.IGNORED, null);
        }
        final com.minecrafttimeline.core.card.Card modelCard = card.getCard();
        if (!gameSession.getGameState().getHand().contains(modelCard)) {
            return new PlacementEvaluation(DropResult.IGNORED, null);
        }
        final int insertionIndex = calculateInsertionIndex(card);
        if (insertionIndex < 0) {
            return new PlacementEvaluation(DropResult.IGNORED, null);
        }
        final PlacementZone zone = insertionIndex < placementZones.size() ? placementZones.get(insertionIndex) : null;
        final boolean placed = gameSession.placeCard(modelCard, insertionIndex);
        if (placed) {
            card.setOpacity(0f);
            return new PlacementEvaluation(DropResult.SUCCESS, zone);
        }
        return new PlacementEvaluation(DropResult.FAILURE, zone);
    }

    private int calculateInsertionIndex(final CardRenderer card) {
        if (card == null) {
            return -1;
        }
        final PlacementZone zone = findZoneContainingCard(card);
        if (zone == null) {
            return -1;
        }
        for (int i = 0; i < placementZones.size(); i++) {
            if (placementZones.get(i) == zone) {
                return i;
            }
        }
        return -1;
    }

    private PlacementZone findZoneContainingCard(final CardRenderer card) {
        if (card == null || placementZones.isEmpty()) {
            return null;
        }
        final Vector2 cardCenter = card.getCenter();
        for (int i = 0; i < placementZones.size(); i++) {
            final PlacementZone zone = placementZones.get(i);
            if (zone != null && zone.getBounds().contains(cardCenter)) {
                return zone;
            }
        }
        return null;
    }

    private List<PlacementZone> convertToPlacementZones(final List<?> zones) {
        if (zones == null || zones.isEmpty()) {
            return Collections.emptyList();
        }
        boolean allPlacementZones = true;
        for (int i = 0; i < zones.size(); i++) {
            final Object element = zones.get(i);
            if (element != null && !(element instanceof PlacementZone)) {
                allPlacementZones = false;
                break;
            }
        }
        if (allPlacementZones) {
            @SuppressWarnings("unchecked")
            final List<PlacementZone> placementZoneList = (List<PlacementZone>) zones;
            return placementZoneList;
        }
        final List<PlacementZone> converted = new ArrayList<>(zones.size());
        for (int i = 0; i < zones.size(); i++) {
            final Object element = zones.get(i);
            if (element instanceof PlacementZone) {
                converted.add((PlacementZone) element);
            } else if (element instanceof CardRenderer) {
                converted.add(new RendererPlacementZone((CardRenderer) element));
            } else {
                converted.add(null);
            }
        }
        return converted;
    }

    private void updateBindings() {
        for (int i = 0; i < bindings.size(); ) {
            final AnimationBinding binding = bindings.get(i);
            if (binding.active && !binding.animation.isDone()) {
                final float value = binding.animation.getCurrentValue();
                if (binding.axis == Axis.X) {
                    binding.card.setPosition(value, binding.card.getPosition().y);
                } else {
                    binding.card.setPosition(binding.card.getPosition().x, value);
                }
                i++;
            } else {
                if (binding.axis == Axis.X) {
                    binding.card.setPosition(binding.targetValue, binding.card.getPosition().y);
                } else {
                    binding.card.setPosition(binding.card.getPosition().x, binding.targetValue);
                }
                final int lastIndex = bindings.size() - 1;
                bindings.set(i, bindings.get(lastIndex));
                bindings.remove(lastIndex);
            }
        }
    }

    /**
     * Updates the list of valid zones. Any tracked placement zone not included in {@code validPositions} becomes an
     * invalid zone for the purposes of red outline rendering.
     *
     * @param validPositions list of valid placement renderers; may be {@code null}
     */
    public void updateValidZones(final List<?> validPositions) {
        validZones.clear();
        invalidZones.clear();
        if (validPositions == null) {
            return;
        }
        for (int i = 0; i < validPositions.size(); i++) {
            final PlacementZone zone = findMatchingZone(validPositions.get(i));
            if (zone != null) {
                validZones.add(zone);
            }
        }
        for (int i = 0; i < placementZones.size(); i++) {
            final PlacementZone zone = placementZones.get(i);
            if (!validZones.contains(zone)) {
                invalidZones.add(zone);
            }
        }
    }

    /**
     * Renders green outlines over valid placement zones.
     *
     * @param batch shared sprite batch
     */
    public void renderValidZones(final SpriteBatch batch) {
        renderZones(batch, validZones, 0f, 1f, 0f, 0.35f);
    }

    /**
     * Renders red outlines over invalid placement zones.
     *
     * @param batch shared sprite batch
     */
    public void renderInvalidZones(final SpriteBatch batch) {
        renderZones(batch, invalidZones, 1f, 0f, 0f, 0.25f);
    }

    private void renderZones(final SpriteBatch batch, final List<PlacementZone> zones, final float r, final float g,
            final float b, final float alpha) {
        if (batch == null) {
            return;
        }
        final Color originalColor = batch.getColor();
        batch.setColor(r, g, b, alpha);
        for (int i = 0; i < zones.size(); i++) {
            final Rectangle bounds = zones.get(i).getBounds();
            drawOutline(batch, bounds);
        }
        batch.setColor(originalColor);
    }

    private void drawOutline(final SpriteBatch batch, final Rectangle bounds) {
        final float x = bounds.x - OUTLINE_THICKNESS;
        final float y = bounds.y - OUTLINE_THICKNESS;
        final float width = bounds.width + (2f * OUTLINE_THICKNESS);
        final float height = bounds.height + (2f * OUTLINE_THICKNESS);
        batch.draw(outlineTexture, x, y, width, OUTLINE_THICKNESS);
        batch.draw(outlineTexture, x, y + height - OUTLINE_THICKNESS, width, OUTLINE_THICKNESS);
        batch.draw(outlineTexture, x, y, OUTLINE_THICKNESS, height);
        batch.draw(outlineTexture, x + width - OUTLINE_THICKNESS, y, OUTLINE_THICKNESS, height);
    }

    /**
     * Creates snapping animations that move the specified card to the target position.
     *
     * @param card      card to animate
     * @param targetPos target position (bottom-left corner)
     */
    public void snapCardToPosition(final CardRenderer card, final Vector2 targetPos) {
        if (card == null || targetPos == null) {
            return;
        }
        final float targetX = targetPos.x;
        final float targetY = targetPos.y;
        final float currentX = card.getPosition().x;
        final float currentY = card.getPosition().y;

        final CardAnimation xAnimation = new CardAnimation(currentX, targetX, SNAP_DURATION, EasingFunctions::easeOutCubic);
        final CardAnimation yAnimation = new CardAnimation(currentY, targetY, SNAP_DURATION, EasingFunctions::easeOutCubic);
        xAnimation.setType(AnimationType.PLACE);
        yAnimation.setType(AnimationType.PLACE);

        final AnimationBinding xBinding = new AnimationBinding(card, xAnimation, Axis.X, targetX);
        final AnimationBinding yBinding = new AnimationBinding(card, yAnimation, Axis.Y, targetY);

        xAnimation.setOnComplete(() -> xBinding.active = false);
        yAnimation.setOnComplete(() -> yBinding.active = false);

        animationManager.addAnimation(xAnimation);
        animationManager.addAnimation(yAnimation);
        bindings.add(xBinding);
        bindings.add(yBinding);
    }

    private void recalculateZoneHighlights(final CardRenderer selectedCard) {
        if (selectedCard == null || gameSession == null) {
            updateValidZones(null);
            return;
        }
        final com.minecrafttimeline.core.card.Card modelCard = selectedCard.getCard();
        if (modelCard == null) {
            updateValidZones(null);
            return;
        }
        final List<Integer> validPositions =
                GameRules.getValidPositionsForCard(modelCard, gameSession.getGameState().getTimeline());
        validZoneBuffer.clear();
        for (int i = 0; i < validPositions.size(); i++) {
            final int index = validPositions.get(i);
            if (index >= 0 && index < placementZones.size()) {
                validZoneBuffer.add(placementZones.get(index));
            }
        }
        updateValidZones(validZoneBuffer);
    }

    private PlacementZone findMatchingZone(final Object candidate) {
        if (candidate instanceof PlacementZone) {
            final PlacementZone zone = (PlacementZone) candidate;
            return placementZones.contains(zone) ? zone : null;
        }
        if (candidate instanceof CardRenderer) {
            final CardRenderer renderer = (CardRenderer) candidate;
            for (int i = 0; i < placementZones.size(); i++) {
                final PlacementZone zone = placementZones.get(i);
                if (zone instanceof RendererPlacementZone
                        && ((RendererPlacementZone) zone).matchesRenderer(renderer)) {
                    return zone;
                }
            }
        }
        return null;
    }

    private static final class PlacementEvaluation {

        final DropResult result;
        final PlacementZone zone;

        PlacementEvaluation(final DropResult result, final PlacementZone zone) {
            this.result = result;
            this.zone = zone;
        }
    }

    private enum Axis {
        X,
        Y
    }

    private static final class AnimationBinding {

        final CardRenderer card;
        final CardAnimation animation;
        final Axis axis;
        final float targetValue;
        boolean active = true;

        AnimationBinding(final CardRenderer card, final CardAnimation animation, final Axis axis, final float targetValue) {
            this.card = card;
            this.animation = animation;
            this.axis = axis;
            this.targetValue = targetValue;
        }
    }

    private static final class RendererPlacementZone implements PlacementZone {

        private final CardRenderer renderer;

        RendererPlacementZone(final CardRenderer renderer) {
            this.renderer = Objects.requireNonNull(renderer, "renderer must not be null");
        }

        @Override
        public Rectangle getBounds() {
            return renderer.getBounds();
        }

        @Override
        public Vector2 getCenter() {
            return renderer.getCenter();
        }

        @Override
        public Vector2 getPosition() {
            return renderer.getPosition();
        }

        @Override
        public Vector2 getSize() {
            return renderer.getSize();
        }

        boolean matchesRenderer(final CardRenderer other) {
            return renderer == other;
        }
    }

    private enum DropResult {
        SUCCESS,
        FAILURE,
        IGNORED
    }
}
