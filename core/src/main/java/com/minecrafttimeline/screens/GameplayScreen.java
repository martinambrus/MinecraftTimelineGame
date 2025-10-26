package com.minecrafttimeline.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.Input;
import com.minecrafttimeline.MinecraftTimelineGame;
import com.minecrafttimeline.core.game.GameSession;
import com.minecrafttimeline.core.input.InputHandler;
import com.minecrafttimeline.core.rendering.AnimationManager;
import com.minecrafttimeline.core.rendering.CardDragSystem;
import com.minecrafttimeline.core.rendering.CardRenderer;
import com.minecrafttimeline.core.rendering.TimelineSlotRenderer;
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
    private static final float SLOT_WIDTH_RATIO = 0.6f;
    private static final float SLOT_HEIGHT_RATIO = 0.7f;

    private final GameSession gameSession;
    private final ScreenManager screenManager;
    private final ViewportConfig viewportConfig = new ViewportConfig();
    private final List<CardRenderer> timelineRenderers = new ArrayList<>();
    private final List<CardRenderer> handRenderers = new ArrayList<>();
    private final List<TimelineSlotRenderer> timelineSlots = new ArrayList<>();

    private BitmapFont fpsFont;
    private AnimationManager animationManager;
    private VisualFeedback visualFeedback;
    private CardDragSystem cardDragSystem;
    private HUD hud;

    private int fpsFrameCounter;
    private String fpsDisplay = "FPS: 0";
    private int lastTimelineCount;
    private int lastHandCount;
    private int lastHandVersion;
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

        inputManager = new InputHandler(viewportConfig, handRenderers);
        Gdx.input.setInputProcessor(inputManager);
        cardDragSystem = new CardDragSystem(
                animationManager,
                visualFeedback,
                inputManager,
                timelineSlots,
                AssetLoader.getInstance(),
                gameSession);
        cardDragSystem.updateValidZones(null);
        viewportConfig.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    private void refreshRenderers() {
        timelineRenderers.clear();
        handRenderers.clear();
        timelineSlots.clear();
        final List<com.minecrafttimeline.core.card.Card> timelineCards = gameSession.getGameState().getTimeline();
        final List<com.minecrafttimeline.core.card.Card> handCards = gameSession.getGameState().getHand();
        final int handVersion = gameSession.getGameState().getHandSnapshotVersion();

        lastTimelineCount = timelineCards.size();
        lastHandCount = handCards.size();
        lastHandVersion = handVersion;

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

        Gdx.app.log("GameplayScreen", String.format(
                "refreshRenderers: timeline=%d hand=%d cardSize=(%.1fx%.1f) handAreaY=%.1f timelineY=%.1f",
                timelineCards.size(), handCards.size(), cardWidth, cardHeight,
                timelineAreaHeight + (handAreaHeight / 2f) - (cardHeight / 2f), spacing));

        final float yTimeline = spacing;
        final float timelineWidth = timelineCards.isEmpty()
                ? cardWidth
                : (timelineCards.size() * cardWidth)
                        + ((Math.max(0, timelineCards.size() - 1)) * spacing);
        float xOffset = Math.max(spacing, (worldWidth - timelineWidth) / 2f);
        for (final com.minecrafttimeline.core.card.Card card : timelineCards) {
            final CardRenderer renderer = new CardRenderer(card, xOffset, yTimeline, cardWidth, cardHeight);
            timelineRenderers.add(renderer);
            xOffset += renderer.getSize().x + spacing;
        }

        rebuildTimelineSlots(worldWidth, spacing, cardWidth, cardHeight, yTimeline);

        final float yHand = timelineAreaHeight + (handAreaHeight / 2f) - (cardHeight / 2f);
        xOffset = spacing;
        for (final com.minecrafttimeline.core.card.Card card : handCards) {
            final CardRenderer renderer = new CardRenderer(card, xOffset, yHand, cardWidth, cardHeight);
            Gdx.app.log("GameplayScreen", String.format(
                    "  Hand card '%s' at (%.1f, %.1f) size (%.1fx%.1f)",
                    card.getTitle(), xOffset, yHand, cardWidth, cardHeight));
            handRenderers.add(renderer);
            xOffset += renderer.getSize().x + spacing;
        }

        if (cardDragSystem != null) {
            cardDragSystem.updateValidZones(null);
        }
    }

    @Override
    protected void handleInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.F3)) {
            CardRenderer.setDebugEnabled(!CardRenderer.isDebugEnabled());
            Gdx.app.log("GameplayScreen", "Card debug overlays "
                    + (CardRenderer.isDebugEnabled() ? "enabled" : "disabled"));
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.F4)) {
            final com.badlogic.gdx.math.Vector3 cursor = getWorldCursorPosition();
            Gdx.app.log("GameplayScreen", String.format("Cursor world position: (%.2f, %.2f)", cursor.x, cursor.y));
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.F5) && inputManager != null) {
            inputManager.toggleDebugLogging();
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.F6)) {
            viewportConfig.setDebugLogging(!viewportConfig.isDebugLoggingEnabled());
        }
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
        final int handVersion = gameSession.getGameState().getHandSnapshotVersion();
        if (timelineSize != lastTimelineCount || handSize != lastHandCount || handVersion != lastHandVersion) {
            refreshRenderers();
        }

        if (!resultsDisplayed && gameSession.isGameOver()) {
            resultsDisplayed = true;
            screenManager.showResults(gameSession);
        }
    }

    @Override
    protected void renderScreen(final float delta) {
        for (final TimelineSlotRenderer slot : timelineSlots) {
            slot.render(batch);
        }
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

    private void rebuildTimelineSlots(
            final float worldWidth,
            final float spacing,
            final float cardWidth,
            final float cardHeight,
            final float yTimeline) {
        final float slotHeight = cardHeight * SLOT_HEIGHT_RATIO;
        final float slotYOffset = yTimeline + ((cardHeight - slotHeight) / 2f);
        final float slotWidthBase = Math.max(12f, spacing * 0.9f);
        final AssetLoader loader = AssetLoader.getInstance();

        if (timelineRenderers.isEmpty()) {
            final float emptySlotWidth = Math.max(cardWidth * SLOT_WIDTH_RATIO, slotWidthBase);
            final float slotX = (worldWidth - emptySlotWidth) / 2f;
            timelineSlots.add(new TimelineSlotRenderer(slotX, slotYOffset, emptySlotWidth, slotHeight, loader));
            return;
        }

        final float slotWidth = slotWidthBase;
        addSlotAt(centerWithinWorld(
                timelineRenderers.get(0).getPosition().x - (spacing / 2f), slotWidth, worldWidth),
                slotYOffset, slotWidth, slotHeight, loader);
        for (int i = 0; i < timelineRenderers.size() - 1; i++) {
            final CardRenderer left = timelineRenderers.get(i);
            final CardRenderer right = timelineRenderers.get(i + 1);
            final float leftEdge = left.getPosition().x + left.getSize().x;
            final float rightEdge = right.getPosition().x;
            final float center = (leftEdge + rightEdge) / 2f;
            addSlotAt(centerWithinWorld(center, slotWidth, worldWidth), slotYOffset, slotWidth, slotHeight, loader);
        }
        final CardRenderer last = timelineRenderers.get(timelineRenderers.size() - 1);
        final float lastCenter = last.getPosition().x + last.getSize().x + (spacing / 2f);
        addSlotAt(centerWithinWorld(lastCenter, slotWidth, worldWidth), slotYOffset, slotWidth, slotHeight, loader);
    }

    private void addSlotAt(
            final float centerX,
            final float slotY,
            final float slotWidth,
            final float slotHeight,
            final AssetLoader loader) {
        final float slotX = centerX - (slotWidth / 2f);
        timelineSlots.add(new TimelineSlotRenderer(slotX, slotY, slotWidth, slotHeight, loader));
    }

    private float centerWithinWorld(final float centerX, final float slotWidth, final float worldWidth) {
        final float half = slotWidth / 2f;
        final float min = half;
        final float max = worldWidth - half;
        return Math.max(min, Math.min(centerX, max));
    }
}
