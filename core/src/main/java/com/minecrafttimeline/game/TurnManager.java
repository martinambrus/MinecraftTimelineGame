package com.minecrafttimeline.game;

import com.minecrafttimeline.cards.Card;
import com.minecrafttimeline.cards.CardDeck;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Coordinates turn progression, card dealing and move application for Minecraft Timeline.
 */
public final class TurnManager {

    private final List<String> playerOrder;
    private final CardDeck deck;
    private final GameRules rules;
    private final List<TurnListener> listeners = new CopyOnWriteArrayList<>();
    private int currentPlayerIndex;

    /**
     * Creates a new manager.
     *
     * @param playerOrder the order in which players take turns
     * @param deck        the deck used to deal cards to players
     * @param rules       the rules engine used to validate moves
     */
    public TurnManager(final List<String> playerOrder, final CardDeck deck, final GameRules rules) {
        this.playerOrder = List.copyOf(Objects.requireNonNull(playerOrder, "playerOrder"));
        this.deck = Objects.requireNonNull(deck, "deck");
        this.rules = Objects.requireNonNull(rules, "rules");
        this.currentPlayerIndex = 0;
    }

    /**
     * Registers a listener for turn based events.
     *
     * @param listener the listener to register
     */
    public void addListener(final TurnListener listener) {
        listeners.add(Objects.requireNonNull(listener, "listener"));
    }

    /**
     * Removes a previously registered listener.
     *
     * @param listener the listener to remove
     */
    public void removeListener(final TurnListener listener) {
        listeners.remove(listener);
    }

    /**
     * Deals the requested number of cards to each player, returning an updated {@link GameState}.
     *
     * @param state          the current game state
     * @param cardsPerPlayer the number of cards to deal to each player
     * @return the updated state after dealing
     */
    public GameState dealInitialCards(final GameState state, final int cardsPerPlayer) {
        Objects.requireNonNull(state, "state");
        if (cardsPerPlayer < 0) {
            throw new IllegalArgumentException("cardsPerPlayer must not be negative");
        }
        if (state.getPlayers().isEmpty()) {
            throw new IllegalStateException("No players available to deal cards to");
        }
        final int totalRequired = cardsPerPlayer * state.getPlayers().size();
        if (totalRequired > deck.size()) {
            throw new IllegalStateException("Not enough cards remaining in the deck to deal");
        }
        final Map<String, List<Card>> newHands = new LinkedHashMap<>();
        for (final String player : state.getPlayers()) {
            final List<Card> dealt = deck.dealCards(cardsPerPlayer);
            newHands.put(player, new ArrayList<>(dealt));
        }
        List<Card> newTimeline = state.getTimeline();
        if (newTimeline.isEmpty() && deck.size() > 0) {
            newTimeline = List.of(deck.dealCards(1).get(0));
        }
        final GameState newState = GameState.from(state)
                .phase(GamePhase.PLAYER_TURN)
                .currentPlayer(playerOrder.get(0))
                .timeline(newTimeline)
                .hands(newHands)
                .scores(initialiseScores(state))
                .build();
        synchroniseCurrentPlayer(newState.getCurrentPlayer());
        notifyTurnChanged(newState);
        return newState;
    }

    private Map<String, Integer> initialiseScores(final GameState state) {
        final Map<String, Integer> scores = new LinkedHashMap<>();
        for (final String player : state.getPlayers()) {
            scores.put(player, state.getScore(player));
        }
        return scores;
    }

    /**
     * Advances to the next player's turn.
     *
     * @param state the current game state
     * @return the updated state for the next player
     */
    public GameState nextTurn(final GameState state) {
        Objects.requireNonNull(state, "state");
        if (playerOrder.isEmpty()) {
            return state;
        }
        currentPlayerIndex = (currentPlayerIndex + 1) % playerOrder.size();
        final GameState newState = GameState.from(state)
                .phase(GamePhase.PLAYER_TURN)
                .currentPlayer(playerOrder.get(currentPlayerIndex))
                .build();
        notifyTurnChanged(newState);
        return newState;
    }

    /**
     * Retrieves the identifier of the active player.
     *
     * @param state the current game state
     * @return the active player's identifier
     */
    public String getCurrentPlayer(final GameState state) {
        Objects.requireNonNull(state, "state");
        return state.getCurrentPlayer();
    }

