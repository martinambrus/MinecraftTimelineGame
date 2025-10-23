package com.minecrafttimeline.core.rendering;

import com.minecrafttimeline.core.util.Logger;

/**
 * Represents a single tween between two scalar values. Rendering systems poll {@link #getCurrentValue()} each frame
 * and apply it to properties such as position, rotation, or opacity. The animation is frame-rate independent thanks
 * to the time-delta driven {@link #update(float)} method.
 */
public class CardAnimation {

    private final float startValue;
    private final float endValue;
    private final float duration;
    private final EasingFunction easing;

    private float elapsed;
    private AnimationType type;
    private Runnable onComplete;
    private boolean completionInvoked;

    /**
     * Creates a new tween.
     *
     * @param start    starting value of the animation
     * @param end      target value reached when the animation completes
     * @param duration duration in seconds; must be greater than {@code 0}
     * @param easing   easing function controlling interpolation
     */
    public CardAnimation(final float start, final float end, final float duration, final EasingFunction easing) {
        if (duration <= 0f) {
            throw new IllegalArgumentException("duration must be greater than zero");
        }
        this.startValue = start;
        this.endValue = end;
        this.duration = duration;
        this.easing = easing != null ? easing : EasingFunctions::linear;
        this.type = AnimationType.DRAG;
    }

    /**
     * Advances the animation by the provided delta time. The delta is typically the frame time reported by libGDX,
     * which keeps the animation consistent even if the frame rate fluctuates.
     *
     * @param delta elapsed time in seconds since the last update
     */
    public void update(final float delta) {
        if (isDone()) {
            return;
        }
        if (delta < 0f) {
            Logger.warn("Negative delta supplied to CardAnimation.update; ignoring value");
            return;
        }
        elapsed = Math.min(duration, elapsed + delta);
    }

    /**
     * Retrieves the interpolated value at the current progress point using the configured easing curve.
     *
     * @return current interpolated value between {@code startValue} and {@code endValue}
     */
    public float getCurrentValue() {
        final float progress = getProgress();
        final float eased = easing.apply(progress);
        return startValue + ((endValue - startValue) * eased);
    }

    /**
     * Indicates whether the animation has completed.
     *
     * @return {@code true} when {@link #elapsed} has reached {@link #duration}
     */
    public boolean isDone() {
        return elapsed >= duration;
    }

    /**
     * Assigns a callback invoked once when the animation finishes. The {@link AnimationManager} triggers the callback
     * so that no additional allocations occur during the render loop.
     *
     * @param callback completion callback, may be {@code null}
     */
    public void setOnComplete(final Runnable callback) {
        onComplete = callback;
    }

    /**
     * Retrieves the current normalized progress between {@code 0.0} and {@code 1.0}.
     *
     * @return normalized progress value
     */
    public float getProgress() {
        return Math.max(0f, Math.min(1f, elapsed / duration));
    }

    /**
     * Retrieves the configured animation type.
     *
     * @return animation type used for categorization
     */
    public AnimationType getType() {
        return type;
    }

    /**
     * Updates the animation type for diagnostics.
     *
     * @param value new animation type
     */
    public void setType(final AnimationType value) {
        type = value != null ? value : AnimationType.DRAG;
    }

    /**
     * Executes the completion callback exactly once.
     */
    void invokeCompletionIfNeeded() {
        if (completionInvoked) {
            return;
        }
        completionInvoked = true;
        if (onComplete != null) {
            onComplete.run();
        }
    }
}
