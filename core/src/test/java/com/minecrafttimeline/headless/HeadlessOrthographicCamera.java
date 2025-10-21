package com.minecrafttimeline.headless;

import com.badlogic.gdx.graphics.OrthographicCamera;

/**
 * Orthographic camera variant that skips frustum updates to avoid native dependencies in headless tests.
 */
public final class HeadlessOrthographicCamera extends OrthographicCamera {

    public HeadlessOrthographicCamera() {
        super();
    }

    public HeadlessOrthographicCamera(final float viewportWidth, final float viewportHeight) {
        super(viewportWidth, viewportHeight);
    }

    @Override
    public void update() {
        super.update(false);
    }

    @Override
    public void update(final boolean updateFrustum) {
        super.update(false);
    }
}
