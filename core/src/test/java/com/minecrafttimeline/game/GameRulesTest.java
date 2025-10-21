package com.minecrafttimeline.game;

import com.minecrafttimeline.cards.Card;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class GameRulesTest {

    private Card card2009;
    private Card card2010;
    private Card card2012;
    private Card card2015;
    private GameState baseState;

    @BeforeEach
    void setUp() {
        card2009 = createCard("2009", LocalDate.of(2009, 1, 1));
        card2010 = createCard("2010", LocalDate.of(2010, 1, 1));
        card2012 = createCard("2012", LocalDate.of(2012, 1, 1));
        card2015 = createCard("2015", LocalDate.of(2015, 1, 1));
        baseState = GameState.builder()
                .phase(GamePhase.PLAYER_TURN)
                .currentPlayer("P1")
                .players(List.of("P1", "P2"))
                .timeline(List.of(card2010, card2015))
                .hands(Map.of("P1", List.of(card2012), "P2", List.of()))
                .scores(Map.of("P1", 0, "P2", 0))
                .history(List.of())
                .build();
    }

    @Test
    void validateCardPlacement_betweenCardsIsValid() {
        final GameRules rules = new GameRules(baseState);
        assertThat(rules.validateCardPlacement(card2012, 1)).isTrue();
    }

    @Test
    void validateCardPlacement_atBeginningIsValid() {
        final GameRules rules = new GameRules(baseState);
        assertThat(rules.validateCardPlacement(card2009, 0)).isTrue();
    }

    @Test
    void validateCardPlacement_atEndIsValid() {
        final GameRules rules = new GameRules(baseState);
        assertThat(rules.validateCardPlacement(card2015, 2)).isFalse();
        assertThat(rules.validateCardPlacement(createCard("2016", LocalDate.of(2016, 1, 1)), 2)).isTrue();
    }

    @Test
    void validateCardPlacement_invalidOutOfOrder() {
        final GameRules rules = new GameRules(baseState);
        assertThat(rules.validateCardPlacement(card2012, 2)).isFalse();
        assertThat(rules.validateCardPlacement(card2012, -1)).isFalse();
        assertThat(rules.validateCardPlacement(card2012, 3)).isFalse();
    }

    @Test
    void getValidPlacements_handlesEdgeCases() {
        final GameRules rules = new GameRules(baseState);
        assertThat(rules.getValidPlacementsForCard(card2012)).containsExactly(1);
        final GameRules emptyRules = new GameRules(GameState.builder()
                .phase(GamePhase.PLAYER_TURN)
                .currentPlayer("P1")
                .players(List.of("P1"))
                .timeline(List.of())
                .hands(Map.of("P1", List.of(card2010)))
                .scores(Map.of("P1", 0))
                .history(List.of())
                .build());
        assertThat(emptyRules.getValidPlacementsForCard(card2010)).containsExactly(0);
    }

    @Test
    void hasPlayerWon_detectsCompletion() {
        final GameRules rules = new GameRules(baseState);
        assertThat(rules.hasPlayerWon()).isFalse();
        final GameState finished = GameState.builder()
                .phase(GamePhase.GAME_OVER)
                .currentPlayer("P1")
                .players(List.of("P1", "P2"))
                .timeline(List.of(card2010, card2012, card2015))
                .hands(Map.of("P1", List.of(), "P2", List.of()))
                .scores(Map.of("P1", 2, "P2", 1))
                .history(List.of())
                .build();
        rules.updateState(finished);
        assertThat(rules.hasPlayerWon()).isTrue();
    }

    @Test
    void getCorrectPosition_returnsInsertionIndex() {
        final GameRules rules = new GameRules(baseState);
        assertThat(rules.getCorrectPosition(card2012)).isEqualTo(1);
        assertThat(rules.getCorrectPosition(card2009)).isEqualTo(0);
        final Card card2020 = createCard("2020", LocalDate.of(2020, 1, 1));
        assertThat(rules.getCorrectPosition(card2020)).isEqualTo(2);
    }

    private Card createCard(final String id, final LocalDate date) {
        return new Card(id, "Title " + id, date, "Trivia", "image.png", "1.0");
    }
}
