package com.minecrafttimeline.core.game;

import com.minecrafttimeline.core.card.Card;
import com.minecrafttimeline.core.card.CardDeck;
import com.minecrafttimeline.models.Player;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

/**
 * High-level orchestrator that combines state, rules and turn management.
 */
public final class GameSession {

    private static final int DEFAULT_CARDS_PER_PLAYER = 4;

    private GameState gameState;
    private TurnManager turnManager;
    private CardDeck deck;
    private int cardsPerPlayer = DEFAULT_CARDS_PER_PLAYER;

    /**
     * Starts a brand new game using the provided players and deck.
     *
     * @param players list of participating players; must not be {@code null}
     * @param deck    deck to draw cards from; must not be {@code null}
     */
    public void startNewGame(final List<Player> players, final CardDeck deck) {
        Objects.requireNonNull(players, "players must not be null");
        Objects.requireNonNull(deck, "deck must not be null");
        if (players.isEmpty()) {
            throw new IllegalArgumentException("At least one player required");
        }
        this.deck = deck;
        this.gameState = new GameState(players);
        this.turnManager = new TurnManager(players);
        this.turnManager.setGameState(gameState);
        this.turnManager.dealInitialCards(deck, cardsPerPlayer);
        this.turnManager.setPhase(GamePhase.PLAYER_TURN);
    }

    /**
     * Attempts to place a card on the timeline.
     *
     * @param card     card to place; must not be {@code null}
     * @param position desired timeline position
     * @return {@code true} when the placement is judged correct (within tolerance)
     */
    public boolean placeCard(final Card card, final int position) {
        Objects.requireNonNull(card, "card must not be null");
        ensureInitialised();
        final int beforeSize = gameState.getTimeline().size();
        final boolean result = turnManager.applyCardPlacement(card, position);
        final int afterSize = gameState.getTimeline().size();
        final boolean cardPlaced = afterSize > beforeSize;
        if (cardPlaced && !gameState.isGameOver()) {
            turnManager.nextTurn();
        }
        return result;
    }

    /**
     * Returns the current game phase.
     *
     * @return phase value
     */
    public GamePhase getCurrentPhase() {
        ensureInitialised();
        return gameState.getCurrentPhase();
    }

    /**
     * Performs an undo operation if possible.
     *
     * @return {@code true} if a move was undone
     */
    public boolean undo() {
        ensureInitialised();
        return turnManager.undo() != null;
    }

    /**
     * Performs a redo operation if possible.
     *
     * @return {@code true} if a move was re-applied
     */
    public boolean redo() {
        ensureInitialised();
        return turnManager.redo() != null;
    }

    /**
     * Provides a textual summary suitable for debug output or a HUD.
     *
     * @return summary string
     */
    public String getGameStatus() {
        ensureInitialised();
        final Player current = turnManager.getCurrentPlayer();
        return "Phase: " + gameState.getCurrentPhase() +
                ", Player: " + current.getName() +
                ", Timeline Cards: " + gameState.getTimeline().size();
    }

    /**
     * Saves the current game state to disk.
     *
     * @param filename destination file; must not be {@code null}
     * @throws IOException in case of I/O issues
     */
    public void saveGame(final String filename) throws IOException {
        ensureInitialised();
        gameState.saveToFile(filename);
    }

    /**
     * Loads a game state from disk and returns a configured session.
     *
     * @param filename source file name; must not be {@code null}
     * @return populated session instance
     * @throws IOException in case of I/O errors
     */
    public static GameSession loadGame(final String filename) throws IOException {
        final GameState state = GameState.loadFromFile(filename);
        final TurnManager manager = new TurnManager(state.getPlayers());
        manager.setGameState(state);
        manager.setCurrentPlayerIndex(state.getCurrentPlayerIndex());
        manager.setPhase(state.getCurrentPhase());
        final GameSession session = new GameSession();
        session.gameState = state;
        session.turnManager = manager;
        return session;
    }

    /**
     * Indicates whether the game is over.
     *
     * @return {@code true} when the session has finished
     */
    public boolean isGameOver() {
        ensureInitialised();
        return gameState.isGameOver();
    }

    /**
     * Returns the player with the highest score. In case of a tie {@code null} is returned.
     *
     * @return winning player or {@code null} if no single winner exists
     */
    public Player getWinner() {
        ensureInitialised();
        Player winner = null;
        boolean tie = false;
        for (final Player player : gameState.getPlayers()) {
            if (winner == null || player.getScore() > winner.getScore()) {
                winner = player;
                tie = false;
            } else if (winner != null && player.getScore() == winner.getScore()) {
                tie = true;
            }
        }
        return tie ? null : winner;
    }

    /**
     * Changes the number of cards dealt to each player at the start of a game.
     *
     * @param cardsPerPlayer number of cards; must be positive
     */
    public void setCardsPerPlayer(final int cardsPerPlayer) {
        if (cardsPerPlayer <= 0) {
            throw new IllegalArgumentException("cardsPerPlayer must be positive");
        }
        this.cardsPerPlayer = cardsPerPlayer;
    }

    public GameState getGameState() {
        return gameState;
    }

    public TurnManager getTurnManager() {
        return turnManager;
    }

    private void ensureInitialised() {
        if (gameState == null || turnManager == null) {
            throw new IllegalStateException("Game has not been started");
        }
    }
}
