package com.minecrafttimeline.core.rendering;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

/**
 * Encapsulates viewport configuration for the gameplay screen, ensuring responsive scaling across resolutions.
 */
public class ViewportConfig {

    /** Base world width in units. */
    public static final float BASE_WIDTH = 1280f;
    /** Base world height in units. */
    public static final float BASE_HEIGHT = 720f;

    private final OrthographicCamera camera;
    private final ExtendViewport viewport;
    private final Vector3 tempVec3 = new Vector3();
    private final Vector2 reusableWorld = new Vector2();
    private final Vector2 reusableScreen = new Vector2();

    /**
     * Creates a viewport configured with a 1280x720 orthographic camera.
     */
    public ViewportConfig() {
        camera = new OrthographicCamera();
        viewport = new ExtendViewport(BASE_WIDTH, BASE_HEIGHT, camera);
        camera.setToOrtho(false, BASE_WIDTH, BASE_HEIGHT);
        camera.update();
    }

    /**
     * Applies viewport updates when the window is resized.
     *
     * @param width  new pixel width
     * @param height new pixel height
     */
    public void update(final int width, final int height) {
        viewport.update(width, height, true);
    }

    /**
     * Converts screen coordinates (origin at top-left) to world coordinates (origin at bottom-left).
     *
     * @param screenX screen-space x coordinate
     * @param screenY screen-space y coordinate
     * @param out     vector to store the result; must not be {@code null}
     * @return {@code out} for chaining
     */
    public Vector2 screenToWorldCoordinates(final int screenX, final int screenY, final Vector2 out) {
        final float adjustedX = screenX - viewport.getScreenX();
        final float adjustedY = viewport.getScreenY() + viewport.getScreenHeight() - screenY;
        tempVec3.set(adjustedX, adjustedY, 0f);
        viewport.unproject(tempVec3);
        out.set(tempVec3.x, tempVec3.y);
        return out;
    }

    /**
     * Converts screen coordinates to world coordinates using an internal reusable vector.
     *
     * @param screenX screen-space x coordinate
     * @param screenY screen-space y coordinate
     * @return mutable vector containing world coordinates
     */
    public Vector2 screenToWorldCoordinates(final int screenX, final int screenY) {
        return screenToWorldCoordinates(screenX, screenY, reusableWorld);
    }

    /**
     * Projects world coordinates back into screen space, mainly for testing and UI alignment.
     *
     * @param worldX world x coordinate
     * @param worldY world y coordinate
     * @return mutable vector containing projected screen coordinates
     */
    public Vector2 worldToScreenCoordinates(final float worldX, final float worldY) {
        tempVec3.set(worldX, worldY, 0f);
        viewport.project(tempVec3);
        final float projectedX = tempVec3.x + viewport.getScreenX();
        final float projectedY = viewport.getScreenY() + viewport.getScreenHeight() - tempVec3.y;
        reusableScreen.set(projectedX, projectedY);
        return reusableScreen;
    }

    /**
     * Retrieves the configured {@link OrthographicCamera}.
     *
     * @return camera reference
     */
    public OrthographicCamera getCamera() {
        return camera;
    }

    /**
     * Retrieves the configured {@link Viewport} implementation.
     *
     * @return extend viewport instance
     */
    public Viewport getViewport() {
        return viewport;
    }
}
