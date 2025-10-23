package com.minecrafttimeline.core.rendering;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Verifies the numerical properties of {@link EasingFunctions}.
 */
class EasingFunctionsTest {

    @Test
    void linearMatchesInput() {
        assertThat(EasingFunctions.linear(0f)).isZero();
        assertThat(EasingFunctions.linear(0.5f)).isEqualTo(0.5f);
        assertThat(EasingFunctions.linear(1f)).isEqualTo(1f);
    }

    @Test
    void easeInQuadAccelerates() {
        final float quarter = EasingFunctions.easeInQuad(0.25f);
        final float half = EasingFunctions.easeInQuad(0.5f);
        assertThat(quarter).isLessThan(0.25f);
        assertThat(half).isLessThan(0.5f);
        assertThat(EasingFunctions.easeInQuad(0f)).isZero();
        assertThat(EasingFunctions.easeInQuad(1f)).isEqualTo(1f);
    }

    @Test
    void easeOutQuadDecelerates() {
        final float quarter = EasingFunctions.easeOutQuad(0.25f);
        final float half = EasingFunctions.easeOutQuad(0.5f);
        final float threeQuarter = EasingFunctions.easeOutQuad(0.75f);
        assertThat(quarter).isGreaterThan(0.25f);
        assertThat(half).isEqualTo(0.75f);
        assertThat(threeQuarter).isGreaterThan(0.9f);
    }

    @Test
    void easeInOutQuadIsSymmetric() {
        final float low = EasingFunctions.easeInOutQuad(0.25f);
        final float high = EasingFunctions.easeInOutQuad(0.75f);
        assertThat(low).isLessThan(0.25f);
        assertThat(high).isGreaterThan(0.75f);
        assertThat(low).isEqualTo(1f - high);
    }

    @Test
    void easeOutCubicProvidesBounce() {
        final float quarter = EasingFunctions.easeOutCubic(0.25f);
        final float half = EasingFunctions.easeOutCubic(0.5f);
        assertThat(quarter).isGreaterThan(0.25f);
        assertThat(half).isGreaterThan(0.5f);
        assertThat(EasingFunctions.easeOutCubic(1f)).isEqualTo(1f);
    }

    @Test
    void allEasingFunctionsRemainWithinBounds() {
        final float[] inputs = { -0.5f, 0f, 0.25f, 0.5f, 0.75f, 1f, 1.5f };
        for (float value : inputs) {
            assertThat(EasingFunctions.linear(value)).isBetween(0f, 1f);
            assertThat(EasingFunctions.easeInQuad(value)).isBetween(0f, 1f);
            assertThat(EasingFunctions.easeOutQuad(value)).isBetween(0f, 1f);
            assertThat(EasingFunctions.easeInOutQuad(value)).isBetween(0f, 1f);
            assertThat(EasingFunctions.easeOutCubic(value)).isBetween(0f, 1f);
        }
    }
}
