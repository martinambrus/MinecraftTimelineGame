package com.minecrafttimeline.assets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.minecrafttimeline.logging.Logger;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Singleton responsible for loading and caching audio and visual assets used by the game.
 */
public final class AssetLoader implements Disposable {

    private static final Object INITIALISATION_LOCK = new Object();
    private static volatile AssetLoader instance;

    private final Map<String, Texture> textures = new ConcurrentHashMap<>();
    private final Map<String, BitmapFont> fonts = new ConcurrentHashMap<>();
    private final Map<String, Sound> sounds = new ConcurrentHashMap<>();
    private final Map<String, Music> music = new ConcurrentHashMap<>();

    private final Texture placeholderTexture;
    private final BitmapFont placeholderFont;
    private final Sound placeholderSound;
    private final Music placeholderMusic;

    private AssetLoader() {
        placeholderTexture = createPlaceholderTexture();
        placeholderFont = new BitmapFont();
        placeholderSound = new NoOpSound();
        placeholderMusic = new NoOpMusic();
    }

    /**
     * Initialises the {@link AssetLoader} singleton if necessary and returns the instance.
     *
     * @return the shared {@link AssetLoader} instance
     */
    public static AssetLoader initialize() {
        AssetLoader localInstance = instance;
        if (localInstance == null) {
            synchronized (INITIALISATION_LOCK) {
                localInstance = instance;
                if (localInstance == null) {
                    localInstance = new AssetLoader();
                    instance = localInstance;
                }
            }
        }
        return localInstance;
    }

    /**
     * Obtains the current {@link AssetLoader} instance, creating it if required.
     *
     * @return the {@link AssetLoader} singleton
     */
    public static AssetLoader getInstance() {
        return initialize();
    }

    /**
     * Retrieves a {@link Texture} for the provided asset path, loading and caching it when required.
     * Missing or invalid files yield a placeholder texture.
     *
     * @param assetPath the relative asset path
     * @return the loaded or cached {@link Texture}
     */
    public Texture getTexture(final String assetPath) {
        Objects.requireNonNull(assetPath, "assetPath");
        return textures.computeIfAbsent(assetPath, this::loadTextureSafely);
    }

    /**
     * Retrieves a {@link BitmapFont} for the provided asset path, returning a placeholder when unavailable.
     *
     * @param assetPath the font asset path
     * @return the cached {@link BitmapFont}
     */
    public BitmapFont getFont(final String assetPath) {
        Objects.requireNonNull(assetPath, "assetPath");
        return fonts.computeIfAbsent(assetPath, this::loadFontSafely);
    }

    /**
     * Retrieves a {@link Sound} for the provided asset path.
     *
     * @param assetPath the sound asset path
     * @return the cached {@link Sound} instance or a placeholder when missing
     */
    public Sound getSound(final String assetPath) {
        Objects.requireNonNull(assetPath, "assetPath");
        return sounds.computeIfAbsent(assetPath, this::loadSoundSafely);
    }

    /**
     * Retrieves a {@link Music} instance for the given asset path.
     *
     * @param assetPath the music asset path
     * @return the cached {@link Music} or a placeholder instance when unavailable
     */
    public Music getMusic(final String assetPath) {
        Objects.requireNonNull(assetPath, "assetPath");
        return music.computeIfAbsent(assetPath, this::loadMusicSafely);
    }

    /**
     * Provides the number of cached textures. Intended primarily for verification in tests.
     *
     * @return the cached texture count
     */
    public int getCachedTextureCount() {
        return textures.size();
    }

    /**
     * Provides the number of cached fonts.
     *
     * @return the cached font count
     */
    public int getCachedFontCount() {
        return fonts.size();
    }

    /**
     * Provides the number of cached sound effects.
     *
     * @return the cached sound count
     */
    public int getCachedSoundCount() {
        return sounds.size();
    }

    /**
     * Provides the number of cached music tracks.
     *
     * @return the cached music count
     */
    public int getCachedMusicCount() {
        return music.size();
    }

