package com.minecrafttimeline.core.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.GdxRuntimeException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for the {@link AssetLoader} singleton, validating caching and fallback behavior.
 */
class AssetLoaderTest {

    private static final String TEXTURE_PATH = "textures/test.png";
    private static final String MISSING_TEXTURE_PATH = "textures/missing.png";

    private AssetLoader assetLoader;
    private AssetManager assetManager;
    private Texture texture;

    @BeforeEach
    void setUp() {
        assetLoader = AssetLoader.getInstance();
        assetLoader.resetForTesting();
        assetManager = mock(AssetManager.class);
        texture = mock(Texture.class);
        assetLoader.initializeWithManager(assetManager);
        assetLoader.setPlaceholderTexture(texture);
    }

    @AfterEach
    void tearDown() {
        assetLoader.resetForTesting();
    }

    @Test
    void singletonPatternReturnsSameInstance() {
        assertThat(AssetLoader.getInstance()).isSameAs(AssetLoader.getInstance());
    }

    @Test
    void getTextureCachesLoadedInstance() {
        when(assetManager.isLoaded(TEXTURE_PATH, Texture.class)).thenReturn(false, true);
        doNothing().when(assetManager).load(TEXTURE_PATH, Texture.class);
        when(assetManager.get(TEXTURE_PATH, Texture.class)).thenReturn(texture);

        final Texture first = assetLoader.getTexture(TEXTURE_PATH);
        final Texture second = assetLoader.getTexture(TEXTURE_PATH);

        assertThat(first).isSameAs(second);
        verify(assetManager, times(1)).load(TEXTURE_PATH, Texture.class);
    }

    @Test
    void missingTextureReturnsPlaceholderWithoutCrashing() {
        when(assetManager.isLoaded(MISSING_TEXTURE_PATH, Texture.class)).thenReturn(false);
        doNothing().when(assetManager).load(MISSING_TEXTURE_PATH, Texture.class);
        doThrow(new GdxRuntimeException("missing"))
                .when(assetManager)
                .finishLoadingAsset(MISSING_TEXTURE_PATH);

        final Texture result = assetLoader.getTexture(MISSING_TEXTURE_PATH);
        final Texture secondCall = assetLoader.getTexture(MISSING_TEXTURE_PATH);

        assertThat(result).isSameAs(texture);
        assertThat(secondCall).isSameAs(texture);
        verify(assetManager, times(1)).load(MISSING_TEXTURE_PATH, Texture.class);
    }

    @Test
    void disposeCleansUpManagedResources() {
        when(assetManager.isLoaded(TEXTURE_PATH, Texture.class)).thenReturn(true);
        when(assetManager.get(TEXTURE_PATH, Texture.class)).thenReturn(texture);
        doNothing().when(assetManager).dispose();
        doNothing().when(texture).dispose();

        assetLoader.dispose();

        verify(assetManager, times(1)).dispose();
        verify(texture, times(1)).dispose();
    }
}
