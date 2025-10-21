package com.minecrafttimeline.game;

import com.minecrafttimeline.cards.Card;
import com.minecrafttimeline.cards.CardDeck;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GameIntegrationTest {

    @Test
    void playFullGameScenario_reachesGameOver() {
        final List<Card> cards = List.of(
                createCard("1", LocalDate.of(2009, 1, 1)),
                createCard("2", LocalDate.of(2010, 1, 1)),
                createCard("3", LocalDate.of(2011, 1, 1)),
                createCard("4", LocalDate.of(2012, 1, 1)),
                createCard("5", LocalDate.of(2013, 1, 1))
        );
        final GameSession session = new GameSession(List.of("P1", "P2"), new CardDeck(cards), 1);
        while (!session.getRules().hasPlayerWon()) {
            final GameState state = session.getGameState();
            final String player = state.getCurrentPlayer();
            final Card card = state.getPlayerHand(player).get(0);
            final int position = session.getRules().getCorrectPosition(card);
            session.placeCard(card, position);
            if (session.getGameState().getCurrentPhase() != GamePhase.GAME_OVER) {
                session.advanceTurn();
            }
        }
        assertThat(session.getGameState().getCurrentPhase()).isEqualTo(GamePhase.GAME_OVER);
        assertThat(session.getRules().hasPlayerWon()).isTrue();
    }

    private Card createCard(final String id, final LocalDate date) {
        return new Card(id, "Title " + id, date, "Trivia", "image.png", "1.0");
    }
}
