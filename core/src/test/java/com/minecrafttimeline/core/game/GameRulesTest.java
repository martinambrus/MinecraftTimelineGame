package com.minecrafttimeline.core.game;

import com.minecrafttimeline.core.card.Card;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class GameRulesTest {

    @Test
    void validateCardPlacement_allowsAnyPositionWhenTimelineEmpty() {
        final Card card = card("1", LocalDate.of(2000, 1, 1));
        assertThat(GameRules.validateCardPlacement(card, List.of(), 0)).isTrue();
        assertThat(GameRules.validateCardPlacement(card, List.of(), 1)).isFalse();
    }

    @Test
    void validateCardPlacement_handlesSingleCardTimeline() {
        final Card card = card("1", LocalDate.of(2000, 1, 1));
        final Card existing = card("2", LocalDate.of(2010, 1, 1));
        final List<Card> timeline = List.of(existing);
        assertThat(GameRules.validateCardPlacement(card, timeline, 0)).isTrue();
        assertThat(GameRules.validateCardPlacement(card, timeline, 1)).isFalse();
    }

    @Test
    void validateCardPlacement_allowsPlacementAtEnd() {
        final List<Card> timeline = List.of(
                card("1", LocalDate.of(2000, 1, 1)),
                card("2", LocalDate.of(2010, 1, 1)));
        final Card card = card("3", LocalDate.of(2020, 1, 1));
        assertThat(GameRules.validateCardPlacement(card, timeline, 2)).isTrue();
        assertThat(GameRules.validateCardPlacement(card, timeline, 0)).isFalse();
    }

    @Test
    void validateCardPlacement_allowsPlacementBetweenNeighbours() {
        final List<Card> timeline = List.of(
                card("1", LocalDate.of(2000, 1, 1)),
                card("2", LocalDate.of(2010, 1, 1)));
        final Card card = card("3", LocalDate.of(2005, 6, 1));
        assertThat(GameRules.validateCardPlacement(card, timeline, 1)).isTrue();
        assertThat(GameRules.validateCardPlacement(card, timeline, 0)).isFalse();
        assertThat(GameRules.validateCardPlacement(card, timeline, 2)).isFalse();
    }

    @Test
    void validateCardPlacement_rejectsOutOfOrderPlacement() {
        final List<Card> timeline = List.of(
                card("1", LocalDate.of(2000, 1, 1)),
                card("2", LocalDate.of(2010, 1, 1)));
        final Card card = card("3", LocalDate.of(1995, 1, 1));
        assertThat(GameRules.validateCardPlacement(card, timeline, 1)).isFalse();
    }

    @Test
    void getValidPositions_returnsAllPossibleIndices() {
        final Card card = card("1", LocalDate.of(2005, 1, 1));
        final List<Card> timeline = List.of(
                card("2", LocalDate.of(2000, 1, 1)),
                card("3", LocalDate.of(2010, 1, 1)));
        assertThat(GameRules.getValidPositionsForCard(card, timeline)).containsExactly(1);
    }

    @Test
    void getValidPositions_handlesEmptyTimeline() {
        final Card card = card("1", LocalDate.of(2005, 1, 1));
        assertThat(GameRules.getValidPositionsForCard(card, List.of())).containsExactly(0);
    }

    @Test
    void getCorrectPosition_returnsExpectedIndex() {
        final List<Card> timeline = List.of(
                card("1", LocalDate.of(1990, 1, 1)),
                card("2", LocalDate.of(2000, 1, 1)),
                card("3", LocalDate.of(2010, 1, 1)));
        final Card newCard = card("4", LocalDate.of(2005, 1, 1));
        assertThat(GameRules.getCorrectPosition(newCard, timeline)).isEqualTo(2);
    }

    @Test
    void isCorrectPlacement_respectsTolerance() {
        final List<Card> timeline = List.of(
                card("1", LocalDate.of(1990, 1, 1)),
                card("2", LocalDate.of(2000, 1, 1)),
                card("3", LocalDate.of(2010, 1, 1)));
        final Card newCard = card("4", LocalDate.of(2005, 1, 1));
        assertThat(GameRules.isCorrectPlacement(newCard, timeline, 1)).isTrue();
        assertThat(GameRules.isCorrectPlacement(newCard, timeline, 0)).isFalse();
    }

    @Test
    void hasPlayerWon_requiresEmptyHandAndOrderedTimeline() {
        final List<Card> timeline = List.of(
                card("1", LocalDate.of(1990, 1, 1)),
                card("2", LocalDate.of(2000, 1, 1)));
        assertThat(GameRules.hasPlayerWon(List.of(), timeline)).isTrue();
        assertThat(GameRules.hasPlayerWon(List.of(card("3", LocalDate.of(2010, 1, 1))), timeline)).isFalse();
    }

    @Test
    void hasPlayerWon_detectsUnorderedTimeline() {
        final List<Card> timeline = List.of(
                card("1", LocalDate.of(2000, 1, 1)),
                card("2", LocalDate.of(1990, 1, 1)));
        assertThat(GameRules.hasPlayerWon(List.of(), timeline)).isFalse();
    }

    @Test
    void getGameProgress_reportsFraction() {
        final List<Card> timeline = List.of(
                card("1", LocalDate.of(1990, 1, 1)),
                card("2", LocalDate.of(2000, 1, 1)),
                card("3", LocalDate.of(2010, 1, 1)));
        assertThat(GameRules.getGameProgress(timeline, 6)).isCloseTo(0.5f, within(0.0001f));
    }

    private static Card card(final String id, final LocalDate date) {
        return new Card(id, "title-" + id, date, "trivia", "image", "version");
    }
}
