package com.minecrafttimeline.core.game;

import com.minecrafttimeline.core.card.Card;
import com.minecrafttimeline.core.card.CardDeck;
import com.minecrafttimeline.models.Player;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Objects;

/**
 * Coordinates player sequencing, move history and high-level phase changes.
 */
public final class TurnManager {

    private final List<Player> players;
    private final List<TurnListener> listeners = new ArrayList<>();
    private final Deque<Move> undoStack = new ArrayDeque<>();
    private final Deque<Move> redoStack = new ArrayDeque<>();
    private GameState gameState;
    private int currentPlayerIndex;
    private GamePhase currentPhase = GamePhase.SETUP;

    /**
     * Creates a new manager handling the provided players.
     *
     * @param players participating players; must not be {@code null}
     */
    public TurnManager(final List<Player> players) {
        this.players = new ArrayList<>(Objects.requireNonNull(players, "players must not be null"));
        if (players.isEmpty()) {
            throw new IllegalArgumentException("At least one player is required");
        }
        currentPlayerIndex = 0;
    }

    /**
     * Associates the manager with a {@link GameState} instance.
     *
     * @param gameState state reference; must not be {@code null}
     */
    public void setGameState(final GameState gameState) {
        this.gameState = Objects.requireNonNull(gameState, "gameState must not be null");
        final int statePlayerIndex = gameState.getCurrentPlayerIndex();
        if (statePlayerIndex < 0 || statePlayerIndex >= players.size()) {
            throw new IllegalArgumentException("Current player index out of bounds: " + statePlayerIndex);
        }
        currentPlayerIndex = statePlayerIndex;
        currentPhase = Objects.requireNonNull(gameState.getCurrentPhase(), "gameState phase must not be null");
        this.gameState.setCurrentPlayerIndex(currentPlayerIndex);
        this.gameState.setPhase(currentPhase);
    }

    /**
     * Deals the initial hand of cards to each player.
     *
     * @param deck            deck to draw from; must not be {@code null}
     * @param cardsPerPlayer  number of cards each player should receive
     */
    public void dealInitialCards(final CardDeck deck, final int cardsPerPlayer) {
        Objects.requireNonNull(deck, "deck must not be null");
        if (cardsPerPlayer < 0) {
            throw new IllegalArgumentException("cardsPerPlayer must be non-negative");
        }
        for (final Player player : players) {
            player.clearHand();
            final List<Card> dealt = deck.dealCards(cardsPerPlayer);
            player.addCardsToHand(dealt);
        }
        if (gameState != null) {
            gameState.setCurrentPlayerIndex(currentPlayerIndex);
        }
    }

    /**
     * Returns the currently active player.
     *
     * @return current player
     */
    public Player getCurrentPlayer() {
        return players.get(currentPlayerIndex);
    }

    /**
     * Returns the index of the active player.
     *
     * @return zero-based player index
     */
    public int getCurrentPlayerIndex() {
        return currentPlayerIndex;
    }

    /**
     * Sets the active player index. Intended for restoring persisted games.
     *
     * @param playerIndex new player index
     */
    public void setCurrentPlayerIndex(final int playerIndex) {
        if (playerIndex < 0 || playerIndex >= players.size()) {
            throw new IllegalArgumentException("playerIndex out of bounds: " + playerIndex);
        }
        currentPlayerIndex = playerIndex;
        if (gameState != null) {
            gameState.setCurrentPlayerIndex(currentPlayerIndex);
        }
    }

    /**
     * Advances the turn to the next player in the list.
     */
    public void nextTurn() {
        currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
        if (gameState != null) {
            gameState.setCurrentPlayerIndex(currentPlayerIndex);
        }
        setPhase(GamePhase.PLAYER_TURN);
        fireTurnChanged();
    }

