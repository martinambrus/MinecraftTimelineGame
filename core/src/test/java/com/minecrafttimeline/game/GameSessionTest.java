package com.minecrafttimeline.game;

import com.minecrafttimeline.cards.Card;
import com.minecrafttimeline.cards.CardDeck;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class GameSessionTest {

    @Test
    void saveAndLoadGame_preservesState() {
        final GameSession session = new GameSession(List.of("P1", "P2"), new CardDeck(sampleCards()), 1);
        final Map<String, Object> saved = session.saveGame();
        assertThat(saved).containsKeys("phase", "currentPlayer", "timeline", "hands", "scores");
        final GameState loadedState = GameState.builder()
                .phase(GamePhase.PLAYER_TURN)
                .currentPlayer("P2")
                .players(List.of("P1", "P2"))
                .timeline(session.getGameState().getTimeline())
                .hands(session.getGameState().getHands())
                .scores(session.getGameState().getScores())
                .history(session.getGameState().getMoveHistory())
                .build();
        session.loadGame(loadedState);
        assertThat(session.getGameState().getCurrentPlayer()).isEqualTo("P2");
    }

    @Test
    void undoRestoresPreviousState() {
        final GameSession session = new GameSession(List.of("P1", "P2"), new CardDeck(sampleCards()), 1);
        final GameState beforeMove = session.getGameState();
        final String currentPlayer = beforeMove.getCurrentPlayer();
        final Card cardToPlay = beforeMove.getPlayerHand(currentPlayer).get(0);
        final int position = session.getRules().getCorrectPosition(cardToPlay);
        session.placeCard(cardToPlay, position);
        assertThat(session.getGameState().getTimeline()).hasSize(beforeMove.getTimeline().size() + 1);
        final boolean undone = session.undo();
        assertThat(undone).isTrue();
        assertThat(session.getGameState().getTimeline()).isEqualTo(beforeMove.getTimeline());
        assertThat(session.getGameState().getPlayerHand(currentPlayer)).contains(cardToPlay);
    }

    @Test
    void placeCardAndAdvanceTurn_updatesState() {
        final GameSession session = new GameSession(List.of("P1", "P2"), new CardDeck(sampleCards()), 1);
        final String currentPlayer = session.getGameState().getCurrentPlayer();
        final Card card = session.getGameState().getPlayerHand(currentPlayer).get(0);
        final int position = session.getRules().getCorrectPosition(card);
        session.placeCard(card, position);
        assertThat(session.getGameState().getCurrentPhase()).isIn(GamePhase.CORRECT_PLACEMENT, GamePhase.GAME_OVER);
        session.advanceTurn();
        assertThat(session.getGameState().getCurrentPlayer()).isEqualTo("P2");
    }

    private List<Card> sampleCards() {
        return List.of(
                createCard("1", LocalDate.of(2009, 1, 1)),
                createCard("2", LocalDate.of(2010, 1, 1)),
                createCard("3", LocalDate.of(2011, 1, 1)),
                createCard("4", LocalDate.of(2012, 1, 1)),
                createCard("5", LocalDate.of(2013, 1, 1)),
                createCard("6", LocalDate.of(2014, 1, 1))
        );
    }

    private Card createCard(final String id, final LocalDate date) {
        return new Card(id, "Title " + id, date, "Trivia", "image.png", "1.0");
    }
}
