package com.minecrafttimeline.core.rendering;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

/**
 * Describes a rectangular placement zone that cards may be dropped onto.
 */
public interface PlacementZone {

    /**
     * Provides the axis-aligned bounds of the zone.
     *
     * @return mutable rectangle representing the zone bounds
     */
    Rectangle getBounds();

    /**
     * Retrieves the centre point of the zone in world coordinates.
     *
     * @return mutable vector containing the centre position
     */
    Vector2 getCenter();

    /**
     * Returns the bottom-left position of the zone.
     *
     * @return mutable vector for the bottom-left coordinates
     */
    Vector2 getPosition();

    /**
     * Returns the size of the zone.
     *
     * @return mutable vector storing width (x) and height (y)
     */
    Vector2 getSize();
}
