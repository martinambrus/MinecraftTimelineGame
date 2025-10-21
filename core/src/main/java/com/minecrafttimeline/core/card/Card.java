package com.minecrafttimeline.core.card;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Immutable representation of a single Minecraft timeline card.
 * <p>
 * Each card encapsulates timeline metadata and an associated trivia description
 * while guaranteeing immutability for safe sharing across the application.
 */
public final class Card {

    private final String id;
    private final String title;
    private final LocalDate date;
    private final String trivia;
    private final String imageAssetPath;
    private final String minecraftVersion;

    /**
     * Creates a new immutable {@link Card} instance.
     *
     * @param id               unique identifier for the card; must not be {@code null}
     * @param title            human readable event title; must not be {@code null}
     * @param date             event occurrence date; must not be {@code null}
     * @param trivia           extended description text; must not be {@code null}
     * @param imageAssetPath   path to the corresponding image asset; must not be {@code null}
     * @param minecraftVersion textual representation of the Minecraft version; must not be {@code null}
     * @throws NullPointerException if any argument is {@code null}
     */
    public Card(
            final String id,
            final String title,
            final LocalDate date,
            final String trivia,
            final String imageAssetPath,
            final String minecraftVersion) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.title = Objects.requireNonNull(title, "title must not be null");
        this.date = Objects.requireNonNull(date, "date must not be null");
        this.trivia = Objects.requireNonNull(trivia, "trivia must not be null");
        this.imageAssetPath = Objects.requireNonNull(imageAssetPath, "imageAssetPath must not be null");
        this.minecraftVersion = Objects.requireNonNull(minecraftVersion, "minecraftVersion must not be null");
    }

    /**
     * Retrieves the unique identifier of the card.
     *
     * @return non-null card identifier
     */
    public String getId() {
        return id;
    }

    /**
     * Retrieves the title of the card.
     *
     * @return non-null card title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Retrieves the event date associated with the card.
     *
     * @return non-null event date
     */
    public LocalDate getDate() {
        return date;
    }

    /**
     * Retrieves the trivia description for the card.
     *
     * @return non-null trivia description
     */
    public String getTrivia() {
        return trivia;
    }

    /**
     * Retrieves the image asset path associated with the card.
     *
     * @return non-null image asset path
     */
    public String getImageAssetPath() {
        return imageAssetPath;
    }

    /**
     * Retrieves the textual representation of the Minecraft version related to the card.
     *
     * @return non-null Minecraft version string
     */
    public String getMinecraftVersion() {
        return minecraftVersion;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Card)) {
            return false;
        }
        final Card card = (Card) o;
        return id.equals(card.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "Card{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", date=" + date +
                ", trivia='" + trivia + '\'' +
                ", imageAssetPath='" + imageAssetPath + '\'' +
                ", minecraftVersion='" + minecraftVersion + '\'' +
                '}';
    }
}
