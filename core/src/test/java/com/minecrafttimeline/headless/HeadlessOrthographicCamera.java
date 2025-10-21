package com.minecrafttimeline.headless;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector3;

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

    @Override
    public Vector3 project(
            final Vector3 worldCoords,
            final float viewportX,
            final float viewportY,
            final float viewportWidth,
            final float viewportHeight) {
        final float zoomedWidth = this.viewportWidth * zoom;
        final float zoomedHeight = this.viewportHeight * zoom;
        final float left = position.x - zoomedWidth * 0.5f;
        final float bottom = position.y - zoomedHeight * 0.5f;

        final float worldX = worldCoords.x;
        final float worldY = worldCoords.y;
        final float worldZ = worldCoords.z;

        final float normalisedX = zoomedWidth == 0f ? 0f : (worldX - left) / zoomedWidth;
        final float normalisedY = zoomedHeight == 0f ? 0f : (worldY - bottom) / zoomedHeight;
        final float depth = far - near;

        worldCoords.x = viewportX + normalisedX * viewportWidth;
        worldCoords.y = viewportY + (1f - normalisedY) * viewportHeight;
        worldCoords.z = depth == 0f ? 0f : (worldZ - near) / depth;
        return worldCoords;
    }

    @Override
    public Vector3 unproject(
            final Vector3 screenCoords,
            final float viewportX,
            final float viewportY,
            final float viewportWidth,
            final float viewportHeight) {
        final float zoomedWidth = this.viewportWidth * zoom;
        final float zoomedHeight = this.viewportHeight * zoom;
        final float left = position.x - zoomedWidth * 0.5f;
        final float bottom = position.y - zoomedHeight * 0.5f;

        final float normalisedX = viewportWidth == 0f ? 0f : (screenCoords.x - viewportX) / viewportWidth;
        final float normalisedY = viewportHeight == 0f ? 0f : (screenCoords.y - viewportY) / viewportHeight;
        final float normalisedZ = screenCoords.z;
        final float depth = far - near;

        screenCoords.x = left + normalisedX * zoomedWidth;
        screenCoords.y = bottom + (1f - normalisedY) * zoomedHeight;
        screenCoords.z = near + normalisedZ * depth;
        return screenCoords;
    }
}
