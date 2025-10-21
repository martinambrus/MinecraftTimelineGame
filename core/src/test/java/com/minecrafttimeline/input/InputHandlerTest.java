package com.minecrafttimeline.input;

import static org.assertj.core.api.Assertions.assertThat;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.minecrafttimeline.TestApplicationSupport;
import com.minecrafttimeline.assets.AssetLoader;
import com.minecrafttimeline.cards.Card;
import com.minecrafttimeline.render.CardRenderer;
import com.minecrafttimeline.headless.HeadlessOrthographicCamera;
import java.time.LocalDate;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link InputHandler} covering selection, dragging and thread safety.
 */
class InputHandlerTest {

    private static final Card SAMPLE_CARD = new Card(
            "sample",
            "Sample",
            LocalDate.of(2010, 1, 1),
            "Trivia",
            "libgdx.png",
            "1.0");

    @BeforeAll
    static void configureHeadless() {
        TestApplicationSupport.initialise();
    }

    @BeforeEach
    void resetAssets() {
        AssetLoader.getInstance().dispose();
    }

    @Test
    void trackPointerDownSelectsTopmostRenderer() {
        final InputHandler handler = new InputHandler();
        final CardRenderer bottom = new CardRenderer(SAMPLE_CARD);
        final CardRenderer top = new CardRenderer(SAMPLE_CARD);
        handler.updateCardBounds(bottom, new Rectangle(0f, 0f, 100f, 100f));
        handler.updateCardBounds(top, new Rectangle(0f, 0f, 100f, 100f));

        final boolean handled = handler.trackPointerDown(50f, 50f);

        assertThat(handled).isTrue();
        assertThat(handler.getSelectedRenderer()).isSameAs(top);
        assertThat(handler.getDragOffset()).isEqualTo(new com.badlogic.gdx.math.Vector2(50f, 50f));
    }

    @Test
    void dragAndReleaseUpdatesPointerAndClearsSelection() {
        final InputHandler handler = new InputHandler();
        final CardRenderer renderer = new CardRenderer(SAMPLE_CARD);
        handler.updateCardBounds(renderer, new Rectangle(0f, 0f, 100f, 100f));
        handler.trackPointerDown(10f, 10f);

        handler.trackPointerDrag(25f, 30f);
        assertThat(handler.getPointerPosition()).isEqualTo(new com.badlogic.gdx.math.Vector2(25f, 30f));

        handler.trackPointerUp(40f, 50f);
        assertThat(handler.getSelectedRenderer()).isNull();
        assertThat(handler.isPointerActive()).isFalse();
        assertThat(handler.getLastReleasePosition()).isEqualTo(new com.badlogic.gdx.math.Vector2(40f, 50f));
    }

    @Test
    void touchDownUsesViewportCoordinates() {
        final InputHandler handler = new InputHandler();
        final FitViewport viewport = new FitViewport(100f, 100f, new HeadlessOrthographicCamera(100f, 100f));
        viewport.update(200, 200, true);
        handler.setViewport(viewport);
        final CardRenderer renderer = new CardRenderer(SAMPLE_CARD);
        handler.updateCardBounds(renderer, new Rectangle(0f, 0f, 100f, 100f));

        final Vector3 screen = new Vector3(10f, 10f, 0f);
        viewport.project(screen);

        final boolean handled = handler.touchDown((int) screen.x, (int) screen.y, 0, 0);

        assertThat(handled).isTrue();
        assertThat(handler.getSelectedRenderer()).isSameAs(renderer);
    }

    @Test
    void concurrentBoundsUpdatesRemainThreadSafe() throws InterruptedException {
        final InputHandler handler = new InputHandler();
        final CardRenderer renderer = new CardRenderer(SAMPLE_CARD);
        final ExecutorService executor = Executors.newFixedThreadPool(4);
        final CountDownLatch latch = new CountDownLatch(4);

        for (int i = 0; i < 4; i++) {
            executor.submit(() -> {
                for (int j = 0; j < 100; j++) {
                    handler.updateCardBounds(renderer, new Rectangle(j, j, 10f, 10f));
                    handler.trackPointerDown(j, j);
                    handler.trackPointerUp(j, j);
                }
                latch.countDown();
            });
        }

        latch.await(5, TimeUnit.SECONDS);
        executor.shutdownNow();

        assertThat(handler.getTrackedRenderers()).containsExactly(renderer);
        assertThat(handler.getBounds(renderer)).isPresent();
    }
}
