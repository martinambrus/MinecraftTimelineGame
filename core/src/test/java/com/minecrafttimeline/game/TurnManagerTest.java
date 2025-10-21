package com.minecrafttimeline.game;

import com.minecrafttimeline.cards.Card;
import com.minecrafttimeline.cards.CardDeck;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TurnManagerTest {

    private List<String> players;
    private List<Card> deckCards;

    @BeforeEach
    void setUp() {
        players = List.of("P1", "P2");
        deckCards = List.of(
                createCard("1", LocalDate.of(2009, 1, 1)),
                createCard("2", LocalDate.of(2010, 1, 1)),
                createCard("3", LocalDate.of(2011, 1, 1)),
                createCard("4", LocalDate.of(2012, 1, 1)),
                createCard("5", LocalDate.of(2013, 1, 1)),
                createCard("6", LocalDate.of(2014, 1, 1))
        );
    }

    @Test
    void dealInitialCards_distributesCardsAndSetsTimeline() {
        final GameState state = emptyState();
        final TurnManager manager = new TurnManager(players, new CardDeck(deckCards), new GameRules(state));
        final GameState result = manager.dealInitialCards(state, 2);
        assertThat(result.getPlayerHand("P1")).hasSize(2);
        assertThat(result.getPlayerHand("P2")).hasSize(2);
        assertThat(result.getTimeline()).hasSize(1);
        assertThat(result.getCurrentPhase()).isEqualTo(GamePhase.PLAYER_TURN);
        assertThat(result.getCurrentPlayer()).isEqualTo("P1");
    }

    @Test
    void nextTurn_cyclesPlayers() {
        final GameState state = emptyState();
        final TurnManager manager = new TurnManager(players, new CardDeck(deckCards), new GameRules(state));
        final GameState dealt = manager.dealInitialCards(state, 1);
        final GameState next = manager.nextTurn(dealt);
        assertThat(next.getCurrentPlayer()).isEqualTo("P2");
    }

    @Test
    void canPlayerMove_checksHand() {
        final GameState state = emptyState();
        final TurnManager manager = new TurnManager(players, new CardDeck(deckCards), new GameRules(state));
        final GameState dealt = manager.dealInitialCards(state, 1);
        assertThat(manager.canPlayerMove(dealt)).isTrue();
        final GameState noCards = GameState.from(dealt)
                .hands(Map.of("P1", List.of(), "P2", dealt.getPlayerHand("P2")))
                .build();
        assertThat(manager.canPlayerMove(noCards)).isFalse();
    }

    @Test
    void applyCardPlacement_validPlacementUpdatesState() {
        final Card cardA = createCard("A", LocalDate.of(2009, 1, 1));
        final Card cardB = createCard("B", LocalDate.of(2010, 1, 1));
        final Card cardC = createCard("C", LocalDate.of(2011, 1, 1));
        final Card cardD = createCard("D", LocalDate.of(2012, 1, 1));
        final GameState initial = GameState.builder()
                .phase(GamePhase.PLAYER_TURN)
                .currentPlayer("P1")
                .players(players)
                .timeline(List.of(cardA, cardC))
                .hands(Map.of("P1", new ArrayList<>(List.of(cardB)), "P2", new ArrayList<>(List.of(cardD))))
                .scores(Map.of("P1", 0, "P2", 0))
                .history(List.of())
                .build();
        final GameRules rules = new GameRules(initial);
        final TurnManager manager = new TurnManager(players, new CardDeck(deckCards), rules);
        final List<Card> handBefore = new ArrayList<>(initial.getPlayerHand("P1"));
        final AtomicInteger correctCalls = new AtomicInteger();
        manager.addListener(new TurnManager.TurnListener() {
            @Override
            public void onTurnChanged(final String currentPlayerId, final GamePhase phase) {
            }

            @Override
            public void onCorrectPlacement(final String playerId, final Card card, final int position) {
                correctCalls.incrementAndGet();
            }

            @Override
            public void onIncorrectPlacement(final String playerId, final Card card, final int position) {
            }
        });
        final GameState result = manager.applyCardPlacement(initial, cardB, 1);
        assertThat(result.getTimeline()).containsExactly(cardA, cardB, cardC);
        assertThat(result.getPlayerHand("P1")).isEmpty();
        assertThat(result.getPlayerHand("P2")).containsExactly(cardD);
        assertThat(result.getScore("P1")).isEqualTo(1);
        assertThat(result.getCurrentPhase()).isEqualTo(GamePhase.CORRECT_PLACEMENT);
        assertThat(result.getMoveHistory()).hasSize(1);
        assertThat(correctCalls.get()).isEqualTo(1);
        assertThat(handBefore).containsExactly(cardB);
    }

    @Test
    void applyCardPlacement_invalidPlacementNotifiesListeners() {
        final Card cardA = createCard("A", LocalDate.of(2009, 1, 1));
        final Card cardC = createCard("C", LocalDate.of(2011, 1, 1));
        final Card cardB = createCard("B", LocalDate.of(2010, 1, 1));
        final GameState initial = GameState.builder()
                .phase(GamePhase.PLAYER_TURN)
                .currentPlayer("P1")
                .players(players)
                .timeline(List.of(cardA, cardC))
                .hands(Map.of("P1", List.of(cardB), "P2", List.of()))
                .scores(Map.of("P1", 0, "P2", 0))
                .history(List.of())
                .build();
        final GameRules rules = new GameRules(initial);
        final TurnManager manager = new TurnManager(players, new CardDeck(deckCards), rules);
        final AtomicInteger incorrectCalls = new AtomicInteger();
        manager.addListener(new TurnManager.TurnListener() {
            @Override
            public void onTurnChanged(final String currentPlayerId, final GamePhase phase) {
            }

            @Override
            public void onCorrectPlacement(final String playerId, final Card card, final int position) {
            }

            @Override
            public void onIncorrectPlacement(final String playerId, final Card card, final int position) {
                incorrectCalls.incrementAndGet();
            }
        });
        final GameState result = manager.applyCardPlacement(initial, cardB, 2);
        assertThat(result.getTimeline()).containsExactly(cardA, cardC);
        assertThat(result.getCurrentPhase()).isEqualTo(GamePhase.INCORRECT_PLACEMENT);
        assertThat(incorrectCalls.get()).isEqualTo(1);
    }

    @Test
    void applyCardPlacement_rejectsCardNotInHand() {
        final Card cardA = createCard("A", LocalDate.of(2009, 1, 1));
        final Card cardB = createCard("B", LocalDate.of(2010, 1, 1));
        final Card cardC = createCard("C", LocalDate.of(2011, 1, 1));
        final GameState initial = GameState.builder()
                .phase(GamePhase.PLAYER_TURN)
                .currentPlayer("P1")
                .players(players)
                .timeline(List.of(cardA, cardC))
                .hands(Map.of("P1", List.of(), "P2", List.of(cardB)))
                .scores(Map.of("P1", 0, "P2", 0))
                .history(List.of())
                .build();
        final TurnManager manager = new TurnManager(players, new CardDeck(deckCards), new GameRules(initial));
        assertThatThrownBy(() -> manager.applyCardPlacement(initial, cardB, 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Card does not belong");
    }

    private GameState emptyState() {
        return GameState.builder()
                .phase(GamePhase.SETUP)
                .currentPlayer("P1")
                .players(players)
                .timeline(List.of())
                .hands(Map.of("P1", List.of(), "P2", List.of()))
                .scores(Map.of("P1", 0, "P2", 0))
                .history(List.of())
                .build();
    }

    private Card createCard(final String id, final LocalDate date) {
        return new Card(id, "Title " + id, date, "Trivia", "image.png", "1.0");
    }
}
