package com.minecrafttimeline.data;

import java.util.Objects;

/**
 * Placeholder responsible for loading structured game data from the assets directory.
 */
public class GameDataLoader {

    /**
     * Resolves a logical asset path to a canonical representation.
     *
     * @param assetPath the relative path within the assets directory
     * @return the normalised path string
     */
    public String resolvePath(final String assetPath) {
        Objects.requireNonNull(assetPath, "assetPath");
        return assetPath.trim();
    }
}