    /**
     * Clears all cached assets and disposes any native resources.
     */
    @Override
    public void dispose() {
        textures.values().forEach(this::disposeIfNecessary);
        fonts.values().forEach(this::disposeIfNecessary);
        sounds.values().forEach(this::disposeIfNecessary);
        music.values().forEach(this::disposeIfNecessary);
        textures.clear();
        fonts.clear();
        sounds.clear();
        music.clear();
    }

    private Texture loadTextureSafely(final String assetPath) {
        final FileHandle handle = resolveHandle(assetPath);
        if (handle == null) {
            return placeholderTexture;
        }
        try {
            return new Texture(handle);
        } catch (GdxRuntimeException ex) {
            Logger.warn("Failed to load texture {} - using placeholder.", assetPath);
            return placeholderTexture;
        }
    }

    private BitmapFont loadFontSafely(final String assetPath) {
        final FileHandle handle = resolveHandle(assetPath);
        if (handle == null) {
            return placeholderFont;
        }
        try {
            return new BitmapFont(handle);
        } catch (GdxRuntimeException ex) {
            Logger.warn("Failed to load font {} - using placeholder.", assetPath);
            return placeholderFont;
        }
    }

    private Sound loadSoundSafely(final String assetPath) {
        final FileHandle handle = resolveHandle(assetPath);
        if (handle == null) {
            return placeholderSound;
        }
        try {
            return Gdx.audio.newSound(handle);
        } catch (GdxRuntimeException ex) {
            Logger.warn("Failed to load sound {} - using placeholder.", assetPath);
            return placeholderSound;
        }
    }

    private Music loadMusicSafely(final String assetPath) {
        final FileHandle handle = resolveHandle(assetPath);
        if (handle == null) {
            return placeholderMusic;
        }
        try {
            return Gdx.audio.newMusic(handle);
        } catch (GdxRuntimeException ex) {
            Logger.warn("Failed to load music {} - using placeholder.", assetPath);
            return placeholderMusic;
        }
    }

    private FileHandle resolveHandle(final String assetPath) {
        if (assetPath.isBlank()) {
            return null;
        }
        if (Gdx.files == null) {
            Logger.warn("Attempted to resolve asset {} before files subsystem initialised.", assetPath);
            return null;
        }
        final FileHandle handle = Gdx.files.internal(assetPath);
        if (!handle.exists()) {
            Logger.warn("Asset {} missing - falling back to placeholder.", assetPath);
            return null;
        }
        return handle;
    }

    private void disposeIfNecessary(final Disposable disposable) {
        if (disposable == placeholderTexture || disposable == placeholderFont
                || disposable == placeholderSound || disposable == placeholderMusic) {
            return;
        }
        if (disposable != null) {
            disposable.dispose();
        }
    }

    private Texture createPlaceholderTexture() {
        try {
            final Pixmap pixmap = new Pixmap(2, 2, Pixmap.Format.RGBA8888);
            pixmap.setColor(Color.MAGENTA);
            pixmap.fill();
            pixmap.setColor(Color.BLACK);
            pixmap.drawLine(0, 0, 1, 1);
            pixmap.drawLine(1, 0, 0, 1);
            final Texture texture = new Texture(pixmap);
            pixmap.dispose();
            return texture;
        } catch (GdxRuntimeException | UnsatisfiedLinkError exception) {
            Logger.warn("Falling back to headless placeholder texture due to initialisation failure.");
            return new Texture(new HeadlessPlaceholderTextureData(2, 2, Pixmap.Format.RGBA8888));
        }
    }

    private static final class HeadlessPlaceholderTextureData implements TextureData {

        private final int width;
        private final int height;
        private final Pixmap.Format format;
        private boolean prepared;

        HeadlessPlaceholderTextureData(final int width, final int height, final Pixmap.Format format) {
            this.width = width;
            this.height = height;
            this.format = format;
        }

        @Override
        public TextureDataType getType() {
            return TextureDataType.Custom;
        }

