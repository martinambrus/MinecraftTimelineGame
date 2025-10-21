package com.minecrafttimeline.core.card;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Utility responsible for loading {@link Card} instances from a JSON trivia database.
 */
public final class TriviaDatabaseLoader {

    private TriviaDatabaseLoader() {
        // Utility class
    }

    /**
     * Loads cards from the provided JSON trivia database file.
     *
     * @param filePath path to the trivia database in JSON format; must not be {@code null}
     * @return immutable list of {@link Card} entries parsed from the file
     * @throws IllegalArgumentException if the file cannot be read, contains invalid structure,
     *                                  or required fields are missing
     */
    public static List<Card> loadFromJson(final String filePath) {
        Objects.requireNonNull(filePath, "filePath must not be null");

        final Path path = Paths.get(filePath);
        final String rawJson;
        try {
            rawJson = Files.readString(path);
        } catch (NoSuchFileException e) {
            throw new IllegalArgumentException("Trivia database file not found: " + filePath, e);
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to read trivia database: " + filePath, e);
        }

        final JSONArray array;
        try {
            array = new JSONArray(rawJson);
        } catch (JSONException e) {
            throw new IllegalArgumentException("Invalid JSON structure in trivia database: " + filePath, e);
        }

        final List<Card> cards = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            final JSONObject object = array.optJSONObject(i);
            if (object == null) {
                throw new IllegalArgumentException("Invalid card entry at index " + i + " in trivia database.");
            }

            final String id = requireString(object, "id", i);
            final String title = requireString(object, "title", i);
            final String dateValue = requireString(object, "date", i);
            final String trivia = requireString(object, "trivia", i);
            final String imageAssetPath = requireString(object, "imageAssetPath", i);
            final String version = requireString(object, "version", i);

            final LocalDate date;
            try {
                date = LocalDate.parse(dateValue);
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException(
                        "Invalid date format for card '" + id + "'. Expected ISO-8601 YYYY-MM-DD.", e);
            }

            cards.add(new Card(id, title, date, trivia, imageAssetPath, version));
        }

        return List.copyOf(cards);
    }

    private static String requireString(final JSONObject object, final String key, final int index) {
        final String value = object.optString(key, null);
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException(
                    "Missing required field '" + key + "' for card entry at index " + index + '.');
        }
        return value;
    }
}
