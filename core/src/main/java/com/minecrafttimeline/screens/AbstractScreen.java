package com.minecrafttimeline.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.minecrafttimeline.core.input.InputHandler;
import java.util.Objects;

/**
 * Base implementation for all Minecraft Timeline screens providing shared rendering and lifecycle handling.
 *
 * <p>
 * The class establishes a consistent viewport/camera pair and coordinates the canonical render pipeline:
 *
 * <pre>
 * +-------------+    +------------------+    +------------------+
 * | handleInput | -> | updateLogic(delta)| -> | renderScreen(...)|
 * +-------------+    +------------------+    +------------------+
 * </pre>
 *
 * Subclasses are expected to populate UI elements inside {@link #buildUI()}, react to user input in
 * {@link #handleInput()}, update domain state within {@link #updateLogic(float)} and perform drawing inside
 * {@link #renderScreen(float)}.
 */
public abstract class AbstractScreen implements Screen {

    /** Default world width used by non-gameplay screens. */
    protected static final float DEFAULT_WORLD_WIDTH = 1280f;
    /** Default world height used by non-gameplay screens. */
    protected static final float DEFAULT_WORLD_HEIGHT = 720f;

    /** Reference to the root libGDX game. */
    protected final Game game;
    /** Shared sprite batch used for rendering. */
    protected final SpriteBatch batch;
    /** Camera controlling the projection matrix. Subclasses may override for specialised setups. */
    protected OrthographicCamera camera;
    /** Viewport describing how the camera maps to the window. Subclasses may override for specialised setups. */
    protected Viewport viewport;
    /** Optional input manager used by complex screens (gameplay). */
    protected InputHandler inputManager;

    private final boolean ownsBatch;
    private final Vector3 unprojectVector = new Vector3();

    /** Background colour applied before rendering the screen contents. */
    protected final Color backgroundColor = new Color(0.02f, 0.02f, 0.04f, 1f);

    /**
     * Creates a new screen bound to the supplied game instance.
     *
     * @param game owning game; must not be {@code null}
     */
    protected AbstractScreen(final Game game) {
        this(game, null, null);
    }

    /**
     * Creates a new screen bound to the supplied game using the provided camera and viewport.
     *
     * @param game     owning game; must not be {@code null}
     * @param camera   camera to use; may be {@code null} to create a default camera
     * @param viewport viewport to use; may be {@code null} to create a default viewport around the supplied camera
     */
    protected AbstractScreen(final Game game, final OrthographicCamera camera, final Viewport viewport) {
        this.game = Objects.requireNonNull(game, "game must not be null");
        SpriteBatch resolvedBatch = null;
        boolean owns = false;
        if (game instanceof SpriteBatchProvider provider) {
            resolvedBatch = provider.getSharedSpriteBatch();
        }
        if (resolvedBatch == null) {
            resolvedBatch = new SpriteBatch();
            owns = true;
        }
        batch = resolvedBatch;
        ownsBatch = owns;
        if (camera != null) {
            this.camera = camera;
        } else {
            this.camera = new OrthographicCamera();
        }
        if (viewport != null) {
            this.viewport = viewport;
        } else {
            this.viewport = new ExtendViewport(DEFAULT_WORLD_WIDTH, DEFAULT_WORLD_HEIGHT, this.camera);
        }
    }

    /**
     * Retrieves the current mouse/touch position in world units.
     *
     * @return mutable vector containing the world coordinates
     */
    protected Vector3 getWorldCursorPosition() {
        unprojectVector.set(Gdx.input.getX(), Gdx.input.getY(), 0f);
        viewport.unproject(unprojectVector);
        return unprojectVector;
    }

    /** {@inheritDoc} */
    @Override
    public void show() {
        buildUI();
    }

    /**
     * Builds the UI elements for the screen. Called exactly once during {@link #show()}.
     */
    protected abstract void buildUI();

    /**
     * Processes input each frame. Subclasses should query {@link #getWorldCursorPosition()} for cursor location.
     */
    protected abstract void handleInput();

    /**
     * Updates internal logic/state prior to rendering.
     *
     * @param delta time elapsed since the previous frame
     */
    protected abstract void updateLogic(float delta);

    /**
     * Performs custom drawing for the screen. Called while the sprite batch is active.
     *
     * @param delta time elapsed since the previous frame
     */
    protected abstract void renderScreen(float delta);

    /** {@inheritDoc} */
    @Override
    public void render(final float delta) {
        handleInput();
        updateLogic(delta);

        Gdx.gl.glClearColor(backgroundColor.r, backgroundColor.g, backgroundColor.b, backgroundColor.a);
        Gdx.gl.glClear(com.badlogic.gdx.graphics.GL20.GL_COLOR_BUFFER_BIT);

        viewport.apply();
        if (camera != null) {
            batch.setProjectionMatrix(camera.combined);
        }

        batch.begin();
        renderScreen(delta);
        batch.end();
    }

    /** {@inheritDoc} */
    @Override
    public void resize(final int width, final int height) {
        if (viewport != null) {
            viewport.update(width, height, true);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void pause() {
        // Default no-op. Override in subclasses when required.
    }

    /** {@inheritDoc} */
    @Override
    public void resume() {
        // Default no-op. Override in subclasses when required.
    }

    /** {@inheritDoc} */
    @Override
    public void hide() {
        if (Gdx.input.getInputProcessor() == inputManager) {
            Gdx.input.setInputProcessor(null);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void dispose() {
        if (ownsBatch && batch != null) {
            batch.dispose();
        }
    }

    /**
     * Requests that the game navigates to another screen type.
     *
     * @param screenClass target screen class; must not be {@code null}
     */
    protected void setScreen(final Class<? extends Screen> screenClass) {
        Objects.requireNonNull(screenClass, "screenClass must not be null");
        if (game instanceof ScreenManagedGame managedGame) {
            managedGame.getScreenManager().switchTo(screenClass);
            return;
        }
        game.setScreen(instantiateScreen(screenClass));
    }

    private Screen instantiateScreen(final Class<? extends Screen> screenClass) {
        try {
            try {
                return screenClass.getConstructor(Game.class).newInstance(game);
            } catch (final NoSuchMethodException exception) {
                // Fallback to zero-arg constructor if no Game-based constructor exists.
                return screenClass.getConstructor().newInstance();
            }
        } catch (final ReflectiveOperationException exception) {
            throw new IllegalStateException("Unable to instantiate screen: " + screenClass.getName(), exception);
        }
    }
}
