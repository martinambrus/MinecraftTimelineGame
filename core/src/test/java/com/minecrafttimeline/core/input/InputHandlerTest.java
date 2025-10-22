package com.minecrafttimeline.core.input;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyFloat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.minecrafttimeline.core.card.Card;
import com.minecrafttimeline.core.rendering.CardRenderer;
import com.minecrafttimeline.core.rendering.ViewportConfig;
import com.minecrafttimeline.core.util.AssetLoader;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link InputHandler}, ensuring proper hit detection and coordinate conversion.
 */
class InputHandlerTest {

    private static final String IMAGE_PATH = "textures/test.png";

    private AssetLoader assetLoader;
    private ViewportConfig viewportConfig;

    private static final float WORLD_TO_SCREEN_SCALE = 2f;

    @BeforeEach
    void setUp() {
        assetLoader = AssetLoader.getInstance();
        assetLoader.resetForTesting();
        final AssetManager assetManager = mock(AssetManager.class);
        final Texture texture = mock(Texture.class);
        when(assetManager.isLoaded(IMAGE_PATH, Texture.class)).thenReturn(true);
        when(assetManager.get(IMAGE_PATH, Texture.class)).thenReturn(texture);
        when(texture.getWidth()).thenReturn(256);
        when(texture.getHeight()).thenReturn(256);
        assetLoader.initializeWithManager(assetManager);
        assetLoader.setPlaceholderTexture(texture);
        viewportConfig = mock(ViewportConfig.class);
        when(viewportConfig.worldToScreenCoordinates(anyFloat(), anyFloat()))
                .thenAnswer(invocation -> {
                    final float worldX = invocation.getArgument(0);
                    final float worldY = invocation.getArgument(1);
                    return new Vector2(worldX * WORLD_TO_SCREEN_SCALE, worldY * WORLD_TO_SCREEN_SCALE);
                });
        when(viewportConfig.screenToWorldCoordinates(anyInt(), anyInt(), any(Vector2.class)))
                .thenAnswer(invocation -> {
                    final int screenX = invocation.getArgument(0);
                    final int screenY = invocation.getArgument(1);
                    final Vector2 out = invocation.getArgument(2);
                    out.set(screenX / WORLD_TO_SCREEN_SCALE, screenY / WORLD_TO_SCREEN_SCALE);
                    return out;
                });
        when(viewportConfig.screenToWorldCoordinates(anyInt(), anyInt()))
                .thenAnswer(invocation -> new Vector2(
                        invocation.getArgument(0, Integer.class) / WORLD_TO_SCREEN_SCALE,
                        invocation.getArgument(1, Integer.class) / WORLD_TO_SCREEN_SCALE));
    }

    @AfterEach
    void tearDown() {
        assetLoader.resetForTesting();
    }

    @Test
    void touchDownSelectsCardUnderPointer() {
        final CardRenderer renderer = createRenderer(100f, 100f);
        final InputHandler handler = new InputHandler(viewportConfig, List.of(renderer));
        final Vector2 screen = viewportConfig.worldToScreenCoordinates(150f, 150f);
        handler.touchDown((int) screen.x, (int) screen.y, 0, 0);
        assertThat(handler.getSelectedCard()).isSameAs(renderer);
    }

    @Test
    void dragUpdatesCardPosition() {
        final CardRenderer renderer = createRenderer(50f, 50f);
        final InputHandler handler = new InputHandler(viewportConfig, List.of(renderer));
        final Vector2 start = viewportConfig.worldToScreenCoordinates(75f, 75f);
        handler.touchDown((int) start.x, (int) start.y, 0, 0);
        final Vector2 end = viewportConfig.worldToScreenCoordinates(200f, 200f);
        handler.touchDragged((int) end.x, (int) end.y, 0);
        assertThat(handler.isCardDragging()).isTrue();
        assertThat(renderer.getPosition().x).isEqualTo(175f);
        assertThat(renderer.getPosition().y).isEqualTo(175f);
    }

    @Test
    void coordinateConversionRoundTripMaintainsValues() {
        final Vector2 screen = viewportConfig.worldToScreenCoordinates(300f, 400f);
        final Vector2 world = viewportConfig.screenToWorldCoordinates((int) screen.x, (int) screen.y);
        assertThat(world.x).isCloseTo(300f, withinTolerance());
        assertThat(world.y).isCloseTo(400f, withinTolerance());
    }

    @Test
    void selectsTopMostCardWhenStacked() {
        final CardRenderer bottom = createRenderer(200f, 200f);
        final CardRenderer top = createRenderer(200f, 200f);
        final InputHandler handler = new InputHandler(viewportConfig, Arrays.asList(bottom, top));
        final Vector2 screen = viewportConfig.worldToScreenCoordinates(220f, 220f);
        handler.touchDown((int) screen.x, (int) screen.y, 0, 0);
        assertThat(handler.getSelectedCard()).isSameAs(top);
    }

    @Test
    void touchUpStopsDraggingButKeepsSelection() {
        final CardRenderer renderer = createRenderer(120f, 120f);
        final InputHandler handler = new InputHandler(viewportConfig, List.of(renderer));
        final Vector2 screen = viewportConfig.worldToScreenCoordinates(130f, 130f);
        handler.touchDown((int) screen.x, (int) screen.y, 0, 0);
        handler.touchUp((int) screen.x, (int) screen.y, 0, 0);
        assertThat(handler.isCardDragging()).isFalse();
        assertThat(handler.getSelectedCard()).isSameAs(renderer);
    }

    private CardRenderer createRenderer(final float x, final float y) {
        final Card card = new Card(
                "id-" + x + '-' + y,
                "Title",
                LocalDate.of(2010, 1, 1),
                "Trivia",
                IMAGE_PATH,
                "1.0");
        return new CardRenderer(card, x, y, 50f, 50f, assetLoader);
    }

    private static org.assertj.core.data.Offset<Float> withinTolerance() {
        return org.assertj.core.data.Offset.offset(0.1f);
    }
}
