package com.minecrafttimeline.components;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link Button} verifying hit detection and callbacks.
 */
class ButtonTest {

    @Test
    void containsShouldReturnTrueInsideBounds() {
        final Button button = new Button(100f, 50f, 200f, 60f, "Test");
        assertThat(button.contains(150f, 80f)).isTrue();
    }

    @Test
    void containsShouldReturnFalseOutsideBounds() {
        final Button button = new Button(10f, 10f, 100f, 40f, "Test");
        assertThat(button.contains(200f, 200f)).isFalse();
    }

    @Test
    void onClickShouldInvokeCallback() {
        final Button button = new Button(0f, 0f, 100f, 40f, "Click");
        final boolean[] invoked = new boolean[1];
        button.setOnClick(() -> invoked[0] = true);

        button.onMouseMoved(10f, 10f);
        button.onMouseDown(10f, 10f);
        button.onMouseUp();

        assertThat(invoked[0]).isTrue();
    }

    @Test
    void hoverAndPressStatesUpdateCorrectly() {
        final Button button = new Button(0f, 0f, 120f, 40f, "Hover");
        button.onMouseMoved(10f, 10f);
        assertThat(button.isHovered()).isTrue();
        button.onMouseDown(10f, 10f);
        assertThat(button.isPressed()).isTrue();
        button.onMouseMoved(200f, 200f);
        assertThat(button.isHovered()).isFalse();
        assertThat(button.isPressed()).isFalse();
    }
}
