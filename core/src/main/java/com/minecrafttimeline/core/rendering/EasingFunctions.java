package com.minecrafttimeline.core.rendering;

/**
 * Collection of easing functions used by the animation system. Each method expects a normalized time value
 * {@code t} within the {@code [0, 1]} range and returns a normalized output that can be used for interpolation.
 */
public final class EasingFunctions {

    private EasingFunctions() {
        // Utility class
    }

    /**
     * Linear interpolation where the output matches the input. Formula: {@code f(t) = t}.
     *
     * @param t normalized time value
     * @return linear interpolation of {@code t}
     */
    public static float linear(final float t) {
        return clamp(t);
    }

    /**
     * Quadratic ease-in that accelerates from rest. Formula: {@code f(t) = t^2}.
     *
     * @param t normalized time value
     * @return eased value following a quadratic curve
     */
    public static float easeInQuad(final float t) {
        final float clamped = clamp(t);
        return clamped * clamped;
    }

    /**
     * Quadratic ease-out that decelerates toward the end. Formula: {@code f(t) = 1 - (1 - t)^2}.
     *
     * @param t normalized time value
     * @return eased value that starts quickly and settles smoothly
     */
    public static float easeOutQuad(final float t) {
        final float clamped = clamp(t);
        final float inverted = 1f - clamped;
        return 1f - (inverted * inverted);
    }

    /**
     * Symmetric quadratic ease-in-out curve. Formula: {@code f(t) = 2t^2} for {@code t < 0.5} and
     * {@code f(t) = 1 - 2(1 - t)^2} for {@code t >= 0.5}.
     *
     * @param t normalized time value
     * @return eased value with gentle start and end
     */
    public static float easeInOutQuad(final float t) {
        final float clamped = clamp(t);
        if (clamped < 0.5f) {
            return 2f * clamped * clamped;
        }
        final float inverted = 1f - clamped;
        return 1f - (2f * inverted * inverted);
    }

    /**
     * Cubic ease-out curve that delivers a slightly bouncy feel. Formula: {@code f(t) = 1 - (1 - t)^3}.
     *
     * @param t normalized time value
     * @return eased value emphasizing a quick response that softens toward completion
     */
    public static float easeOutCubic(final float t) {
        final float clamped = clamp(t);
        final float inverted = 1f - clamped;
        return 1f - (inverted * inverted * inverted);
    }

    private static float clamp(final float value) {
        return Math.max(0f, Math.min(1f, value));
    }
}
