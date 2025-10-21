package com.minecrafttimeline.screen;

import static org.assertj.core.api.Assertions.assertThat;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Affine2;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.minecrafttimeline.TestApplicationSupport;
import com.minecrafttimeline.assets.AssetLoader;
import com.minecrafttimeline.cards.Card;
import com.minecrafttimeline.cards.CardDeck;
import com.minecrafttimeline.input.InputHandler;
import com.minecrafttimeline.render.CardRenderer;
import com.minecrafttimeline.headless.HeadlessOrthographicCamera;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link GameplayScreen} covering rendering, layout, and input integration.
 */
class GameplayScreenTest {

    private TrackingBatch spriteBatch;
    private GameplayScreen gameplayScreen;
    private InputHandler inputHandler;

    @BeforeAll
    static void initialiseHeadless() {
        TestApplicationSupport.initialise();
    }

    @BeforeEach
    void setUp() {
        AssetLoader.getInstance().dispose();
        spriteBatch = new TrackingBatch();
        inputHandler = new InputHandler();
        final Viewport viewport = new FitViewport(1920f, 1080f, new HeadlessOrthographicCamera(1920f, 1080f));
        viewport.update(1920, 1080, true);
        gameplayScreen = new GameplayScreen(createDeck(), inputHandler, spriteBatch, viewport);
        gameplayScreen.show();
    }

    @AfterEach
    void tearDown() {
        gameplayScreen.dispose();
        spriteBatch.dispose();
    }

    @Test
    void renderUpdatesBatchesAndRecordsFrameMetrics() {
        gameplayScreen.render(1f / 60f);

        assertThat(spriteBatch.beginCount).isEqualTo(1);
        assertThat(spriteBatch.endCount).isEqualTo(1);
        assertThat(gameplayScreen.getLastRenderTimeMillis()).isGreaterThanOrEqualTo(0f);
        assertThat(gameplayScreen.getLastMeasuredFps()).isGreaterThanOrEqualTo(0);
    }

    @Test
    void resizeInvalidatesLayoutAndAdjustsViewport() {
        gameplayScreen.resize(1280, 720);
        gameplayScreen.render(1f / 60f);

        assertThat(gameplayScreen.getViewport().getScreenWidth()).isEqualTo(1280);
        assertThat(gameplayScreen.getViewport().getScreenHeight()).isEqualTo(720);
    }

    @Test
    void inputHandlerIntegratesWithRendering() {
        // Allow interpolation to settle for stable bounds
        for (int i = 0; i < 5; i++) {
            gameplayScreen.render(1f / 60f);
        }
        final List<CardRenderer> handRenderers = gameplayScreen.getHandRenderers();
        assertThat(handRenderers).isNotEmpty();
        final CardRenderer first = handRenderers.get(0);
        final com.badlogic.gdx.math.Rectangle bounds = first.getCurrentBounds();

        final boolean selected = inputHandler.trackPointerDown(bounds.x + 5f, bounds.y + 5f);
        assertThat(selected).isTrue();
        assertThat(inputHandler.getSelectedCard()).isSameAs(first.getCard());

        inputHandler.trackPointerDrag(bounds.x + 25f, bounds.y + 30f);
        gameplayScreen.render(1f / 60f);

        final com.badlogic.gdx.math.Vector2 pointer = inputHandler.getPointerPosition();
        final com.badlogic.gdx.math.Vector2 offset = inputHandler.getDragOffset();
        final com.badlogic.gdx.math.Vector2 expectedTarget = pointer.cpy().sub(offset);
        assertThat(first.getTargetPosition()).isEqualTo(expectedTarget);

        inputHandler.trackPointerUp(pointer.x, pointer.y);
        gameplayScreen.render(1f / 60f);
        assertThat(inputHandler.getSelectedRenderer()).isNull();
    }

    @Test
    void debugRenderingDoesNotThrow() {
        gameplayScreen.setDebugEnabled(true);
        gameplayScreen.render(1f / 60f);
        gameplayScreen.render(1f / 60f);
    }

