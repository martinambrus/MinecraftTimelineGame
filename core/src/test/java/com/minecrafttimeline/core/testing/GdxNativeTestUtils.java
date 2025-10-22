package com.minecrafttimeline.core.testing;

import com.badlogic.gdx.utils.GdxNativesLoader;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Utility helper to ensure libGDX native libraries are loaded once before tests execute.
 */
public final class GdxNativeTestUtils {

    private static final AtomicBoolean LOADED = new AtomicBoolean();

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
}
