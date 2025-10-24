package com.minecrafttimeline.integration;

import com.minecrafttimeline.core.card.Card;
import com.minecrafttimeline.core.card.CardDeck;
import com.minecrafttimeline.core.game.GamePhase;
import com.minecrafttimeline.core.game.GameSession;
import com.minecrafttimeline.models.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GamePlayIntegrationTest {

    private GameSession session;
    private List<Player> players;

    @BeforeEach
    void setUp() {
        session = new GameSession();
        players = List.of(new Player("p1", "Alex"), new Player("p2", "Blake"));
        final CardDeck deck = new CardDeck(List.of(
                card("c1", 1990),
                card("c2", 2000),
                card("c3", 2010),
                card("c4", 2020),
                card("c5", 2030),
                card("c6", 2040)));
        session.setCardsPerPlayer(2);
        session.startNewGame(players, deck);
    }

    @Test
    void fullGamePlaythrough() {
        assertThat(session.getGameState().getTimeline()).hasSize(1);
        final Card baseCard = session.getGameState().getTimeline().get(0);
        final Card firstPlay = players.get(0).getHand().get(0);
        assertThat(session.placeCard(firstPlay, 0)).isTrue();
        assertThat(session.getGameState().getTimeline()).containsExactly(firstPlay, baseCard);

        final Card secondPlayerCard = players.get(1).getHand().get(0);
        assertThat(session.placeCard(secondPlayerCard, 0)).isFalse();
        assertThat(session.getGameState().getTimeline()).containsExactly(firstPlay, baseCard);
        assertThat(session.getTurnManager().getCurrentPlayer()).isEqualTo(players.get(1));

        assertThat(session.placeCard(secondPlayerCard, 1)).isTrue();
        assertThat(session.getGameState().getTimeline()).containsExactly(firstPlay, secondPlayerCard, baseCard);

        final Card secondCardFirstPlayer = players.get(0).getHand().get(0);
        assertThat(session.placeCard(secondCardFirstPlayer, 1)).isTrue();
        assertThat(session.getGameState().getTimeline()).containsExactly(firstPlay, secondCardFirstPlayer, secondPlayerCard, baseCard);

        final Card finalCard = players.get(1).getHand().get(0);
        assertThat(session.placeCard(finalCard, 3)).isTrue();
        assertThat(session.getGameState().getTimeline()).containsExactly(firstPlay, secondCardFirstPlayer, secondPlayerCard, finalCard, baseCard);
        assertThat(session.isGameOver()).isTrue();
        assertThat(session.getCurrentPhase()).isEqualTo(GamePhase.GAME_OVER);
        assertThat(session.getWinner()).isNull();

        final String status = session.getGameStatus();
        assertThat(status).contains("Phase: GAME_OVER");
        assertThat(status).contains("Timeline Cards: 5");

        assertThat(session.undo()).isTrue();
        assertThat(session.isGameOver()).isFalse();
        assertThat(session.getGameState().getTimeline()).hasSize(4);

        assertThat(session.redo()).isTrue();
        assertThat(session.isGameOver()).isTrue();
        assertThat(session.getGameState().getTimeline()).hasSize(5);
        assertThat(session.getCurrentPhase()).isEqualTo(GamePhase.GAME_OVER);
    }

    private static Card card(final String id, final int year) {
        return new Card(id, "title-" + id, LocalDate.of(year, 1, 1), "trivia", "image", "version");
    }
}
