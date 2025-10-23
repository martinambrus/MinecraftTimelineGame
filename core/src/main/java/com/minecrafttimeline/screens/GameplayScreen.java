package com.minecrafttimeline.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.minecrafttimeline.core.card.Card;
import com.minecrafttimeline.core.input.InputHandler;
import com.minecrafttimeline.core.rendering.AnimationManager;
import com.minecrafttimeline.core.rendering.CardDragSystem;
import com.minecrafttimeline.core.rendering.CardRenderer;
import com.minecrafttimeline.core.rendering.VisualFeedback;
import com.minecrafttimeline.core.rendering.ViewportConfig;
import com.minecrafttimeline.core.util.AssetLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Primary gameplay screen responsible for rendering cards and handling interaction.
 */
public class GameplayScreen implements Screen {

    private static final int FPS_UPDATE_INTERVAL = 60;
    private static final float CARD_ASPECT_RATIO = 0.7f;
    private static final float HAND_AREA_HEIGHT_RATIO = 0.45f;

    private final List<Card> handCards;
    private final List<Card> timelineCards;

    private final List<CardRenderer> timelineRenderers = new ArrayList<>();
    private final List<CardRenderer> handRenderers = new ArrayList<>();
    private final List<CardRenderer> combinedRenderers = new ArrayList<>();

    private final ViewportConfig viewportConfig = new ViewportConfig();

    private SpriteBatch spriteBatch;
    private InputHandler inputHandler;
    private BitmapFont fpsFont;
    private AnimationManager animationManager;
    private VisualFeedback visualFeedback;
    private CardDragSystem cardDragSystem;

    private int fpsFrameCounter;
    private String fpsDisplay = "FPS: 0";

    /**
     * Creates a gameplay screen with the provided card collections.
     *
     * @param handCards     cards currently in the player's hand; must not be {@code null}
     * @param timelineCards cards already on the timeline; must not be {@code null}
     */
    public GameplayScreen(final List<Card> handCards, final List<Card> timelineCards) {
        this.handCards = List.copyOf(Objects.requireNonNull(handCards, "handCards must not be null"));
        this.timelineCards = List.copyOf(Objects.requireNonNull(timelineCards, "timelineCards must not be null"));
    }

