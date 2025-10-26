package com.minecrafttimeline.core.rendering;

import com.badlogic.gdx.Gdx;
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
    private int screenHeight = (int) BASE_HEIGHT;
    private boolean debugLogging = false;

    /**
     * Creates a viewport configured with a 1280x720 orthographic camera.
     * Uses Y-up coordinate system (Y=0 at bottom, Y=720 at top).
     */
    public ViewportConfig() {
        camera = new OrthographicCamera();
        camera.setToOrtho(false, BASE_WIDTH, BASE_HEIGHT);
        viewport = new ExtendViewport(BASE_WIDTH, BASE_HEIGHT, camera);
    }

    /**
     * Applies viewport updates when the window is resized.
     *
     * @param width  new pixel width
     * @param height new pixel height
     */
    public void update(final int width, final int height) {
        final int safeWidth = Math.max(1, width);
        final int safeHeight = Math.max(1, height);
        viewport.update(safeWidth, safeHeight, true);
        if (height > 0) {
            screenHeight = height;
        } else if (screenHeight <= 0) {
            screenHeight = (int) BASE_HEIGHT;
        }
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
        if (debugLogging) {
            Gdx.app.log("ViewportConfig", String.format(
                    "screenToWorld: input=(%d,%d) screenHeight=%d",
                    screenX, screenY, screenHeight));
        }

        final int effectiveHeight = screenHeight > 0 ? screenHeight : Math.max(1, viewport.getScreenHeight());
        final float flippedY = effectiveHeight - screenY;

        tempVec3.set(screenX, flippedY, 0f);
        viewport.unproject(tempVec3);

        if (debugLogging) {
            Gdx.app.log("ViewportConfig", String.format(
                    "  after unproject: (%.2f,%.2f)",
                    tempVec3.x, tempVec3.y));
        }

        // Viewport unproject already handles the coordinate system correctly
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
        // Project world coords - world Y and LibGDX screen Y are both bottom-origin
        tempVec3.set(worldX, worldY, 0f);
        viewport.project(tempVec3);

        // LibGDX returns screen Y=0 at bottom, flip to Y=0 at top (standard screen coords)
        final int effectiveHeight = screenHeight > 0 ? screenHeight : Math.max(1, viewport.getScreenHeight());
        final float topLeftY = effectiveHeight - tempVec3.y;
        reusableScreen.set(tempVec3.x, topLeftY);
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

    /**
     * Enables or disables debug logging for coordinate transformations.
     *
     * @param enabled true to enable debug logging
     */
    public void setDebugLogging(final boolean enabled) {
        debugLogging = enabled;
        Gdx.app.log("ViewportConfig", "Debug logging " + (debugLogging ? "enabled" : "disabled"));
    }

    /**
     * Checks if debug logging is currently enabled.
     *
     * @return true if debug logging is enabled
     */
    public boolean isDebugLoggingEnabled() {
        return debugLogging;
    }
}