    /**
     * Determines whether the current player has a legal move available.
     *
     * @param state the game state to inspect
     * @return {@code true} when the player can play a card, otherwise {@code false}
     */
    public boolean canPlayerMove(final GameState state) {
        Objects.requireNonNull(state, "state");
        final String currentPlayer = state.getCurrentPlayer();
        return currentPlayer != null && !state.getPlayerHand(currentPlayer).isEmpty();
    }

    /**
     * Applies the requested card placement, returning an updated {@link GameState}.
     *
     * @param state    the current state
     * @param card     the card to place
     * @param position the timeline slot to insert at
     * @return the new state reflecting the placement result
     */
    public GameState applyCardPlacement(final GameState state, final Card card, final int position) {
        Objects.requireNonNull(state, "state");
        Objects.requireNonNull(card, "card");
        if (state.getCurrentPlayer() == null) {
            throw new IllegalStateException("No active player to perform the move");
        }
        final List<Card> playerHand = new ArrayList<>(state.getPlayerHand(state.getCurrentPlayer()));
        if (!playerHand.contains(card)) {
            throw new IllegalArgumentException("Card does not belong to the active player");
        }
        final GameState validatingState = state.setPhase(GamePhase.VALIDATING);
        if (!rules.validateCardPlacement(card, position)) {
            final GameState invalidState = GameState.from(validatingState)
                    .phase(GamePhase.INCORRECT_PLACEMENT)
                    .build();
            notifyIncorrectPlacement(invalidState, card, position);
            return invalidState;
        }
        playerHand.remove(card);
        final Map<String, List<Card>> updatedHands = new LinkedHashMap<>(state.getHands());
        updatedHands.put(state.getCurrentPlayer(), playerHand);
        final List<Card> updatedTimeline = new ArrayList<>(state.getTimeline());
        updatedTimeline.add(position, card);
        final Map<String, Integer> updatedScores = new LinkedHashMap<>(state.getScores());
        updatedScores.merge(state.getCurrentPlayer(), 1, Integer::sum);
        final List<GameState.GameSnapshot> history = new ArrayList<>(state.getMoveHistory());
        history.add(state.createSnapshot());
        final GamePhase resultingPhase = playerHand.isEmpty() && allHandsEmpty(updatedHands)
                ? GamePhase.GAME_OVER
                : GamePhase.CORRECT_PLACEMENT;
        final GameState newState = GameState.from(state)
                .phase(resultingPhase)
                .timeline(updatedTimeline)
                .hands(updatedHands)
                .scores(updatedScores)
                .history(history)
                .build();
        notifyCorrectPlacement(newState, card, position);
        return newState;
    }

    private boolean allHandsEmpty(final Map<String, List<Card>> hands) {
        return hands.values().stream().allMatch(List::isEmpty);
    }

    private void notifyTurnChanged(final GameState state) {
        for (final TurnListener listener : listeners) {
            listener.onTurnChanged(state.getCurrentPlayer(), state.getCurrentPhase());
        }
    }

    private void notifyCorrectPlacement(final GameState state, final Card card, final int position) {
        for (final TurnListener listener : listeners) {
            listener.onCorrectPlacement(state.getCurrentPlayer(), card, position);
        }
    }

    private void notifyIncorrectPlacement(final GameState state, final Card card, final int position) {
        for (final TurnListener listener : listeners) {
            listener.onIncorrectPlacement(state.getCurrentPlayer(), card, position);
        }
    }

    /**
     * Aligns the internal player index with the provided identifier. Used when loading games.
     *
     * @param playerId the active player's identifier
     */
    public void synchroniseCurrentPlayer(final String playerId) {
        if (playerId == null) {
            currentPlayerIndex = 0;
            return;
        }
        final int index = playerOrder.indexOf(playerId);
        if (index >= 0) {
            currentPlayerIndex = index;
        }
    }

    /**
     * Listener interface for turn-based events.
     */
    public interface TurnListener {
        void onTurnChanged(String currentPlayerId, GamePhase phase);

        void onCorrectPlacement(String playerId, Card card, int position);

        void onIncorrectPlacement(String playerId, Card card, int position);
    }
}
