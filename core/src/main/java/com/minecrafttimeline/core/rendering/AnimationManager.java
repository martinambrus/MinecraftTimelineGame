package com.minecrafttimeline.core.rendering;

import java.util.ArrayList;
import java.util.List;

/**
 * Central coordinator for all active {@link CardAnimation} instances. Animations are updated once per frame with the
 * current delta time to keep motion smooth and frame-rate independent.
 */
public class AnimationManager {

    private final List<CardAnimation> activeAnimations = new ArrayList<>();

    /**
     * Adds a new animation to the manager.
     *
     * @param animation animation to track
     */
    public void addAnimation(final CardAnimation animation) {
        if (animation == null) {
            return;
        }
        activeAnimations.add(animation);
    }

    /**
     * Updates all tracked animations. The method does not allocate new objects inside the loop which keeps garbage
     * collection pressure minimal and avoids frame drops during rendering.
     *
     * @param delta elapsed time in seconds since the previous frame
     */
    public void update(final float delta) {
        for (int i = 0; i < activeAnimations.size(); ) {
            final CardAnimation animation = activeAnimations.get(i);
            animation.update(delta);
            if (animation.isDone()) {
                // Swap-remove pattern avoids shifting the entire list and keeps the loop allocation-free.
                final int lastIndex = activeAnimations.size() - 1;
                final CardAnimation removed = activeAnimations.remove(lastIndex);
                if (i < activeAnimations.size()) {
                    activeAnimations.set(i, removed);
                }
                animation.invokeCompletionIfNeeded();
            } else {
                i++;
            }
        }
    }

    /**
     * Removes the specified animation when no longer needed.
     *
     * @param animation animation to remove
     */
    public void removeAnimation(final CardAnimation animation) {
        activeAnimations.remove(animation);
    }

    /**
     * Clears all animations, immediately stopping every active tween.
     */
    public void clear() {
        activeAnimations.clear();
    }

    /**
     * Retrieves the number of active animations currently being managed.
     *
     * @return active animation count
     */
    public int getActiveCount() {
        return activeAnimations.size();
    }

    /**
     * Indicates whether any animations are currently running.
     *
     * @return {@code true} when there is at least one animation that has not completed
     */
    public boolean hasAnimationsRunning() {
        return !activeAnimations.isEmpty();
    }
}