        @Override
        public boolean isPrepared() {
            return prepared;
        }

        @Override
        public void prepare() {
            prepared = true;
        }

        @Override
        public void consumeCustomData(final int target) {
            if (!prepared) {
                throw new IllegalStateException("Texture data must be prepared before consumption.");
            }
            final ByteBuffer pixels = BufferUtils.newByteBuffer(width * height * 4);
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    final boolean drawCross = x == y || x + y == width - 1;
                    if (drawCross) {
                        pixels.put((byte) 0);
                        pixels.put((byte) 0);
                        pixels.put((byte) 0);
                    } else {
                        pixels.put((byte) 0xFF);
                        pixels.put((byte) 0);
                        pixels.put((byte) 0xFF);
                    }
                    pixels.put((byte) 0xFF);
                }
            }
            pixels.flip();
            Gdx.gl.glTexImage2D(target, 0, Pixmap.Format.toGlFormat(format), width, height, 0,
                Pixmap.Format.toGlFormat(format), Pixmap.Format.toGlType(format), pixels);
            prepared = false;
        }

        @Override
        public Pixmap consumePixmap() {
            throw new UnsupportedOperationException("Headless placeholder texture does not expose Pixmap data.");
        }

        @Override
        public boolean disposePixmap() {
            return false;
        }

        @Override
        public int getWidth() {
            return width;
        }

        @Override
        public int getHeight() {
            return height;
        }

        @Override
        public Format getFormat() {
            return format;
        }

        @Override
        public boolean useMipMaps() {
            return false;
        }

        @Override
        public boolean isManaged() {
            return false;
        }
    }

    private static final class NoOpSound implements Sound {

        @Override
        public long play() {
            return 0;
        }

        @Override
        public long play(final float volume) {
            return 0;
        }

        @Override
        public long play(final float volume, final float pitch, final float pan) {
            return 0;
        }

        @Override
        public long loop() {
            return 0;
        }

        @Override
        public long loop(final float volume) {
            return 0;
        }

        @Override
        public long loop(final float volume, final float pitch, final float pan) {
            return 0;
        }

        @Override
        public void stop() {
            // no-op
        }

        @Override
        public void pause() {
            // no-op
        }

        @Override
        public void resume() {
            // no-op
        }

        @Override
        public void dispose() {
            // no-op
        }

        @Override
        public void stop(final long soundId) {
            // no-op
        }

        @Override
        public void pause(final long soundId) {
            // no-op
        }

        @Override
        public void resume(final long soundId) {
            // no-op
        }

        @Override
        public void setLooping(final long soundId, final boolean looping) {
            // no-op
        }

        @Override
        public void setPitch(final long soundId, final float pitch) {
            // no-op
        }

        @Override
        public void setVolume(final long soundId, final float volume) {
            // no-op
        }

        @Override
        public void setPan(final long soundId, final float pan, final float volume) {
            // no-op
        }
    }

    private static final class NoOpMusic implements Music {

        private float volume = 1f;
        private boolean looping;

        @Override
        public void play() {
            // no-op
        }

        @Override
        public void pause() {
            // no-op
        }

        @Override
        public void stop() {
            // no-op
        }

        @Override
        public boolean isPlaying() {
            return false;
        }

        @Override
        public void setLooping(final boolean isLooping) {
            this.looping = isLooping;
        }

        @Override
        public boolean isLooping() {
            return looping;
        }

        @Override
        public void setVolume(final float volume) {
            this.volume = volume;
        }

        @Override
        public float getVolume() {
            return volume;
        }

        @Override
        public void setPan(final float pan, final float volume) {
            // no-op
        }

        @Override
        public void setPosition(final float position) {
            // no-op
        }

        @Override
        public float getPosition() {
            return 0f;
        }

        @Override
        public void dispose() {
            // no-op
        }

        @Override
        public void setOnCompletionListener(final OnCompletionListener listener) {
            // no-op
        }

    }
}
