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
        update(true);
    }

    @Override
    public void update(final boolean updateFrustum) {
        if (!updateFrustum) {
            super.update(false);
            invProjectionView.set(combined).inv();
            return;
        }
        try {
            super.update(true);
        } catch (final UnsatisfiedLinkError error) {
            super.update(false);
            invProjectionView.set(combined).inv();
        }
    }
}
