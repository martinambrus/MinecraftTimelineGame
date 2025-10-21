package com.minecrafttimeline;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;

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
        final HeadlessApplicationConfiguration configuration = new HeadlessApplicationConfiguration();
        application = new HeadlessApplication(new ApplicationAdapter() {
            // no implementation required for tests
        }, configuration);
    }
}
