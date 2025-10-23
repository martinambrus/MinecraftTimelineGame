package com.minecrafttimeline.core.rendering;

/**
 * Enumerates the different animation categories used for cards. The type is primarily used for diagnostics and
 * allows the manager to group similar transitions if needed.
 */
public enum AnimationType {
    /** Dragging feedback while the player holds a card. */
    DRAG,
    /** Placement animation when a card snaps onto the timeline. */
    PLACE,
    /** Flip animation for revealing hidden card faces. */
    FLIP,
    /** Entry animation for cards appearing on screen. */
    APPEAR,
    /** Exit animation for cards leaving the screen. */
    DISAPPEAR,
    /** Pulsing emphasis used for success feedback. */
    PULSE,
    /** Shake feedback to highlight errors. */
    SHAKE
}
