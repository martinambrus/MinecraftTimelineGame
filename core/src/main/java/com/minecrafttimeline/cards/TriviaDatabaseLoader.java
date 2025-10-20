package com.minecrafttimeline.cards;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Loader responsible for deserialising card definitions from JSON files.
 */
public class TriviaDatabaseLoader {

    private final ObjectMapper objectMapper;

    /**
     * Creates a loader with a default {@link ObjectMapper} instance.
     */
    public TriviaDatabaseLoader() {
        this(createDefaultMapper());
    }

    /**
     * Creates a loader with the provided {@link ObjectMapper} instance.
     *
     * @param objectMapper the mapper to use for JSON parsing
     */
    public TriviaDatabaseLoader(final ObjectMapper objectMapper) {
        this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper");
    }

    private static ObjectMapper createDefaultMapper() {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.findAndRegisterModules();
        return mapper;
    }

    /**
     * Loads cards from the supplied JSON file path.
     *
     * @param path the path to the trivia JSON file
     * @return an immutable list of {@link Card} instances
     * @throws IOException              if reading the file fails
     * @throws IllegalArgumentException if the JSON content is invalid
     */
    public List<Card> load(final Path path) throws IOException {
        Objects.requireNonNull(path, "path");
        try (InputStream inputStream = Files.newInputStream(path)) {
            return load(inputStream);
        }
    }

    /**
     * Loads cards from an {@link InputStream} representing JSON content.
     *
     * @param inputStream the stream providing the trivia database
     * @return an immutable list of {@link Card} instances
     * @throws IOException              if reading the stream fails
     * @throws IllegalArgumentException if the JSON content is invalid
     */
    public List<Card> load(final InputStream inputStream) throws IOException {
        Objects.requireNonNull(inputStream, "inputStream");
        try (JsonParser parser = new JsonFactory(objectMapper).createParser(inputStream)) {
            final JsonNode root = objectMapper.readTree(parser);
            if (root == null || !root.isArray()) {
                throw new IllegalArgumentException("Trivia database must be a JSON array");
            }

            final List<Card> cards = new ArrayList<>();
            final Set<String> identifiers = new HashSet<>();
            for (JsonNode node : root) {
                cards.add(parseCard(node, identifiers));
            }
            return List.copyOf(cards);
        }
    }

    private Card parseCard(final JsonNode node, final Set<String> identifiers) {
        if (node == null || !node.isObject()) {
            throw new IllegalArgumentException("Card entry must be a JSON object");
        }

        final String id = requireText(node, "id");
        final String title = requireText(node, "title");
        final String dateText = requireText(node, "date");
        final String trivia = requireText(node, "trivia");
        final String imageAssetPath = requireText(node, "imageAssetPath");
        final String version = requireText(node, "version");

        if (!identifiers.add(id)) {
            throw new IllegalArgumentException("Duplicate card identifier detected: " + id);
        }

        try {
            final LocalDate date = LocalDate.parse(dateText);
            return new Card(id, title, date, trivia, imageAssetPath, version);
        } catch (DateTimeParseException exception) {
            throw new IllegalArgumentException("Invalid date format for card '" + id + "': " + dateText,
                    exception);
        }
    }

    private String requireText(final JsonNode node, final String fieldName) {
        final JsonNode value = node.get(fieldName);
        if (value == null || value.isNull()) {
            throw new IllegalArgumentException("Missing required field: " + fieldName);
        }
        final String text = value.asText();
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("Field '" + fieldName + "' must not be blank");
        }
        return text;
    }
}
