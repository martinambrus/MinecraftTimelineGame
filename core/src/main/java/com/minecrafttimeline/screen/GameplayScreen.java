package com.minecrafttimeline.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.utils.GdxNativesLoader;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.minecrafttimeline.assets.AssetLoader;
import com.minecrafttimeline.cards.Card;
import com.minecrafttimeline.cards.CardDeck;
import com.minecrafttimeline.input.InputHandler;
import com.minecrafttimeline.logging.Logger;
import com.minecrafttimeline.render.CardRenderer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Primary gameplay screen responsible for rendering the timeline and player hand.
 */
public class GameplayScreen implements Screen {

    private static final float WORLD_WIDTH = 1920f;
    private static final float WORLD_HEIGHT = 1080f;
    private static final float DESIRED_CARD_ASPECT = 0.7f;
    private static final int MAX_HAND_CARDS = 5;

    private final CardDeck deck;
    private final InputHandler inputHandler;
    private final Batch spriteBatch;
    private final Viewport viewport;
    private final List<CardRenderer> timelineRenderers = new ArrayList<>();
    private final List<CardRenderer> handRenderers = new ArrayList<>();

    private ShapeRenderer debugShapeRenderer;
    private boolean debugEnabled;
    private boolean layoutDirty = true;

    private float fpsTimer;
    private int lastMeasuredFps;
    private float lastRenderTimeMillis;

    /**
     * Creates a gameplay screen using default rendering components.
     *
     * @param deck the deck containing cards to display
     */
    public GameplayScreen(final CardDeck deck) {
        this(deck, new InputHandler(), new SpriteBatch(), new FitViewport(WORLD_WIDTH, WORLD_HEIGHT, new OrthographicCamera()));
    }

    /**
     * Creates a gameplay screen using the supplied dependencies, simplifying unit testing.
     *
     * @param deck         the card deck to display
     * @param inputHandler the input handler responsible for user interaction
     * @param spriteBatch  the sprite batch used for rendering
     * @param viewport     the viewport controlling world scaling
     */
    public GameplayScreen(
            final CardDeck deck,
            final InputHandler inputHandler,
            final Batch spriteBatch,
            final Viewport viewport) {
        this.deck = Objects.requireNonNull(deck, "deck");
        this.inputHandler = Objects.requireNonNull(inputHandler, "inputHandler");
        this.spriteBatch = Objects.requireNonNull(spriteBatch, "spriteBatch");
        this.viewport = Objects.requireNonNull(viewport, "viewport");
        AssetLoader.initialize();
        initialiseRenderers();
    }

    private void initialiseRenderers() {
        final List<Card> cards = new ArrayList<>(deck.viewCards());
        final int handCount = Math.min(MAX_HAND_CARDS, cards.size());
        for (int i = 0; i < handCount; i++) {
            final Card card = cards.get(i);
            final CardRenderer renderer = new CardRenderer(card);
            renderer.setTargetOpacity(1f);
            handRenderers.add(renderer);
        }
        for (int i = handCount; i < cards.size(); i++) {
            final Card card = cards.get(i);
            final CardRenderer renderer = new CardRenderer(card);
            renderer.setTargetOpacity(1f);
            timelineRenderers.add(renderer);
        }
    }

    @Override
    public void show() {
        inputHandler.setViewport(viewport);
        Gdx.input.setInputProcessor(inputHandler);
        layoutDirty = true;
    }

    @Override
    public void render(final float delta) {
        if (layoutDirty) {
            layoutCards();
        }
        updateDragState();
        final long frameStart = TimeUtils.nanoTime();
        viewport.apply();
        spriteBatch.setProjectionMatrix(viewport.getCamera().combined);
        spriteBatch.begin();
        renderCards(delta, timelineRenderers);
        renderCards(delta, handRenderers);
        spriteBatch.end();
        if (debugEnabled) {
            renderDebug();
        }
        lastRenderTimeMillis = (TimeUtils.nanoTime() - frameStart) / 1_000_000f;
        updateFps(delta);
    }

    private void updateDragState() {
        final CardRenderer selected = inputHandler.getSelectedRenderer();
        if (selected == null || !inputHandler.isPointerActive()) {
            return;
        }
        final Vector2 pointer = inputHandler.getPointerPosition();
        final Vector2 offset = inputHandler.getDragOffset();
        final float width = Math.max(1f, selected.getCurrentBounds().width);
        final float height = Math.max(1f, selected.getCurrentBounds().height);
        selected.setTargetLayout(pointer.x - offset.x, pointer.y - offset.y, width, height);
    }

    private void renderCards(final float delta, final List<CardRenderer> renderers) {
        for (final CardRenderer renderer : renderers) {
            renderer.update(delta);
            renderer.render(spriteBatch);
            inputHandler.updateCardBounds(renderer, renderer.getCurrentBounds());
        }
    }

