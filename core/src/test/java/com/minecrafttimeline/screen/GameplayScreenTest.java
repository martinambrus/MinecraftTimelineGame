package com.minecrafttimeline.screen;

import static org.assertj.core.api.Assertions.assertThat;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.minecrafttimeline.TestApplicationSupport;
import com.minecrafttimeline.assets.AssetLoader;
import com.minecrafttimeline.cards.Card;
import com.minecrafttimeline.cards.CardDeck;
import com.minecrafttimeline.input.InputHandler;
import com.minecrafttimeline.render.CardRenderer;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link GameplayScreen} covering rendering, layout, and input integration.
 */
class GameplayScreenTest {

    private TrackingSpriteBatch spriteBatch;
    private GameplayScreen gameplayScreen;
    private InputHandler inputHandler;

    @BeforeAll
    static void initialiseHeadless() {
        TestApplicationSupport.initialise();
    }

    @BeforeEach
    void setUp() {
        AssetLoader.getInstance().dispose();
        spriteBatch = new TrackingSpriteBatch();
        inputHandler = new InputHandler();
        final Viewport viewport = new FitViewport(1920f, 1080f);
        viewport.update(1920, 1080, true);
        gameplayScreen = new GameplayScreen(createDeck(), inputHandler, spriteBatch, viewport);
        gameplayScreen.show();
    }

    @AfterEach
    void tearDown() {
        gameplayScreen.dispose();
        spriteBatch.dispose();
    }

    @Test
    void renderUpdatesBatchesAndRecordsFrameMetrics() {
        gameplayScreen.render(1f / 60f);

        assertThat(spriteBatch.beginCount).isEqualTo(1);
        assertThat(spriteBatch.endCount).isEqualTo(1);
        assertThat(gameplayScreen.getLastRenderTimeMillis()).isGreaterThanOrEqualTo(0f);
        assertThat(gameplayScreen.getLastMeasuredFps()).isGreaterThanOrEqualTo(0);
    }

    @Test
    void resizeInvalidatesLayoutAndAdjustsViewport() {
        gameplayScreen.resize(1280, 720);
        gameplayScreen.render(1f / 60f);

        assertThat(gameplayScreen.getViewport().getScreenWidth()).isEqualTo(1280);
        assertThat(gameplayScreen.getViewport().getScreenHeight()).isEqualTo(720);
    }

    @Test
    void inputHandlerIntegratesWithRendering() {
        // Allow interpolation to settle for stable bounds
        for (int i = 0; i < 5; i++) {
            gameplayScreen.render(1f / 60f);
        }
        final List<CardRenderer> handRenderers = gameplayScreen.getHandRenderers();
        assertThat(handRenderers).isNotEmpty();
        final CardRenderer first = handRenderers.get(0);
        final com.badlogic.gdx.math.Rectangle bounds = first.getCurrentBounds();

        final boolean selected = inputHandler.trackPointerDown(bounds.x + 5f, bounds.y + 5f);
        assertThat(selected).isTrue();
        assertThat(inputHandler.getSelectedCard()).isSameAs(first.getCard());

        inputHandler.trackPointerDrag(bounds.x + 25f, bounds.y + 30f);
        gameplayScreen.render(1f / 60f);

        final com.badlogic.gdx.math.Vector2 pointer = inputHandler.getPointerPosition();
        final com.badlogic.gdx.math.Vector2 offset = inputHandler.getDragOffset();
        final com.badlogic.gdx.math.Vector2 expectedTarget = pointer.cpy().sub(offset);
        assertThat(first.getTargetPosition()).isEqualTo(expectedTarget);

        inputHandler.trackPointerUp(pointer.x, pointer.y);
        gameplayScreen.render(1f / 60f);
        assertThat(inputHandler.getSelectedRenderer()).isNull();
    }

    @Test
    void debugRenderingDoesNotThrow() {
        gameplayScreen.setDebugEnabled(true);
        gameplayScreen.render(1f / 60f);
        gameplayScreen.render(1f / 60f);
    }

    private static CardDeck createDeck() {
        final List<Card> cards = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            cards.add(new Card(
                    "card-" + i,
                    "Card " + i,
                    LocalDate.of(2010, 1, 1).plusDays(i),
                    "Trivia " + i,
                    "libgdx.png",
                    "1." + i));
        }
        return new CardDeck(cards);
    }

    private static final class TrackingSpriteBatch extends SpriteBatch {

        private int beginCount;
        private int endCount;

        @Override
        public void begin() {
            beginCount++;
            super.begin();
        }

        @Override
        public void end() {
            endCount++;
            super.end();
        }
    }
}
