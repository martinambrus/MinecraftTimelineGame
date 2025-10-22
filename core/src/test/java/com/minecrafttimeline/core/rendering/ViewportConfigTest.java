package com.minecrafttimeline.core.rendering;

import static org.assertj.core.api.Assertions.assertThat;

import com.badlogic.gdx.math.Vector2;
import com.minecrafttimeline.core.testing.GdxNativeTestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Validates the coordinate conversions handled by {@link ViewportConfig}.
 */
class ViewportConfigTest {

    private ViewportConfig viewportConfig;

    @BeforeAll
    static void bootstrapGdx() {
        GdxNativeTestUtils.ensureHeadlessApplication();
    }

    @BeforeEach
    void setUp() {
        viewportConfig = new ViewportConfig();
        viewportConfig.update(1280, 720);
    }

    @Test
    void worldToScreenAndBackReturnsOriginalPoint() {
        final Vector2 screen = viewportConfig.worldToScreenCoordinates(400f, 300f);
        final Vector2 world = viewportConfig.screenToWorldCoordinates((int) screen.x, (int) screen.y);
        assertThat(world.x).isCloseTo(400f, org.assertj.core.data.Offset.offset(0.1f));
        assertThat(world.y).isCloseTo(300f, org.assertj.core.data.Offset.offset(0.1f));
    }

    @Test
    void aspectRatioMaintainedWhenResized() {
        viewportConfig.update(1920, 1080);
        assertThat(viewportConfig.getViewport().getWorldWidth()).isEqualTo(ViewportConfig.BASE_WIDTH);
        assertThat(viewportConfig.getViewport().getWorldHeight()).isEqualTo(ViewportConfig.BASE_HEIGHT);
    }

    @Test
    void screenToWorldCoordinatesRespectOrigin() {
        final Vector2 world = viewportConfig.screenToWorldCoordinates(0, 0);
        assertThat(world.x).isCloseTo(0f, org.assertj.core.data.Offset.offset(0.1f));
        assertThat(world.y).isCloseTo(0f, org.assertj.core.data.Offset.offset(0.1f));
    }
}
