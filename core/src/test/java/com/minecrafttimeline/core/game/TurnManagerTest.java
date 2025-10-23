package com.minecrafttimeline.core.game;

import com.minecrafttimeline.core.card.Card;
import com.minecrafttimeline.core.card.CardDeck;
import com.minecrafttimeline.models.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class TurnManagerTest {

    private List<Player> players;
    private GameState gameState;
    private TurnManager turnManager;
    private CardDeck deck;

    @BeforeEach
    void setUp() {
        players = new ArrayList<>();
        players.add(new Player("p1", "Alice"));
        players.add(new Player("p2", "Bob"));
        gameState = new GameState(players);
        turnManager = new TurnManager(players);
        turnManager.setGameState(gameState);
        deck = new CardDeck(List.of(
                card("c1", 1990),
                card("c2", 1995),
                card("c3", 2000),
                card("c4", 2005),
                card("c5", 2010),
                card("c6", 2015)));
    }

    @Test
    void dealInitialCards_distributesCards() {
        turnManager.dealInitialCards(deck, 2);
        assertThat(players.get(0).getHand()).hasSize(2);
        assertThat(players.get(1).getHand()).hasSize(2);
        assertThat(deck.size()).isEqualTo(2);
    }

    @Test
    void getCurrentPlayer_returnsCorrectPlayer() {
        assertThat(turnManager.getCurrentPlayer()).isEqualTo(players.get(0));
        turnManager.nextTurn();
        assertThat(turnManager.getCurrentPlayer()).isEqualTo(players.get(1));
    }

    @Test
    void nextTurn_cyclesThroughPlayers() {
        turnManager.nextTurn();
        turnManager.nextTurn();
        assertThat(turnManager.getCurrentPlayer()).isEqualTo(players.get(0));
    }

    @Test
    void canPlayerMove_detectsAvailableMoves() {
        populateTimeline(1990, 2000);
        players.get(0).addCardToHand(card("play", 1995));
        assertThat(turnManager.canPlayerMove()).isTrue();
        players.get(0).removeCardFromHand(players.get(0).getHand().get(0));
        assertThat(turnManager.canPlayerMove()).isFalse();
    }

    @Test
    void applyCardPlacement_updatesTimelineScoreAndHistory() {
        populateTimeline(1990, 2000);
        final Card card = card("play", 1995);
        players.get(0).addCardToHand(card);
        final boolean correct = turnManager.applyCardPlacement(card, 1);
        assertThat(correct).isTrue();
        assertThat(gameState.getTimeline()).contains(card);
        assertThat(players.get(0).getScore()).isEqualTo(2);
        assertThat(gameState.getMoveHistory()).hasSize(1);
    }

    @Test
    void applyCardPlacement_triggersListeners() {
        populateTimeline(1990, 2000);
        final Card card = card("play", 1995);
        players.get(0).addCardToHand(card);
        final AtomicInteger moveEvents = new AtomicInteger();
        turnManager.addListener(new TurnManager.TurnListener() {
            @Override
            public void onTurnStart(final Player currentPlayer, final GamePhase phase) {
            }

            @Override
            public void onPhaseChanged(final GamePhase phase) {
            }

            @Override
            public void onMoveEvaluated(final Move move) {
                moveEvents.incrementAndGet();
            }

            @Override
            public void onGameOver(final Player winner) {
            }
        });
        turnManager.applyCardPlacement(card, 1);
        assertThat(moveEvents.get()).isEqualTo(1);
    }

    @Test
    void undo_restoresPreviousState() {
        populateTimeline(1990, 2000);
        final Card card = card("play", 1995);
        players.get(0).addCardToHand(card);
        turnManager.applyCardPlacement(card, 1);
        final Move undone = turnManager.undo();
        assertThat(undone).isNotNull();
        assertThat(gameState.getTimeline()).doesNotContain(card);
        assertThat(players.get(0).getHand()).contains(card);
        assertThat(players.get(0).getScore()).isZero();
    }

    @Test
    void redo_reappliesMove() {
        populateTimeline(1990, 2000);
        final Card card = card("play", 1995);
        players.get(0).addCardToHand(card);
        turnManager.applyCardPlacement(card, 1);
        turnManager.undo();
        final Move redone = turnManager.redo();
        assertThat(redone).isNotNull();
        assertThat(gameState.getTimeline()).contains(card);
        assertThat(players.get(0).getScore()).isEqualTo(2);
    }

    @Test
    void redo_reappliesGameOverStateForWinningMove() {
        populateTimeline(1990, 2000);
        final Card winningCard = card("win", 1995);
        players.get(0).addCardToHand(winningCard);

        turnManager.applyCardPlacement(winningCard, 1);
        assertThat(gameState.isGameOver()).isTrue();

        turnManager.undo();
        assertThat(gameState.isGameOver()).isFalse();

        turnManager.redo();

        assertThat(gameState.isGameOver()).isTrue();
        assertThat(turnManager.getCurrentPhase()).isEqualTo(GamePhase.GAME_OVER);
        assertThat(turnManager.getCurrentPlayer()).isEqualTo(players.get(0));
    }

    @Test
    void setCurrentPlayerIndex_restoresFromState() {
        turnManager.setCurrentPlayerIndex(1);
        assertThat(turnManager.getCurrentPlayer()).isEqualTo(players.get(1));
        assertThat(gameState.getCurrentPlayerIndex()).isEqualTo(1);
    }

    private void populateTimeline(final int... years) {
        int index = 0;
        for (final int year : years) {
            gameState.addCardToTimeline(card("base" + index++, year), gameState.getTimeline().size());
        }
    }

    private static Card card(final String id, final int year) {
        return new Card(id, "title-" + id, LocalDate.of(year, 1, 1), "trivia", "image", "version");
    }
}
