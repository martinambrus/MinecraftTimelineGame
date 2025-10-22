package com.minecrafttimeline.core.game;

import com.minecrafttimeline.core.card.Card;
import com.minecrafttimeline.core.card.CardDeck;
import com.minecrafttimeline.models.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GameSessionTest {

    private GameSession session;
    private List<Player> players;
    private CardDeck deck;

    @BeforeEach
    void setUp() {
        session = new GameSession();
        players = List.of(new Player("p1", "Alice"), new Player("p2", "Bob"));
        deck = new CardDeck(List.of(
                card("c1", 1990),
                card("c2", 2000),
                card("c3", 2010),
                card("c4", 2020)));
        session.setCardsPerPlayer(1);
        session.startNewGame(players, deck);
    }

    @Test
    void startNewGame_initialisesState() {
        assertThat(session.getCurrentPhase()).isEqualTo(GamePhase.PLAYER_TURN);
        assertThat(players.get(0).getHand()).hasSize(1);
        assertThat(players.get(1).getHand()).hasSize(1);
    }

    @Test
    void placeCard_validPlacementAdvancesTurn() {
        final Card card = players.get(0).getHand().get(0);
        final boolean correct = session.placeCard(card, 0);
        assertThat(correct).isTrue();
        assertThat(session.getGameState().getTimeline()).contains(card);
        assertThat(session.getTurnManager().getCurrentPlayer()).isEqualTo(players.get(1));
    }

    @Test
    void placeCard_invalidPlacementKeepsTurn() {
        final Card card = players.get(0).getHand().get(0);
        // Attempt to place card at an invalid index beyond the timeline bounds
        final boolean result = session.placeCard(card, 5);
        assertThat(result).isFalse();
        assertThat(session.getTurnManager().getCurrentPlayer()).isEqualTo(players.get(0));
        assertThat(session.getGameState().getTimeline()).isEmpty();
    }

    @Test
    void undoAndRedo_restoreStateCorrectly() {
        final Card card = players.get(0).getHand().get(0);
        session.placeCard(card, 0);
        assertThat(session.undo()).isTrue();
        assertThat(session.getGameState().getTimeline()).isEmpty();
        assertThat(players.get(0).getHand()).contains(card);
        assertThat(session.redo()).isTrue();
        assertThat(session.getGameState().getTimeline()).contains(card);
    }

    @Test
    void winDetection_setsGameOverPhase() {
        final Card card = players.get(0).getHand().get(0);
        session.placeCard(card, 0);
        assertThat(session.isGameOver()).isTrue();
        assertThat(session.getWinner()).isEqualTo(players.get(0));
    }

    @Test
    void saveAndLoad_restoreSession() throws Exception {
        final Card card = players.get(0).getHand().get(0);
        session.placeCard(card, 0);
        final String filename = "test-session.json";
        session.saveGame(filename);
        final GameSession loaded = GameSession.loadGame(filename);
        assertThat(loaded.getGameState().getTimeline()).hasSize(1);
        assertThat(loaded.getCurrentPhase()).isEqualTo(GamePhase.GAME_OVER);
        Files.deleteIfExists(Path.of(filename));
    }

    private static Card card(final String id, final int year) {
        return new Card(id, "title-" + id, LocalDate.of(year, 1, 1), "trivia", "image", "version");
    }
}
