package com.minecrafttimeline.core.rendering;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;

/**
 * Validates the behavior of {@link CardAnimation}.
 */
class CardAnimationTest {

    @Test
    void initialValueMatchesStart() {
        final CardAnimation animation = new CardAnimation(0f, 10f, 1f, EasingFunctions::linear);
        assertThat(animation.getCurrentValue()).isEqualTo(0f);
        assertThat(animation.getProgress()).isEqualTo(0f);
    }

    @Test
    void progressesTowardEndValue() {
        final CardAnimation animation = new CardAnimation(0f, 10f, 1f, EasingFunctions::linear);
        animation.update(0.5f);
        assertThat(animation.getCurrentValue()).isCloseTo(5f, withinTolerance());
        assertThat(animation.getProgress()).isCloseTo(0.5f, withinTolerance());
    }

    @Test
    void clampsAtEndValue() {
        final CardAnimation animation = new CardAnimation(0f, 10f, 1f, EasingFunctions::linear);
        animation.update(2f);
        assertThat(animation.getCurrentValue()).isEqualTo(10f);
        assertThat(animation.isDone()).isTrue();
    }

    @Test
    void completionCallbackInvokedOnce() {
        final AtomicBoolean invoked = new AtomicBoolean(false);
        final CardAnimation animation = new CardAnimation(0f, 10f, 1f, EasingFunctions::linear);
        animation.setOnComplete(() -> invoked.set(true));
        animation.update(1f);
        animation.invokeCompletionIfNeeded();
        animation.invokeCompletionIfNeeded();
        assertThat(invoked).isTrue();
    }

    @Test
    void easingFunctionAffectsCurve() {
        final CardAnimation linear = new CardAnimation(0f, 10f, 1f, EasingFunctions::linear);
        final CardAnimation eased = new CardAnimation(0f, 10f, 1f, EasingFunctions::easeInQuad);
        linear.update(0.5f);
        eased.update(0.5f);
        assertThat(eased.getCurrentValue()).isLessThan(linear.getCurrentValue());
    }

    @Test
    void typeCanBeUpdated() {
        final CardAnimation animation = new CardAnimation(0f, 10f, 1f, EasingFunctions::linear);
        assertThat(animation.getType()).isEqualTo(AnimationType.DRAG);
        animation.setType(AnimationType.PLACE);
        assertThat(animation.getType()).isEqualTo(AnimationType.PLACE);
    }

    private org.assertj.core.data.Offset<Float> withinTolerance() {
        return org.assertj.core.data.Offset.offset(0.001f);
    }
}
