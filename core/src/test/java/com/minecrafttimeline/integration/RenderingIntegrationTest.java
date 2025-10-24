package com.minecrafttimeline.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Vector2;
import com.minecrafttimeline.core.card.Card;
import com.minecrafttimeline.core.card.CardManager;
import com.minecrafttimeline.core.input.InputHandler;
import com.minecrafttimeline.core.rendering.CardRenderer;
import com.minecrafttimeline.core.rendering.ViewportConfig;
import com.minecrafttimeline.core.testing.GdxNativeTestUtils;
import com.minecrafttimeline.core.util.AssetLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Integration-style test verifying input selection across rendering components.
 */
class RenderingIntegrationTest {

    private AssetLoader assetLoader;

    @BeforeAll
    static void bootstrapGdx() {
        GdxNativeTestUtils.ensureHeadlessApplication();
    }

    @BeforeEach
    void setUp() {
        assetLoader = AssetLoader.getInstance();
        assetLoader.resetForTesting();
        final AssetManager assetManager = mock(AssetManager.class);
        final Texture texture = mock(Texture.class);
        final BitmapFont font = mock(BitmapFont.class);
        when(assetManager.isLoaded(anyString(), eq(Texture.class))).thenReturn(true);
        when(assetManager.isLoaded(anyString(), eq(BitmapFont.class))).thenReturn(true);
        when(assetManager.get(anyString(), eq(Texture.class))).thenReturn(texture);
        when(assetManager.get(anyString(), eq(BitmapFont.class))).thenReturn(font);
        when(texture.getWidth()).thenReturn(256);
        when(texture.getHeight()).thenReturn(256);
        assetLoader.initializeWithManager(assetManager);
        assetLoader.setPlaceholderTexture(texture);
    }

    @AfterEach
    void tearDown() {
        assetLoader.resetForTesting();
    }

    @Test
    void cardsAreSelectableThroughViewportAndInput() throws Exception {
        final Path triviaPath = locateTrivia();
        final CardManager cardManager = CardManager.getInstance();
        cardManager.initialize(triviaPath.toString());
        final List<Card> allCards = new ArrayList<>(cardManager.getAllCards());
        allCards.sort(Comparator.comparing(Card::getDate));

        final ViewportConfig viewportConfig = new ViewportConfig();
        viewportConfig.update(1280, 720);

        final List<CardRenderer> renderers = new ArrayList<>();
        float x = 40f;
        for (Card card : allCards.subList(0, Math.min(4, allCards.size()))) {
            renderers.add(new CardRenderer(card, x, 80f, 120f, 180f, assetLoader));
            x += 140f;
        }
        assertThat(renderers).hasSizeGreaterThanOrEqualTo(2);

        final InputHandler handler = new InputHandler(viewportConfig, renderers);
        final CardRenderer target = renderers.get(1);
        final Vector2 screen = viewportConfig.worldToScreenCoordinates(
                target.getPosition().x + 10f,
                target.getPosition().y + 10f);

        handler.touchDown((int) screen.x, (int) screen.y, 0, 0);
        assertThat(handler.getSelectedCard()).isSameAs(target);

        final Vector2 world = viewportConfig.screenToWorldCoordinates((int) screen.x, (int) screen.y);
        assertThat(world.x).isCloseTo(target.getPosition().x + 10f, org.assertj.core.data.Offset.offset(0.1f));
        assertThat(world.y).isCloseTo(target.getPosition().y + 10f, org.assertj.core.data.Offset.offset(0.1f));
    }

