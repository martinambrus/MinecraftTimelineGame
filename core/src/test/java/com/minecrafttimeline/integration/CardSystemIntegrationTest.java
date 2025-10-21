package com.minecrafttimeline.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.minecrafttimeline.core.card.Card;
import com.minecrafttimeline.core.card.CardDeck;
import com.minecrafttimeline.core.card.CardManager;
import com.minecrafttimeline.core.card.TriviaDatabaseLoader;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CardSystemIntegrationTest {

    private List<Card> cards;
    private CardDeck deck;

    @BeforeEach
    void setUp() {
        Path triviaPath = Path.of("assets", "data", "trivia.json");
        if (!Files.exists(triviaPath)) {
            triviaPath = Path.of("core", "assets", "data", "trivia.json");
        }

        if (!Files.exists(triviaPath)) {
            throw new IllegalStateException("Unable to locate trivia database for integration test");
        }
        cards = TriviaDatabaseLoader.loadFromJson(triviaPath.toString());
        CardManager.getInstance().initialize(triviaPath.toString());
        deck = new CardDeck(cards);
    }

    @Test
    void fullWorkflowLoadsShufflesAndDealsCards() {
        assertThat(cards).hasSizeGreaterThanOrEqualTo(10);
        deck.shuffle();

        final List<Card> dealt = deck.dealCards(5);
        assertThat(dealt).hasSize(5);
        assertThat(new HashSet<>(dealt)).hasSize(5);

        final List<Card> remaining = deck.dealCards(deck.size());
        final Set<String> remainingIds = new HashSet<>();
        for (Card card : remaining) {
            remainingIds.add(card.getId());
        }

        for (Card card : dealt) {
            assertThat(remainingIds).doesNotContain(card.getId());
        }

        assertThat(remaining.size()).isEqualTo(cards.size() - 5);
        assertThat(CardManager.getInstance().getTotalCardCount()).isEqualTo(cards.size());
    }
}
