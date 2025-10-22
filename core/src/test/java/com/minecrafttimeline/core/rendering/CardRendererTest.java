package com.minecrafttimeline.core.rendering;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.minecrafttimeline.core.card.Card;
import com.minecrafttimeline.core.util.AssetLoader;
import java.time.LocalDate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Verifies the data-centric operations performed by {@link CardRenderer}.
 */
class CardRendererTest {

    private static final String IMAGE_PATH = "textures/test.png";

    private AssetLoader assetLoader;

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
    }

    @AfterEach
    void tearDown() {
        assetLoader.resetForTesting();
    }

    @Test
    void positionCanBeUpdatedAndRead() {
        final CardRenderer renderer = createRenderer();
        renderer.setPosition(50f, 75f);
        assertThat(renderer.getPosition().x).isEqualTo(50f);
        assertThat(renderer.getPosition().y).isEqualTo(75f);
    }

    @Test
    void sizeCanBeUpdatedAndRead() {
        final CardRenderer renderer = createRenderer();
        renderer.setSize(128f, 256f);
        assertThat(renderer.getSize().x).isEqualTo(128f);
        assertThat(renderer.getSize().y).isEqualTo(256f);
    }

    @Test
    void boundsReflectPositionAndSize() {
        final CardRenderer renderer = createRenderer();
        renderer.setPosition(10f, 20f);
        renderer.setSize(100f, 200f);
        assertThat(renderer.getBounds().x).isEqualTo(10f);
        assertThat(renderer.getBounds().y).isEqualTo(20f);
        assertThat(renderer.getBounds().width).isEqualTo(100f);
        assertThat(renderer.getBounds().height).isEqualTo(200f);
    }

    @Test
    void rotationIsStored() {
        final CardRenderer renderer = createRenderer();
        renderer.setRotation(45f);
        assertThat(renderer.getRotation()).isEqualTo(45f);
    }

    @Test
    void opacityIsClampedBetweenZeroAndOne() {
        final CardRenderer renderer = createRenderer();
        renderer.setOpacity(1.5f);
        assertThat(renderer.getOpacity()).isEqualTo(1f);
        renderer.setOpacity(-0.5f);
        assertThat(renderer.getOpacity()).isEqualTo(0f);
    }

    private CardRenderer createRenderer() {
        final Card card = new Card(
                "id",
                "Title",
                LocalDate.of(2010, 1, 1),
                "Trivia",
                IMAGE_PATH,
                "1.0");
        return new CardRenderer(card, 0f, 0f, 100f, 150f, assetLoader);
    }
}
