package com.minecrafttimeline.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.minecrafttimeline.core.card.Card;
import com.minecrafttimeline.core.input.InputHandler;
import com.minecrafttimeline.core.rendering.AnimationManager;
import com.minecrafttimeline.core.rendering.CardDragSystem;
import com.minecrafttimeline.core.rendering.CardRenderer;
import com.minecrafttimeline.core.rendering.VisualFeedback;
import com.minecrafttimeline.core.util.AssetLoader;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Integration tests for the drag/animation/feedback pipeline.
 */
class AnimationIntegrationTest {

    private AssetLoader assetLoader;

    @BeforeEach
    void setUp() {
        assetLoader = AssetLoader.getInstance();
        assetLoader.resetForTesting();
        final AssetManager assetManager = mock(AssetManager.class);
        final Texture texture = mock(Texture.class);
        when(assetManager.isLoaded(anyString(), eq(Texture.class))).thenReturn(true);
        when(assetManager.get(anyString(), eq(Texture.class))).thenReturn(texture);
        assetLoader.initializeWithManager(assetManager);
        assetLoader.setPlaceholderTexture(texture);
    }

    @AfterEach
    void tearDown() {
        assetLoader.resetForTesting();
    }

    @Test
    void dragPlaceAndFeedbackSequenceCompletes() {
        final Card handCardData = createCard("hand");
        final Card timelineCardData = createCard("timeline");
        final CardRenderer targetZone = new CardRenderer(timelineCardData, 400f, 200f, 120f, 180f, assetLoader);
        final CardRenderer draggedCard = new CardRenderer(handCardData, 50f, 350f, 120f, 180f, assetLoader);

        final List<CardRenderer> timelineZones = new ArrayList<>();
        timelineZones.add(targetZone);

        final AnimationManager manager = new AnimationManager();
        final VisualFeedback feedback = new VisualFeedback(manager, assetLoader);

        final AtomicBoolean dragging = new AtomicBoolean(true);
        final Vector2 dragPosition = new Vector2(targetZone.getPosition());
        final InputHandler inputHandler = mock(InputHandler.class);
        when(inputHandler.getSelectedCard()).thenAnswer(invocation -> dragging.get() ? draggedCard : null);
        when(inputHandler.isCardDragging()).thenAnswer(invocation -> dragging.get());
        when(inputHandler.getSelectedCardPosition()).thenAnswer(invocation -> dragPosition);

        final CardDragSystem dragSystem = new CardDragSystem(manager, feedback, inputHandler, timelineZones, assetLoader);
        dragSystem.updateValidZones(timelineZones);

        final float delta = 1f / 60f;
        float previousDistance = distanceToZone(draggedCard, targetZone);
        for (int i = 0; i < 10; i++) {
            manager.update(delta);
            dragSystem.update(delta);
            feedback.update();
            final float currentDistance = distanceToZone(draggedCard, targetZone);
            assertThat(currentDistance).isLessThanOrEqualTo(previousDistance + 0.5f);
            previousDistance = currentDistance;
        }

        dragging.set(false);
        manager.update(delta);
        dragSystem.update(delta);
        feedback.update();

        assertThat(manager.getActiveCount()).isGreaterThan(0);

        for (int i = 0; i < 90; i++) {
            manager.update(delta);
            dragSystem.update(delta);
            feedback.update();
        }

        assertThat(manager.hasAnimationsRunning()).isFalse();
        assertThat(draggedCard.getPosition().x).isCloseTo(targetZone.getPosition().x, offset());
        assertThat(draggedCard.getPosition().y).isCloseTo(targetZone.getPosition().y, offset());
    }

    private Card createCard(final String idSuffix) {
        return new Card(
                "id-" + idSuffix,
                "Card " + idSuffix,
                LocalDate.of(2015, 1, 1),
                "Trivia",
                "textures/test.png",
                "1.0");
    }

    private float distanceToZone(final CardRenderer card, final CardRenderer zone) {
        final Vector2 cardCenter = card.getCenter();
        final Vector2 zoneCenter = zone.getCenter();
        return cardCenter.dst(zoneCenter);
    }

    private org.assertj.core.data.Offset<Float> offset() {
        return org.assertj.core.data.Offset.offset(0.5f);
    }
}
