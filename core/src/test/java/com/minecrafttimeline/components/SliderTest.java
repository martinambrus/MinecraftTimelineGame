package com.minecrafttimeline.components;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Tests for {@link Slider} ensuring value bounds and callbacks operate correctly.
 */
class SliderTest {

    @Test
    void initialValueRespectsBounds() {
        final Slider slider = new Slider(0f, 0f, 200f, 20f, 0f, 100f, 50f);
        assertThat(slider.getValue()).isEqualTo(50f);
    }

    @Test
    void setValueClampsToBounds() {
        final Slider slider = new Slider(0f, 0f, 200f, 20f, 0f, 100f, 50f);
        slider.setValue(500f);
        assertThat(slider.getValue()).isEqualTo(100f);
        slider.setValue(-10f);
        assertThat(slider.getValue()).isEqualTo(0f);
    }

    @Test
    void onMouseDraggedUpdatesValue() {
        final Slider slider = new Slider(0f, 0f, 200f, 20f, 0f, 100f, 0f);
        slider.onMouseDragged(100f);
        assertThat(slider.getValue()).isEqualTo(50f);
    }

    @Test
    void changeCallbackInvoked() {
        final Slider slider = new Slider(0f, 0f, 200f, 20f, 0f, 100f, 0f);
        final float[] captured = new float[1];
        slider.setOnChange(value -> captured[0] = value);
        slider.setValue(40f);
        assertThat(captured[0]).isEqualTo(40f);
    }
}
