package com.minecrafttimeline;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.utils.GdxNativesLoader;
import com.minecrafttimeline.headless.HeadlessMockGL20;

/**
 * Utility used by tests to bootstrap the LibGDX headless environment once.
 */
public final class TestApplicationSupport {

    private static HeadlessApplication application;

    private TestApplicationSupport() {
        // utility class
    }

    /**
     * Initialises the headless LibGDX backend when required.
     */
    public static synchronized void initialise() {
        if (application != null) {
            return;
        }
        GdxNativesLoader.disableNativesLoading = true;
        final HeadlessApplicationConfiguration configuration = new HeadlessApplicationConfiguration();
        application = new HeadlessApplication(new ApplicationAdapter() {
            // no implementation required for tests
        }, configuration);
        final GL20 mockGL20 = new HeadlessMockGL20();
        Gdx.gl20 = mockGL20;
        Gdx.gl = mockGL20;
    }
}