    @Override
    public void show() {
        spriteBatch = new SpriteBatch();
        fpsFont = new BitmapFont(); // Use libGDX's built-in default font
        fpsFont.setColor(Color.WHITE);

        layoutCards();

        combinedRenderers.clear();
        combinedRenderers.addAll(timelineRenderers);
        combinedRenderers.addAll(handRenderers);

        inputHandler = new InputHandler(viewportConfig, combinedRenderers);
        Gdx.input.setInputProcessor(inputHandler);

        animationManager = new AnimationManager();
        visualFeedback = new VisualFeedback(animationManager, AssetLoader.getInstance());
        cardDragSystem = new CardDragSystem(
                animationManager,
                visualFeedback,
                inputHandler,
                timelineRenderers,
                AssetLoader.getInstance());
        cardDragSystem.updateValidZones(timelineRenderers);

        viewportConfig.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    private void layoutCards() {
        timelineRenderers.clear();
        handRenderers.clear();
        final float worldWidth = ViewportConfig.BASE_WIDTH;
        final float worldHeight = ViewportConfig.BASE_HEIGHT;
        final float handAreaHeight = worldHeight * HAND_AREA_HEIGHT_RATIO;
        final float timelineAreaHeight = worldHeight - handAreaHeight;
        final float spacing = 20f;
        final int maxCardCount = Math.max(1, Math.max(handCards.size(), timelineCards.size()));
        final float availableWidth = worldWidth - (2f * spacing);
        final float distributedWidth = (availableWidth - ((maxCardCount - 1) * spacing)) / maxCardCount;
        final float candidateHeight = Math.min(timelineAreaHeight - (2f * spacing), distributedWidth / CARD_ASPECT_RATIO);
        final float cardHeight = Math.max(100f, candidateHeight);
        final float cardWidth = cardHeight * CARD_ASPECT_RATIO;
        float xOffset = spacing;
        final float yTimeline = spacing;
        for (Card card : timelineCards) {
            final CardRenderer renderer = new CardRenderer(card, xOffset, yTimeline, cardWidth, cardHeight);
            timelineRenderers.add(renderer);
            xOffset += renderer.getSize().x + spacing;
        }
        final float yHand = timelineAreaHeight + (handAreaHeight / 2f) - (cardHeight / 2f);
        xOffset = spacing;
        for (Card card : handCards) {
            final CardRenderer renderer = new CardRenderer(card, xOffset, yHand, cardWidth, cardHeight);
            handRenderers.add(renderer);
            xOffset += renderer.getSize().x + spacing;
        }
        if (cardDragSystem != null) {
            cardDragSystem.updateValidZones(timelineRenderers);
        }
    }

    @Override
    public void render(final float delta) {
        fpsFrameCounter++;
        if (fpsFrameCounter >= FPS_UPDATE_INTERVAL) {
            fpsFrameCounter = 0;
            fpsDisplay = "FPS: " + Gdx.graphics.getFramesPerSecond();
        }

        Gdx.gl.glClearColor(0.05f, 0.05f, 0.05f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        viewportConfig.getViewport().apply();
        spriteBatch.setProjectionMatrix(viewportConfig.getCamera().combined);

        if (animationManager != null) {
            animationManager.update(delta);
        }
        if (cardDragSystem != null) {
            cardDragSystem.update(delta);
        }
        if (visualFeedback != null) {
            visualFeedback.update();
        }

        spriteBatch.begin();
        if (cardDragSystem != null) {
            cardDragSystem.renderInvalidZones(spriteBatch);
            cardDragSystem.renderValidZones(spriteBatch);
        }
        // Draw timeline cards first so that the player's hand can render on top for clarity.
        for (CardRenderer renderer : timelineRenderers) {
            renderer.render(spriteBatch);
        }
        // Hand cards render after timeline cards to ensure selection priority.
        for (CardRenderer renderer : handRenderers) {
            renderer.render(spriteBatch);
        }
        if (visualFeedback != null) {
            visualFeedback.render(spriteBatch);
        }
        fpsFont.draw(spriteBatch, fpsDisplay, 20f, ViewportConfig.BASE_HEIGHT - 20f);
        spriteBatch.end();
    }

    @Override
    public void resize(final int width, final int height) {
        viewportConfig.update(width, height);
    }

    @Override
    public void pause() {
        // No-op
    }

    @Override
    public void resume() {
        // No-op
    }

    @Override
    public void hide() {
        if (Gdx.input.getInputProcessor() == inputHandler) {
            Gdx.input.setInputProcessor(null);
        }
    }

    @Override
    public void dispose() {
        if (spriteBatch != null) {
            spriteBatch.dispose();
        }
        if (fpsFont != null) {
            fpsFont.dispose();
        }
        if (animationManager != null) {
            animationManager.clear();
        }
    }

    /**
     * Retrieves the input handler for testing and diagnostics.
     *
     * @return current input handler or {@code null}
     */
    public InputHandler getInputHandler() {
        return inputHandler;
    }

    /**
     * Exposes the viewport configuration for testing.
     *
     * @return viewport configuration instance
     */
    public ViewportConfig getViewportConfig() {
        return viewportConfig;
    }

    /**
     * Provides read-only access to the timeline renderers.
     *
     * @return timeline renderer list
     */
    public List<CardRenderer> getTimelineRenderers() {
        return Collections.unmodifiableList(timelineRenderers);
    }

    /**
     * Provides read-only access to the hand renderers.
     *
     * @return hand renderer list
     */
    public List<CardRenderer> getHandRenderers() {
        return Collections.unmodifiableList(handRenderers);
    }
}
