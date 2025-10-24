package com.minecrafttimeline.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.minecrafttimeline.MinecraftTimelineGame;
import com.minecrafttimeline.core.game.GameSession;
import com.minecrafttimeline.core.input.InputHandler;
import com.minecrafttimeline.core.rendering.AnimationManager;
import com.minecrafttimeline.core.rendering.CardDragSystem;
import com.minecrafttimeline.core.rendering.CardRenderer;
import com.minecrafttimeline.core.rendering.ViewportConfig;
import com.minecrafttimeline.core.rendering.VisualFeedback;
import com.minecrafttimeline.core.util.AssetLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Gameplay screen responsible for rendering cards, handling drag interactions and displaying the HUD overlay.
 *
 * <p>Layout overview:</p>
 *
 * <pre>
 * +-------------------------------------------------------------+
 * |               HUD (scores / turn / progress)                |
 * |                                                             |
 * |  Timeline cards rendered across the centre of the screen    |
 * |                                                             |
 * |  Player hand cards anchored near the bottom                 |
 * +-------------------------------------------------------------+
 * </pre>
 */
public class GameplayScreen extends AbstractScreen {

    private static final int FPS_UPDATE_INTERVAL = 60;
    private static final float CARD_ASPECT_RATIO = 0.7f;
    private static final float HAND_AREA_HEIGHT_RATIO = 0.45f;

    private final GameSession gameSession;
    private final ScreenManager screenManager;
    private final ViewportConfig viewportConfig = new ViewportConfig();
    private final List<CardRenderer> timelineRenderers = new ArrayList<>();
    private final List<CardRenderer> handRenderers = new ArrayList<>();
    private final List<CardRenderer> combinedRenderers = new ArrayList<>();

    private BitmapFont fpsFont;
    private AnimationManager animationManager;
    private VisualFeedback visualFeedback;
    private CardDragSystem cardDragSystem;
    private HUD hud;

    private int fpsFrameCounter;
    private String fpsDisplay = "FPS: 0";
    private int lastTimelineCount;
    private int lastHandCount;
    private boolean resultsDisplayed;

    /**
     * Creates a new gameplay screen bound to the provided session.
     *
     * @param game         owning game instance; must not be {@code null}
     * @param gameSession  session providing gameplay data; must not be {@code null}
     * @param screenManager manager used for screen transitions; must not be {@code null}
     */
    public GameplayScreen(
            final MinecraftTimelineGame game,
            final GameSession gameSession,
            final ScreenManager screenManager) {
        super(game);
        this.gameSession = Objects.requireNonNull(gameSession, "gameSession must not be null");
        this.screenManager = Objects.requireNonNull(screenManager, "screenManager must not be null");
        camera = viewportConfig.getCamera();
        viewport = viewportConfig.getViewport();
        backgroundColor.set(0.05f, 0.05f, 0.08f, 1f);
    }

    @Override
    protected void buildUI() {
        fpsFont = new BitmapFont();
        fpsFont.setColor(Color.WHITE);

        hud = new HUD(screenManager.getSettings());

        animationManager = new AnimationManager();
        visualFeedback = new VisualFeedback(animationManager, AssetLoader.getInstance());

        refreshRenderers();
        combinedRenderers.clear();
        combinedRenderers.addAll(timelineRenderers);
        combinedRenderers.addAll(handRenderers);

        inputManager = new InputHandler(viewportConfig, combinedRenderers);
        Gdx.input.setInputProcessor(inputManager);
        cardDragSystem = new CardDragSystem(
                animationManager,
                visualFeedback,
                inputManager,
                timelineRenderers,
                AssetLoader.getInstance());
        viewportConfig.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    private void refreshRenderers() {
        timelineRenderers.clear();
        handRenderers.clear();
        final List<com.minecrafttimeline.core.card.Card> timelineCards = gameSession.getGameState().getTimeline();
        final List<com.minecrafttimeline.core.card.Card> handCards = gameSession.getGameState().getHand();

        lastTimelineCount = timelineCards.size();
        lastHandCount = handCards.size();

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
        for (final com.minecrafttimeline.core.card.Card card : timelineCards) {
            final CardRenderer renderer = new CardRenderer(card, xOffset, yTimeline, cardWidth, cardHeight);
            timelineRenderers.add(renderer);
            xOffset += renderer.getSize().x + spacing;
        }

        final float yHand = timelineAreaHeight + (handAreaHeight / 2f) - (cardHeight / 2f);
        xOffset = spacing;
        for (final com.minecrafttimeline.core.card.Card card : handCards) {
            final CardRenderer renderer = new CardRenderer(card, xOffset, yHand, cardWidth, cardHeight);
            handRenderers.add(renderer);
            xOffset += renderer.getSize().x + spacing;
        }

        if (cardDragSystem != null) {
            cardDragSystem.updateValidZones(timelineRenderers);
        }
    }

    @Override
    protected void handleInput() {
        // Gameplay input is handled by {@link InputHandler}; no additional polling required.
    }

    @Override
    protected void updateLogic(final float delta) {
        fpsFrameCounter++;
        if (fpsFrameCounter >= FPS_UPDATE_INTERVAL) {
            fpsFrameCounter = 0;
            fpsDisplay = "FPS: " + Gdx.graphics.getFramesPerSecond();
        }

        if (animationManager != null) {
            animationManager.update(delta);
        }
        if (cardDragSystem != null) {
            cardDragSystem.update(delta);
        }
        if (visualFeedback != null) {
            visualFeedback.update();
        }

        final int timelineSize = gameSession.getGameState().getTimeline().size();
        final int handSize = gameSession.getGameState().getHand().size();
        if (timelineSize != lastTimelineCount || handSize != lastHandCount) {
            refreshRenderers();
            combinedRenderers.clear();
            combinedRenderers.addAll(timelineRenderers);
            combinedRenderers.addAll(handRenderers);
        }

        if (!resultsDisplayed && gameSession.isGameOver()) {
            resultsDisplayed = true;
            screenManager.showResults(gameSession);
        }
    }

    @Override
    protected void renderScreen(final float delta) {
        if (cardDragSystem != null) {
            cardDragSystem.renderInvalidZones(batch);
            cardDragSystem.renderValidZones(batch);
        }
        for (final CardRenderer renderer : timelineRenderers) {
            renderer.render(batch);
        }
        for (final CardRenderer renderer : handRenderers) {
            renderer.render(batch);
        }
        if (visualFeedback != null) {
            visualFeedback.render(batch);
        }
        if (screenManager.getSettings().isShowFps()) {
            fpsFont.draw(batch, fpsDisplay, 20f, ViewportConfig.BASE_HEIGHT - 20f);
        }
        if (hud != null) {
            hud.render(batch, gameSession);
        }
    }

    @Override
    public void resize(final int width, final int height) {
        super.resize(width, height);
        viewportConfig.update(width, height);
    }

    @Override
    public void hide() {
        super.hide();
    }

    @Override
    public void dispose() {
        super.dispose();
        if (fpsFont != null) {
            fpsFont.dispose();
        }
        if (animationManager != null) {
            animationManager.clear();
        }
        if (hud != null) {
            hud.dispose();
        }
    }
}
