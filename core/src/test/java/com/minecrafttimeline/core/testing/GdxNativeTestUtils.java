package com.minecrafttimeline.core.testing;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.backends.headless.mock.graphics.MockGL20;
import com.badlogic.gdx.utils.GdxNativesLoader;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Utility helper to ensure libGDX native libraries are loaded once before tests execute.
 */
public final class GdxNativeTestUtils {

    private static final AtomicBoolean LOADED = new AtomicBoolean();
    private static final AtomicBoolean HEADLESS_INITIALIZED = new AtomicBoolean();
    private static HeadlessApplication headlessApplication;

    private GdxNativeTestUtils() {
        // utility class
    }

    /**
     * Loads the libGDX native libraries exactly once for the current JVM.
     */
    public static void loadNativesIfNeeded() {
        if (LOADED.compareAndSet(false, true)) {
            GdxNativesLoader.load();
        }
    }

    /**
     * Ensures a libGDX headless application is running so static {@code Gdx} services are available.
     * This is primarily required for tests that rely on {@code Gdx.graphics} during viewport updates.
     */
    public static void ensureHeadlessApplication() {
        loadNativesIfNeeded();
        if (HEADLESS_INITIALIZED.compareAndSet(false, true)) {
            final HeadlessApplicationConfiguration configuration = new HeadlessApplicationConfiguration();
            headlessApplication = new HeadlessApplication(new ApplicationAdapter() {}, configuration);
            final GL20 mockGl = new MockGL20();
            Gdx.gl = mockGl;
            Gdx.gl20 = mockGl;
        }
    }
}
