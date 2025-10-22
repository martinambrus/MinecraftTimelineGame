package com.minecrafttimeline.core.util;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.utils.GdxRuntimeException;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Centralized asset loader backed by libGDX {@link AssetManager} with caching and graceful fallbacks.
 */
public final class AssetLoader {

    private static final AssetLoader INSTANCE = new AssetLoader();
    private static final String ASSETS_ROOT = "";

    private final Lock lock = new ReentrantLock();
    private AssetManager assetManager;
    private Texture placeholderTexture;
    private BitmapFont defaultFont;
    private volatile boolean initialized;
    private final Set<String> missingAssets = new HashSet<>();

    private AssetLoader() {
        // Singleton
    }

    /**
     * Retrieves the global {@link AssetLoader} instance.
     *
     * @return singleton loader
     */
    public static AssetLoader getInstance() {
        return INSTANCE;
    }

    /**
     * Initializes the loader and prepares the underlying {@link AssetManager}. This method is thread-safe and idempotent.
     */
    public void initialize() {
        initializeInternal(new AssetManager(createResolver()));
    }

    private FileHandleResolver createResolver() {
        return new InternalFileHandleResolver();
    }

    /**
     * Visible for testing to inject a custom {@link AssetManager} without touching global state.
     */
    public void initializeWithManager(final AssetManager manager) {
        initializeInternal(manager);
    }

    private void initializeInternal(final AssetManager manager) {
        Objects.requireNonNull(manager, "manager must not be null");
        lock.lock();
        try {
            if (initialized) {
                return;
            }
            assetManager = manager;
            initialized = true;
            missingAssets.clear();
        } finally {
            lock.unlock();
        }
    }

    private void ensureInitialized() {
        if (!initialized) {
            throw new IllegalStateException("AssetLoader has not been initialized. Call initialize() first.");
        }
    }

    /**
     * Retrieves (and caches) a texture residing within the {@code core/assets} directory.
     *
     * @param path relative asset path
     * @return cached {@link Texture}
     */
    public Texture getTexture(final String path) {
        ensureInitialized();
        if (path == null || path.isEmpty()) {
            Logger.error("Requested texture with null/empty path");
            return obtainPlaceholderTexture();
        }
        final String resolvedPath = resolvePath(path);
        if (missingAssets.contains(resolvedPath)) {
            return obtainPlaceholderTexture();
        }
        lock.lock();
        try {
            if (!assetManager.isLoaded(resolvedPath, Texture.class)) {
                try {
                    assetManager.load(resolvedPath, Texture.class);
                    assetManager.finishLoadingAsset(resolvedPath);
                } catch (final GdxRuntimeException exception) {
                    Logger.error("Failed to load texture: " + resolvedPath + ", using placeholder.", exception);
                    safeUnload(resolvedPath);
                    missingAssets.add(resolvedPath);
                    return obtainPlaceholderTexture();
                }
            }
            return assetManager.get(resolvedPath, Texture.class);
        } finally {
            lock.unlock();
        }
    }

    private void safeUnload(final String resolvedPath) {
        if (assetManager.isLoaded(resolvedPath)) {
            assetManager.unload(resolvedPath);
        }
    }

    /**
     * Creates a fresh {@link Sprite} instance for the specified texture path.
     *
     * @param path relative asset path
     * @return new sprite instance
     */
    public Sprite getSprite(final String path) {
        final Texture texture = getTexture(path);
        return new Sprite(texture);
    }

    /**
     * Retrieves (and caches) bitmap fonts from the {@code core/assets} directory.
     *
     * @param path relative asset path
     * @return cached {@link BitmapFont}
     */
    public BitmapFont getBitmapFont(final String path) {
        ensureInitialized();
        if (path == null || path.isEmpty()) {
            Logger.error("Requested bitmap font with null/empty path");
            return obtainDefaultFont();
        }
        final String resolvedPath = resolvePath(path);
        if (missingAssets.contains(resolvedPath)) {
            return obtainDefaultFont();
        }
        lock.lock();
        try {
            if (!assetManager.isLoaded(resolvedPath, BitmapFont.class)) {
                try {
                    assetManager.load(resolvedPath, BitmapFont.class);
                    assetManager.finishLoadingAsset(resolvedPath);
                } catch (final GdxRuntimeException exception) {
                    Logger.error("Failed to load bitmap font: " + resolvedPath + ", falling back to default font.",
                            exception);
                    safeUnload(resolvedPath);
                    missingAssets.add(resolvedPath);
                    return obtainDefaultFont();
                }
            }
            return assetManager.get(resolvedPath, BitmapFont.class);
        } finally {
            lock.unlock();
        }
    }

    private String resolvePath(final String path) {
        return ASSETS_ROOT + path;
    }

    private Texture obtainPlaceholderTexture() {
        lock.lock();
        try {
            if (placeholderTexture == null) {
                placeholderTexture = createPlaceholderTexture();
            }
            return placeholderTexture;
        } finally {
            lock.unlock();
        }
    }

    private Texture createPlaceholderTexture() {
        try {
            final Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
            pixmap.setColor(1f, 1f, 1f, 1f);
            pixmap.fill();
            final Texture texture = new Texture(pixmap);
            pixmap.dispose();
            return texture;
        } catch (final Throwable throwable) {
            Logger.error("Unable to create placeholder texture; returning mocked instance.", throwable);
            return new Texture(1, 1, Pixmap.Format.RGBA8888);
        }
    }

    private BitmapFont obtainDefaultFont() {
        lock.lock();
        try {
            if (defaultFont == null) {
                defaultFont = new BitmapFont();
            }
            return defaultFont;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Disposes all cached resources and resets the loader state.
     */
    public void dispose() {
        lock.lock();
        try {
            if (assetManager != null) {
                assetManager.dispose();
            }
            if (placeholderTexture != null) {
                placeholderTexture.dispose();
                placeholderTexture = null;
            }
            if (defaultFont != null) {
                defaultFont.dispose();
                defaultFont = null;
            }
            missingAssets.clear();
            initialized = false;
            assetManager = null;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Visible for testing so unit tests can inject deterministic placeholder textures.
     */
    public void setPlaceholderTexture(final Texture texture) {
        lock.lock();
        try {
            placeholderTexture = texture;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Clears all cached resources; exposed for tests that require a pristine loader state.
     */
    public void resetForTesting() {
        dispose();
    }
}
