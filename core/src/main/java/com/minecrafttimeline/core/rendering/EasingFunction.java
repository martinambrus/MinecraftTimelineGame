package com.minecrafttimeline.core.rendering;

/**
 * Functional interface describing easing functions that map normalized time to normalized value.
 */
@FunctionalInterface
public interface EasingFunction {

    /**
     * Applies the easing formula to the supplied normalized time value.
     *
     * @param t normalized time within {@code [0, 1]}
     * @return normalized output value within {@code [0, 1]}
     */
    float apply(float t);
}
