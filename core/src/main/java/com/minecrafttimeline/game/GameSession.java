package com.minecrafttimeline.game;

import com.minecrafttimeline.cards.Card;
import com.minecrafttimeline.cards.CardDeck;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Coordinates the interaction between the game state, rules and turn management systems.
 */
public final class GameSession {

    private GameState gameState;
    private final GameRules rules;
    private final TurnManager turnManager;

    /**
     * Creates a new session initialised with the supplied players and deck.
     *
     * @param playerIds      ordered list of player identifiers
     * @param deck           the deck of cards used for the match
     * @param cardsPerPlayer the number of cards dealt to each player at the start of the game
     */
    public GameSession(final List<String> playerIds, final CardDeck deck, final int cardsPerPlayer) {
        Objects.requireNonNull(playerIds, "playerIds");
        Objects.requireNonNull(deck, "deck");
        if (playerIds.isEmpty()) {
            throw new IllegalArgumentException("At least one player is required");
        }
        if (cardsPerPlayer < 0) {
            throw new IllegalArgumentException("cardsPerPlayer must not be negative");
        }
        final Map<String, List<Card>> emptyHands = new LinkedHashMap<>();
        final Map<String, Integer> emptyScores = new LinkedHashMap<>();
        for (final String player : playerIds) {
            emptyHands.put(player, List.of());
            emptyScores.put(player, 0);
        }
        this.gameState = GameState.builder()
                .players(playerIds)
                .phase(GamePhase.SETUP)
                .currentPlayer(playerIds.get(0))
                .timeline(List.of())
                .hands(emptyHands)
                .scores(emptyScores)
                .history(List.of())
                .build();
        this.rules = new GameRules(gameState);
        this.turnManager = new TurnManager(playerIds, deck, rules);
        this.gameState = turnManager.dealInitialCards(gameState, cardsPerPlayer);
        this.rules.updateState(gameState);
    }

    /**
     * Loads the provided {@link GameState} as the authoritative state.
     *
     * @param state the state to load
     */
    public void loadGame(final GameState state) {
        applyState(Objects.requireNonNull(state, "state"));
        turnManager.synchroniseCurrentPlayer(gameState.getCurrentPlayer());
    }

    /**
     * Serialises the current session into a map suitable for persistence.
     *
     * @return a serialisable representation of the session state
     */
    public Map<String, Object> saveGame() {
        final Map<String, Object> serialised = new LinkedHashMap<>();
        serialised.put("phase", gameState.getCurrentPhase().name());
        serialised.put("currentPlayer", gameState.getCurrentPlayer());
        final List<Map<String, Object>> timeline = new ArrayList<>();
        for (final Card card : gameState.getTimeline()) {
            timeline.add(card.toSerializableMap());
        }
        serialised.put("timeline", timeline);
        final Map<String, List<Map<String, Object>>> hands = new LinkedHashMap<>();
        for (final Map.Entry<String, List<Card>> entry : gameState.getHands().entrySet()) {
            final List<Map<String, Object>> cards = new ArrayList<>();
            for (final Card card : entry.getValue()) {
                cards.add(card.toSerializableMap());
            }
            hands.put(entry.getKey(), cards);
        }
        serialised.put("hands", hands);
        serialised.put("scores", new LinkedHashMap<>(gameState.getScores()));
        return serialised;
    }

    /**
     * Undoes the most recent move, if possible.
     *
     * @return {@code true} when a move was undone, otherwise {@code false}
     */
    public boolean undo() {
        final List<GameState.GameSnapshot> history = new ArrayList<>(gameState.getMoveHistory());
        if (history.isEmpty()) {
            return false;
        }
        final GameState.GameSnapshot snapshot = history.remove(history.size() - 1);
        final GameState restored = GameState.fromSnapshot(snapshot, history);
        applyState(restored);
        turnManager.synchroniseCurrentPlayer(gameState.getCurrentPlayer());
        return true;
    }

    /**
     * Applies a placement made by the current player.
     *
     * @param card     the card being placed
     * @param position the target timeline index
     */
    public void placeCard(final Card card, final int position) {
        final GameState updated = turnManager.applyCardPlacement(gameState, card, position);
        applyState(updated);
    }

    /**
     * Advances the game to the next player's turn.
     */
    public void advanceTurn() {
        final GameState updated = turnManager.nextTurn(gameState);
        applyState(updated);
    }

    /**
     * Provides the authoritative {@link GameState}.
     *
     * @return the current state
     */
    public GameState getGameState() {
        return gameState;
    }

    /**
     * Exposes the {@link GameRules} in use.
     *
     * @return the rules engine
     */
    public GameRules getRules() {
        return rules;
    }

    /**
     * Exposes the {@link TurnManager}.
     *
     * @return the turn manager
     */
    public TurnManager getTurnManager() {
        return turnManager;
    }

    private void applyState(final GameState newState) {
        this.gameState = Objects.requireNonNull(newState, "newState");
        this.rules.updateState(gameState);
    }
}
