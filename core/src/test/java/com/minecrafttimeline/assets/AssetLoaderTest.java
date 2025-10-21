package com.minecrafttimeline.assets;

import static org.assertj.core.api.Assertions.assertThat;

import com.badlogic.gdx.graphics.Texture;
import com.minecrafttimeline.TestApplicationSupport;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests verifying {@link AssetLoader} caching behaviour and cleanup.
 */
class AssetLoaderTest {

    @BeforeAll
    static void configureHeadlessEnvironment() {
        TestApplicationSupport.initialise();
    }

    @BeforeEach
    void resetLoader() {
        AssetLoader.getInstance().dispose();
    }

    @Test
    void initialiseReturnsSingletonInstance() {
        final AssetLoader first = AssetLoader.initialize();
        final AssetLoader second = AssetLoader.getInstance();
        assertThat(second).isSameAs(first);
    }

    @Test
    void cachesTextureRequests() {
        final AssetLoader loader = AssetLoader.getInstance();
        final Texture first = loader.getTexture("libgdx.png");
        final Texture second = loader.getTexture("libgdx.png");
        assertThat(first).isSameAs(second);
        assertThat(loader.getCachedTextureCount()).isEqualTo(1);
    }

    @Test
    void disposeClearsAllCaches() {
        final AssetLoader loader = AssetLoader.getInstance();
        loader.getTexture("missing/texture.png");
        loader.getFont("missing/font.fnt");
        loader.getSound("missing/sound.mp3");
        loader.getMusic("missing/music.ogg");

        assertThat(loader.getCachedTextureCount()).isEqualTo(1);
        assertThat(loader.getCachedFontCount()).isEqualTo(1);
        assertThat(loader.getCachedSoundCount()).isEqualTo(1);
        assertThat(loader.getCachedMusicCount()).isEqualTo(1);

        loader.dispose();

        assertThat(loader.getCachedTextureCount()).isZero();
        assertThat(loader.getCachedFontCount()).isZero();
        assertThat(loader.getCachedSoundCount()).isZero();
        assertThat(loader.getCachedMusicCount()).isZero();
    }
}