    private void renderDebug() {
        if (GdxNativesLoader.disableNativesLoading) {
            return;
        }
        if (debugShapeRenderer == null) {
            debugShapeRenderer = new ShapeRenderer();
        }
        debugShapeRenderer.setProjectionMatrix(viewport.getCamera().combined);
        debugShapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        for (final CardRenderer renderer : timelineRenderers) {
            renderer.renderDebug(debugShapeRenderer);
        }
        for (final CardRenderer renderer : handRenderers) {
            renderer.renderDebug(debugShapeRenderer);
        }
        debugShapeRenderer.end();
    }

    private void layoutCards() {
        final float worldWidth = viewport.getWorldWidth();
        final float worldHeight = viewport.getWorldHeight();
        final float padding = Math.min(worldWidth, worldHeight) * 0.025f;
        final float handAreaHeight = worldHeight * 0.28f;
        final float timelineAreaHeight = worldHeight * 0.32f;

        layoutRow(handRenderers, padding + worldHeight - handAreaHeight, handAreaHeight, worldWidth, padding);
        layoutRow(timelineRenderers, padding, timelineAreaHeight, worldWidth, padding);
        layoutDirty = false;
    }

    private void layoutRow(
            final List<CardRenderer> renderers,
            final float areaStartY,
            final float areaHeight,
            final float totalWidth,
            final float horizontalPadding) {
        if (renderers.isEmpty()) {
            return;
        }
        final float availableWidth = totalWidth - (horizontalPadding * 2f);
        final float cellWidth = availableWidth / renderers.size();
        final float maxCardHeight = Math.max(1f, areaHeight * 0.9f);
        final float desiredWidthFromHeight = maxCardHeight * DESIRED_CARD_ASPECT;
        float cardWidth = Math.min(cellWidth * 0.85f, desiredWidthFromHeight);
        float cardHeight = Math.min(maxCardHeight, cardWidth / DESIRED_CARD_ASPECT);
        if (cardHeight > maxCardHeight) {
            cardHeight = maxCardHeight;
            cardWidth = cardHeight * DESIRED_CARD_ASPECT;
        }
        final float verticalOffset = areaStartY + (areaHeight - cardHeight) / 2f;
        for (int i = 0; i < renderers.size(); i++) {
            final CardRenderer renderer = renderers.get(i);
            if (inputHandler.isPointerActive() && renderer.equals(inputHandler.getSelectedRenderer())) {
                continue;
            }
            final float baseX = horizontalPadding + i * cellWidth;
            final float x = baseX + (cellWidth - cardWidth) / 2f;
            renderer.setTargetLayout(x, verticalOffset, cardWidth, cardHeight);
            renderer.setTargetRotation(0f);
            renderer.setTargetOpacity(1f);
        }
    }

    private void updateFps(final float delta) {
        if (Gdx.graphics != null) {
            lastMeasuredFps = Gdx.graphics.getFramesPerSecond();
        }
        fpsTimer += delta;
        if (fpsTimer >= 1f) {
            Logger.debug("GameplayScreen FPS: {} Render time: {} ms", lastMeasuredFps, lastRenderTimeMillis);
            fpsTimer = 0f;
        }
    }

    @Override
    public void resize(final int width, final int height) {
        viewport.update(width, height, true);
        layoutDirty = true;
    }

    @Override
    public void pause() {
        // no specific pause behaviour required
    }

    @Override
    public void resume() {
        // no specific resume behaviour required
    }

    @Override
    public void hide() {
        inputHandler.clearSelection();
    }

    @Override
    public void dispose() {
        spriteBatch.dispose();
        if (debugShapeRenderer != null) {
            debugShapeRenderer.dispose();
        }
        timelineRenderers.clear();
        handRenderers.clear();
    }

    /**
     * Enables or disables debug rendering.
     *
     * @param enabled {@code true} to enable debug outlines
     */
    public void setDebugEnabled(final boolean enabled) {
        this.debugEnabled = enabled;
    }

    /**
     * Retrieves the viewport managing the screen layout.
     *
     * @return the active {@link Viewport}
     */
    public Viewport getViewport() {
        return viewport;
    }

    /**
     * Retrieves the input handler managing user interactions.
     *
     * @return the {@link InputHandler} associated with this screen
     */
    public InputHandler getInputHandler() {
        return inputHandler;
    }

    /**
     * Provides access to the timeline card renderers.
     *
     * @return an immutable view of the timeline renderers
     */
    public List<CardRenderer> getTimelineRenderers() {
        return Collections.unmodifiableList(timelineRenderers);
    }

    /**
     * Provides access to the hand card renderers.
     *
     * @return an immutable view of the hand renderers
     */
    public List<CardRenderer> getHandRenderers() {
        return Collections.unmodifiableList(handRenderers);
    }

    /**
     * Returns the most recent render time in milliseconds.
     *
     * @return the last frame render duration
     */
    public float getLastRenderTimeMillis() {
        return lastRenderTimeMillis;
    }

    /**
     * Returns the most recently measured frames per second.
     *
     * @return the last recorded FPS value
     */
    public int getLastMeasuredFps() {
        return lastMeasuredFps;
    }
}