    @Test
    void cardsRemainSelectableAfterViewportResize() throws Exception {
        final Path triviaPath = locateTrivia();
        final CardManager cardManager = CardManager.getInstance();
        cardManager.initialize(triviaPath.toString());
        final List<Card> allCards = new ArrayList<>(cardManager.getAllCards());
        allCards.sort(Comparator.comparing(Card::getDate));

        final ViewportConfig viewportConfig = new ViewportConfig();
        viewportConfig.update(1920, 1080);

        final List<CardRenderer> renderers = new ArrayList<>();
        float x = 40f;
        for (Card card : allCards.subList(0, Math.min(4, allCards.size()))) {
            renderers.add(new CardRenderer(card, x, 80f, 120f, 180f, assetLoader));
            x += 140f;
        }
        assertThat(renderers).hasSizeGreaterThanOrEqualTo(2);

        final InputHandler handler = new InputHandler(viewportConfig, renderers);
        final CardRenderer target = renderers.get(2);
        final Vector2 screen = viewportConfig.worldToScreenCoordinates(
                target.getPosition().x + 35f,
                target.getPosition().y + 75f);

        handler.touchDown((int) screen.x, (int) screen.y, 0, 0);
        assertThat(handler.getSelectedCard()).isSameAs(target);

        final Vector2 world = viewportConfig.screenToWorldCoordinates((int) screen.x, (int) screen.y);
        assertThat(world.x).isCloseTo(target.getPosition().x + 35f, org.assertj.core.data.Offset.offset(0.5f));
        assertThat(world.y).isCloseTo(target.getPosition().y + 75f, org.assertj.core.data.Offset.offset(0.5f));
    }

    @Test
    void cardsRemainSelectableWithAspectRatioChanges() throws Exception {
        final Path triviaPath = locateTrivia();
        final CardManager cardManager = CardManager.getInstance();
        cardManager.initialize(triviaPath.toString());
        final List<Card> allCards = new ArrayList<>(cardManager.getAllCards());
        allCards.sort(Comparator.comparing(Card::getDate));

        final ViewportConfig viewportConfig = new ViewportConfig();
        viewportConfig.update(1600, 900);

        final List<CardRenderer> renderers = new ArrayList<>();
        float x = 40f;
        for (Card card : allCards.subList(0, Math.min(3, allCards.size()))) {
            renderers.add(new CardRenderer(card, x, 120f, 120f, 180f, assetLoader));
            x += 160f;
        }

        final InputHandler handler = new InputHandler(viewportConfig, renderers);
        final CardRenderer target = renderers.get(0);
        final Vector2 screen = viewportConfig.worldToScreenCoordinates(
                target.getPosition().x + 60f,
                target.getPosition().y + 90f);

        handler.touchDown((int) screen.x, (int) screen.y, 0, 0);
        assertThat(handler.getSelectedCard()).isSameAs(target);

        final Vector2 world = viewportConfig.screenToWorldCoordinates((int) screen.x, (int) screen.y);
        assertThat(world.x).isCloseTo(target.getPosition().x + 60f, org.assertj.core.data.Offset.offset(0.5f));
        assertThat(world.y).isCloseTo(target.getPosition().y + 90f, org.assertj.core.data.Offset.offset(0.5f));
    }

    @Test
    void draggingCardUpdatesItsPositionInWorldSpace() throws Exception {
        final ViewportConfig viewportConfig = new ViewportConfig();
        viewportConfig.update(1280, 720);

        final Card card = new Card(
                "drag-test",
                "Drag Test",
                java.time.LocalDate.of(2010, 1, 1),
                "Trivia",
                "textures/test.png",
                "1.0");
        final CardRenderer renderer = new CardRenderer(card, 50f, 60f, 100f, 150f, assetLoader);
        final List<CardRenderer> renderers = List.of(renderer);
        final InputHandler handler = new InputHandler(viewportConfig, renderers);

        final Vector2 start = viewportConfig.worldToScreenCoordinates(80f, 100f);
        handler.touchDown((int) start.x, (int) start.y, 0, 0);

        final Vector2 dragTarget = viewportConfig.worldToScreenCoordinates(280f, 260f);
        handler.touchDragged((int) dragTarget.x, (int) dragTarget.y, 0);

        assertThat(handler.isCardDragging()).isTrue();
        assertThat(renderer.getPosition().x).isCloseTo(250f, org.assertj.core.data.Offset.offset(1.5f));
        assertThat(renderer.getPosition().y).isCloseTo(220f, org.assertj.core.data.Offset.offset(1.5f));
    }

    private Path locateTrivia() {
        Path triviaPath = Path.of("assets", "data", "trivia.json");
        if (!Files.exists(triviaPath)) {
            triviaPath = Path.of("core", "assets", "data", "trivia.json");
        }
        if (!Files.exists(triviaPath)) {
            throw new IllegalStateException("Unable to locate trivia database for rendering integration test");
        }
        return triviaPath;
    }
}
