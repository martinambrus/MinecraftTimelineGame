package com.minecrafttimeline.render;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.minecrafttimeline.TestApplicationSupport;
import com.minecrafttimeline.assets.AssetLoader;
import com.minecrafttimeline.cards.Card;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link CardRenderer} verifying interpolation and rendering behaviour.
 */
class CardRendererTest {

    private static final Card SAMPLE_CARD = new Card(
            "card-1",
            "Test Card",
            LocalDate.of(2011, 11, 18),
            "Test trivia",
            "libgdx.png",
            "1.0");

    @BeforeAll
    static void initialiseHeadless() {
        TestApplicationSupport.initialise();
    }

    @BeforeEach
    void resetAssets() {
        AssetLoader.getInstance().dispose();
    }

    @Test
    void interpolatesTowardsTargetState() {
        final CardRenderer renderer = new CardRenderer(SAMPLE_CARD);
        renderer.setInterpolationSpeed(1f);
        renderer.setTargetLayout(100f, 200f, 160f, 220f);
        renderer.setTargetOpacity(0.5f);
        renderer.setTargetRotation(45f);

        renderer.update(0.5f);

        assertThat(renderer.getCurrentPosition().x).isCloseTo(50f, within(0.01f));
        assertThat(renderer.getCurrentPosition().y).isCloseTo(100f, within(0.01f));
        assertThat(renderer.getCurrentBounds().width).isCloseTo(80f, within(0.01f));
        assertThat(renderer.getCurrentBounds().height).isCloseTo(110f, within(0.01f));
        assertThat(renderer.getCurrentOpacity()).isCloseTo(0.75f, within(0.01f));
        assertThat(renderer.getCurrentRotation()).isCloseTo(22.5f, within(0.01f));
    }

    @Test
    void renderUsesSpriteBatchAndResetsColour() {
        final SpriteBatch batch = mock(SpriteBatch.class);
        final Color previous = new Color(1f, 1f, 1f, 1f);
        when(batch.getColor()).thenReturn(previous);
        final CardRenderer renderer = new CardRenderer(SAMPLE_CARD);
        renderer.setTargetLayout(10f, 20f, 30f, 40f);
        renderer.snapToTarget();

        renderer.render(batch);

        final Texture texture = AssetLoader.getInstance().getTexture("libgdx.png");
        verify(batch).draw(
                texture,
                10f,
                20f,
                15f,
                20f,
                30f,
                40f,
                1f,
                1f,
                0f,
                0,
                0,
                texture.getWidth(),
                texture.getHeight(),
                false,
                false);
        verify(batch).setColor(previous);
    }

    @Test
    void debugRendererDrawsBoundsWhenEnabled() {
        final ShapeRenderer shapeRenderer = mock(ShapeRenderer.class);
        final CardRenderer renderer = new CardRenderer(SAMPLE_CARD);
        renderer.setDebugEnabled(true);
        renderer.setTargetLayout(5f, 6f, 7f, 8f);
        renderer.snapToTarget();

        renderer.renderDebug(shapeRenderer);

        verify(shapeRenderer).rect(5f, 6f, 3.5f, 4f, 7f, 8f, 1f, 1f, 0f);
    }
}
