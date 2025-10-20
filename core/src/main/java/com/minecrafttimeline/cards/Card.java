package com.minecrafttimeline.cards;

import java.time.LocalDate;
import java.util.Map;
import java.util.Objects;

/**
 * Immutable data structure representing a single historical Minecraft event card.
 */
public record Card(
        String id,
        String title,
        LocalDate date,
        String trivia,
        String imageAssetPath,
        String minecraftVersion) {

    /**
     * Constructs a {@link Card} ensuring that all properties are non-null.
     *
     * @param id               the unique identifier for the card
     * @param title            the display title of the card
     * @param date             the historical date represented by the card
     * @param trivia           the descriptive trivia text for the card
     * @param imageAssetPath   the path to the associated image asset
     * @param minecraftVersion the Minecraft version relevant to the event
     */
    public Card {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(title, "title");
        Objects.requireNonNull(date, "date");
        Objects.requireNonNull(trivia, "trivia");
        Objects.requireNonNull(imageAssetPath, "imageAssetPath");
        Objects.requireNonNull(minecraftVersion, "minecraftVersion");
    }

    /**
     * Provides a serialisable view of the card data.
     *
     * @return a map containing all card properties
     */
    public Map<String, Object> toSerializableMap() {
        return Map.of(
                "id", id,
                "title", title,
                "date", date.toString(),
                "trivia", trivia,
                "imageAssetPath", imageAssetPath,
                "minecraftVersion", minecraftVersion);
    }

    /**
     * Retrieves the card identifier.
     *
     * @return the unique card identifier
     */
    @Override
    public String id() {
        return id;
    }

    /**
     * Retrieves the display title for the card.
     *
     * @return the card title
     */
    @Override
    public String title() {
        return title;
    }

    /**
     * Retrieves the historical date of the card.
     *
     * @return the date of the represented event
     */
    @Override
    public LocalDate date() {
        return date;
    }

    /**
     * Retrieves the trivia text.
     *
     * @return the descriptive trivia text
     */
    @Override
    public String trivia() {
        return trivia;
    }

    /**
     * Retrieves the image asset path.
     *
     * @return the relative path to the associated image asset
     */
    @Override
    public String imageAssetPath() {
        return imageAssetPath;
    }

    /**
     * Retrieves the Minecraft version associated with the card.
     *
     * @return the related Minecraft version string
     */
    @Override
    public String minecraftVersion() {
        return minecraftVersion;
    }
}
