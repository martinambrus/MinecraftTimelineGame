package com.minecrafttimeline.core.rendering;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests the orchestration logic of {@link AnimationManager}.
 */
class AnimationManagerTest {

    private AnimationManager manager;

    @BeforeEach
    void setUp() {
        manager = new AnimationManager();
    }

    @Test
    void addAnimationIncreasesCount() {
        manager.addAnimation(new CardAnimation(0f, 1f, 1f, EasingFunctions::linear));
        assertThat(manager.getActiveCount()).isEqualTo(1);
        assertThat(manager.hasAnimationsRunning()).isTrue();
    }

    @Test
    void updateAdvancesAnimation() {
        final CardAnimation animation = new CardAnimation(0f, 1f, 1f, EasingFunctions::linear);
        manager.addAnimation(animation);
        manager.update(0.25f);
        assertThat(animation.getProgress()).isCloseTo(0.25f, withinTolerance());
    }

    @Test
    void completedAnimationsAreRemoved() {
        manager.addAnimation(new CardAnimation(0f, 1f, 0.1f, EasingFunctions::linear));
        manager.update(0.5f);
        assertThat(manager.getActiveCount()).isZero();
        assertThat(manager.hasAnimationsRunning()).isFalse();
    }

    @Test
    void removeAnimationStopsTracking() {
        final CardAnimation animation = new CardAnimation(0f, 1f, 1f, EasingFunctions::linear);
        manager.addAnimation(animation);
        manager.removeAnimation(animation);
        assertThat(manager.getActiveCount()).isZero();
    }

    @Test
    void clearRemovesAllAnimations() {
        manager.addAnimation(new CardAnimation(0f, 1f, 1f, EasingFunctions::linear));
        manager.addAnimation(new CardAnimation(0f, 1f, 1f, EasingFunctions::linear));
        manager.clear();
        assertThat(manager.hasAnimationsRunning()).isFalse();
    }

    @Test
    void completionCallbacksAreInvoked() {
        final AtomicInteger counter = new AtomicInteger();
        final CardAnimation animation = new CardAnimation(0f, 1f, 0.1f, EasingFunctions::linear);
        animation.setOnComplete(counter::incrementAndGet);
        manager.addAnimation(animation);
        manager.update(0.2f);
        assertThat(counter.get()).isEqualTo(1);
    }

    private org.assertj.core.data.Offset<Float> withinTolerance() {
        return org.assertj.core.data.Offset.offset(0.001f);
    }
}