    private static CardDeck createDeck() {
        final List<Card> cards = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            cards.add(new Card(
                    "card-" + i,
                    "Card " + i,
                    LocalDate.of(2010, 1, 1).plusDays(i),
                    "Trivia " + i,
                    "libgdx.png",
                    "1." + i));
        }
        return new CardDeck(cards);
    }

    private static final class TrackingBatch implements Batch {

        private final Color color = new Color(Color.WHITE);
        private final Matrix4 projection = new Matrix4();
        private final Matrix4 transform = new Matrix4();
        private ShaderProgram shader;
        private boolean blendingEnabled = true;
        private boolean drawing;
        private int blendSrcFunc = -1;
        private int blendDstFunc = -1;
        private int blendSrcFuncAlpha = -1;
        private int blendDstFuncAlpha = -1;
        int beginCount;
        int endCount;

        @Override
        public void begin() {
            beginCount++;
            drawing = true;
        }

        @Override
        public void end() {
            endCount++;
            drawing = false;
        }

        @Override
        public void setColor(final Color tint) {
            color.set(tint);
        }

        @Override
        public void setColor(final float r, final float g, final float b, final float a) {
            color.set(r, g, b, a);
        }

        @Override
        public Color getColor() {
            return color;
        }

        @Override
        public void setPackedColor(final float packedColor) {
            Color.abgr8888ToColor(color, packedColor);
        }

        @Override
        public float getPackedColor() {
            return color.toFloatBits();
        }

        @Override
        public void draw(final Texture texture, final float x, final float y, final float originX, final float originY,
                final float width, final float height, final float scaleX, final float scaleY, final float rotation,
                final int srcX, final int srcY, final int srcWidth, final int srcHeight, final boolean flipX,
                final boolean flipY) {
            // no-op for headless verification
        }

        @Override
        public void draw(final Texture texture, final float x, final float y, final float width, final float height,
                final int srcX, final int srcY, final int srcWidth, final int srcHeight, final boolean flipX,
                final boolean flipY) {
            // no-op
        }

        @Override
        public void draw(final Texture texture, final float x, final float y, final int srcX, final int srcY,
                final int srcWidth, final int srcHeight) {
            // no-op
        }

        @Override
        public void draw(final Texture texture, final float x, final float y, final float width, final float height,
                final float u, final float v, final float u2, final float v2) {
            // no-op
        }

        @Override
        public void draw(final Texture texture, final float x, final float y) {
            // no-op
        }

        @Override
        public void draw(final Texture texture, final float x, final float y, final float width, final float height) {
            // no-op
        }

        @Override
        public void draw(final Texture texture, final float[] spriteVertices, final int offset, final int count) {
            // no-op
        }

        @Override
        public void draw(final TextureRegion region, final float x, final float y) {
            // no-op
        }

        @Override
        public void draw(final TextureRegion region, final float x, final float y, final float width, final float height) {
            // no-op
        }

        @Override
        public void draw(final TextureRegion region, final float x, final float y, final float originX, final float originY,
                final float width, final float height, final float scaleX, final float scaleY, final float rotation) {
            // no-op
        }

        @Override
        public void draw(final TextureRegion region, final float x, final float y, final float originX, final float originY,
                final float width, final float height, final float scaleX, final float scaleY, final float rotation,
                final boolean clockwise) {
            // no-op
        }

        @Override
        public void draw(final TextureRegion region, final float width, final float height, final Affine2 transform) {
            // no-op
        }

        @Override
        public void flush() {
            // no-op
        }

        @Override
        public void disableBlending() {
            blendingEnabled = false;
        }

        @Override
        public void enableBlending() {
            blendingEnabled = true;
        }

        @Override
        public void setBlendFunction(final int srcFunc, final int dstFunc) {
            blendSrcFunc = srcFunc;
            blendDstFunc = dstFunc;
        }

        @Override
        public void setBlendFunctionSeparate(final int srcFuncColor, final int dstFuncColor, final int srcFuncAlpha,
                final int dstFuncAlpha) {
            blendSrcFunc = srcFuncColor;
            blendDstFunc = dstFuncColor;
            blendSrcFuncAlpha = srcFuncAlpha;
            blendDstFuncAlpha = dstFuncAlpha;
        }

        @Override
        public int getBlendSrcFunc() {
            return blendSrcFunc;
        }

        @Override
        public int getBlendDstFunc() {
            return blendDstFunc;
        }

        @Override
        public int getBlendSrcFuncAlpha() {
            return blendSrcFuncAlpha;
        }

        @Override
        public int getBlendDstFuncAlpha() {
            return blendDstFuncAlpha;
        }

        @Override
        public Matrix4 getProjectionMatrix() {
            return projection;
        }

        @Override
        public Matrix4 getTransformMatrix() {
            return transform;
        }

        @Override
        public void setProjectionMatrix(final Matrix4 projection) {
            this.projection.set(projection);
        }

        @Override
        public void setTransformMatrix(final Matrix4 transform) {
            this.transform.set(transform);
        }

        @Override
        public void setShader(final ShaderProgram shader) {
            this.shader = shader;
        }

        @Override
        public ShaderProgram getShader() {
            return shader;
        }

        @Override
        public boolean isBlendingEnabled() {
            return blendingEnabled;
        }

        @Override
        public boolean isDrawing() {
            return drawing;
        }

        @Override
        public void dispose() {
            // nothing to release
        }
    }
}