    /**
     * Determines whether the active player has at least one valid move.
     *
     * @return {@code true} if a valid placement exists, {@code false} otherwise
     */
    public boolean canPlayerMove() {
        if (gameState == null) {
            return false;
        }
        final List<Card> timeline = gameState.getTimeline();
        for (final Card card : getCurrentPlayer().getHand()) {
            if (!GameRules.getValidPositionsForCard(card, timeline).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Applies the provided card placement and updates scores/history.
     *
     * @param card     card being placed; must not be {@code null}
     * @param position desired timeline index
     * @return outcome describing whether the placement was correct, incorrect or invalid
     */
    public PlacementOutcome applyCardPlacement(final Card card, final int position) {
        Objects.requireNonNull(card, "card must not be null");
        if (gameState == null) {
            throw new IllegalStateException("Game state not attached");
        }
        if (!getCurrentPlayer().getHand().contains(card)) {
            throw new IllegalArgumentException("Current player does not possess the specified card");
        }
        setPhase(GamePhase.VALIDATING);
        final List<Card> timelineSnapshot = gameState.getTimeline();
        if (!GameRules.validateCardPlacement(card, timelineSnapshot, position)) {
            setPhase(GamePhase.INCORRECT_PLACEMENT);
            return PlacementOutcome.INVALID;
        }
        final boolean correct = GameRules.isCorrectPlacement(card, timelineSnapshot, position);
        final Player currentPlayer = getCurrentPlayer();
        if (!correct) {
            setPhase(GamePhase.INCORRECT_PLACEMENT);
            return PlacementOutcome.INCORRECT;
        }
        gameState.addCardToTimeline(card, position);
        currentPlayer.removeCardFromHand(card);
        currentPlayer.addToScore(2);
        gameState.setCurrentPlayerIndex(currentPlayerIndex);
        final Move move = new Move(card, position, true, currentPlayerIndex, System.currentTimeMillis());
        setPhase(GamePhase.CORRECT_PLACEMENT);
        recordMove(move);
        if (GameRules.hasPlayerWon(currentPlayer.getHand(), gameState.getTimeline())) {
            setPhase(GamePhase.GAME_OVER);
            fireGameOver(currentPlayer);
        }
        return PlacementOutcome.CORRECT;
    }

    /**
     * Returns the current phase.
     *
     * @return current phase value
     */
    public GamePhase getCurrentPhase() {
        return currentPhase;
    }

    /**
     * Updates the current phase and synchronises it with the state container.
     *
     * @param phase new phase; must not be {@code null}
     */
    public void setPhase(final GamePhase phase) {
        currentPhase = Objects.requireNonNull(phase, "phase must not be null");
        if (gameState != null) {
            gameState.setPhase(currentPhase);
        }
        firePhaseChanged();
    }

    /**
     * Records a move and updates the undo/redo history.
     *
     * @param move move to record; must not be {@code null}
     */
    public void recordMove(final Move move) {
        Objects.requireNonNull(move, "move must not be null");
        undoStack.push(move);
        redoStack.clear();
        if (gameState != null) {
            gameState.addMoveToHistory(move);
        }
        fireMoveEvaluated(move);
    }

    /**
     * Undoes the most recent move if available.
     *
     * @return the undone move or {@code null} when no move can be undone
     */
    public Move undo() {
        if (gameState == null || undoStack.isEmpty()) {
            return null;
        }
        final Move move = undoStack.pop();
        redoStack.push(move);
        final Player player = players.get(move.getPlayerIndex());
        final Card removed = gameState.removeCardFromTimeline(move.getPositionInTimeline());
        if (!removed.equals(move.getCard())) {
            // In case of discrepancies, re-insert the removed card and search for the original.
            gameState.addCardToTimeline(removed, move.getPositionInTimeline());
            final int index = gameState.getTimeline().indexOf(move.getCard());
            if (index >= 0) {
                gameState.removeCardFromTimeline(index);
            }
        }
        player.addCardToHand(move.getCard());
        if (move.isCorrect()) {
            player.addToScore(-2);
        } else {
            player.addToScore(-1);
        }
        gameState.removeLastMoveFromHistory();
        currentPlayerIndex = move.getPlayerIndex();
        gameState.setCurrentPlayerIndex(currentPlayerIndex);
        setPhase(GamePhase.PLAYER_TURN);
        fireTurnChanged();
        return move;
    }

    /**
     * Redoes the most recently undone move.
     *
     * @return the redone move or {@code null} when no move is available
     */
    public Move redo() {
        if (gameState == null || redoStack.isEmpty()) {
            return null;
        }
        final Move move = redoStack.pop();
        final Player player = players.get(move.getPlayerIndex());
        if (!player.getHand().contains(move.getCard())) {
            throw new IllegalStateException("Player hand is inconsistent for redo operation");
        }
        if (!GameRules.validateCardPlacement(move.getCard(), gameState.getTimeline(), move.getPositionInTimeline())) {
            throw new IllegalStateException("Timeline has diverged and redo is impossible");
        }
        gameState.addCardToTimeline(move.getCard(), move.getPositionInTimeline());
        player.removeCardFromHand(move.getCard());
        if (move.isCorrect()) {
            player.addToScore(2);
        } else {
            player.addToScore(1);
        }
        undoStack.push(move);
        if (gameState != null) {
            gameState.addMoveToHistory(move);
        }
        final GamePhase placementPhase = move.isCorrect() ? GamePhase.CORRECT_PLACEMENT : GamePhase.INCORRECT_PLACEMENT;
        setPhase(placementPhase);
        if (GameRules.hasPlayerWon(player.getHand(), gameState.getTimeline())) {
            currentPlayerIndex = move.getPlayerIndex();
            gameState.setCurrentPlayerIndex(currentPlayerIndex);
            setPhase(GamePhase.GAME_OVER);
            fireGameOver(player);
        } else {
            currentPlayerIndex = (move.getPlayerIndex() + 1) % players.size();
            gameState.setCurrentPlayerIndex(currentPlayerIndex);
            fireTurnChanged();
        }
        return move;
    }

    /**
     * Registers a listener interested in turn and phase events.
     *
     * @param listener listener instance; must not be {@code null}
     */
    public void addListener(final TurnListener listener) {
        listeners.add(Objects.requireNonNull(listener, "listener must not be null"));
    }

    /**
     * Removes a previously registered listener.
     *
     * @param listener listener instance; must not be {@code null}
     */
    public void removeListener(final TurnListener listener) {
        listeners.remove(Objects.requireNonNull(listener, "listener must not be null"));
    }

    private void fireTurnChanged() {
        final Player current = getCurrentPlayer();
        for (final TurnListener listener : List.copyOf(listeners)) {
            listener.onTurnStart(current, currentPhase);
        }
    }

    private void firePhaseChanged() {
        for (final TurnListener listener : List.copyOf(listeners)) {
            listener.onPhaseChanged(currentPhase);
        }
    }

    private void fireMoveEvaluated(final Move move) {
        for (final TurnListener listener : List.copyOf(listeners)) {
            listener.onMoveEvaluated(move);
        }
    }

    private void fireGameOver(final Player winner) {
        for (final TurnListener listener : List.copyOf(listeners)) {
            listener.onGameOver(winner);
        }
    }

    /**
     * Listener interface used to receive callbacks for turn/phase changes.
     */
    public interface TurnListener {
        /**
         * Called whenever a new turn starts.
         *
         * @param currentPlayer the player whose turn has begun
         * @param phase         the current phase
         */
        void onTurnStart(Player currentPlayer, GamePhase phase);

        /**
         * Called when the phase changes.
         *
         * @param phase new phase
         */
        void onPhaseChanged(GamePhase phase);

        /**
         * Called after a move has been evaluated (correct or incorrect).
         *
         * @param move move details
         */
        void onMoveEvaluated(Move move);

        /**
         * Called when the game reaches a terminal state.
         *
         * @param winner winning player
         */
        void onGameOver(Player winner);
    }
}
