package com.minecrafttimeline.game;

import com.minecrafttimeline.cards.Card;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Immutable representation of the current logical state of a Minecraft Timeline match.
 * <p>
 * The class is deliberately defensive: every method that adjusts state returns a new
 * {@link GameState} instance. This design ensures that the {@link GameSession} can act as the
 * single source of truth for the game whilst still supporting undo/redo via the {@link #moveHistory}.
 */
public final class GameState {

    private final GamePhase currentPhase;
    private final String currentPlayerId;
    private final List<Card> timeline;
    private final Map<String, List<Card>> hands;
    private final Map<String, Integer> scores;
    private final List<String> players;
    private final List<GameSnapshot> moveHistory;

    private GameState(
            final GamePhase currentPhase,
            final String currentPlayerId,
            final List<Card> timeline,
            final Map<String, List<Card>> hands,
            final Map<String, Integer> scores,
            final List<String> players,
            final List<GameSnapshot> moveHistory) {
        this.currentPhase = Objects.requireNonNull(currentPhase, "currentPhase");
        this.players = List.copyOf(Objects.requireNonNull(players, "players"));
        if (!players.isEmpty() && currentPlayerId == null) {
            throw new IllegalArgumentException("currentPlayerId must be present when players exist");
        }
        if (!players.isEmpty() && !players.contains(currentPlayerId)) {
            throw new IllegalArgumentException("currentPlayerId must exist in players list");
        }
        this.currentPlayerId = currentPlayerId;
        this.timeline = validateTimeline(Objects.requireNonNull(timeline, "timeline"));
        this.hands = createHandsCopy(Objects.requireNonNull(hands, "hands"));
        this.scores = Map.copyOf(Objects.requireNonNull(scores, "scores"));
        this.moveHistory = List.copyOf(Objects.requireNonNull(moveHistory, "moveHistory"));
    }

    private static List<Card> validateTimeline(final List<Card> proposed) {
        final List<Card> copy = List.copyOf(proposed);
        for (int i = 1; i < copy.size(); i++) {
            if (copy.get(i - 1).date().isAfter(copy.get(i).date())) {
                throw new IllegalArgumentException("Timeline must be sorted chronologically");
            }
        }
        return copy;
    }

    private static Map<String, List<Card>> createHandsCopy(final Map<String, List<Card>> source) {
        final Map<String, List<Card>> copy = new LinkedHashMap<>();
        source.forEach((player, cards) -> copy.put(player, List.copyOf(cards)));
        return Collections.unmodifiableMap(copy);
    }

    /**
     * Creates a builder for constructing new immutable {@link GameState} instances.
     *
     * @return a new builder with default values
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Creates a builder that is initialised with the current state's data.
     *
     * @param state the state to copy
     * @return a builder initialised from {@code state}
     */
    public static Builder from(final GameState state) {
        return new Builder(state);
    }

    /**
     * Retrieves the phase describing the game's current progress.
     *
     * @return the current phase
     */
    public GamePhase getCurrentPhase() {
        return currentPhase;
    }

    /**
     * Returns a copy of the state with the provided phase applied.
     *
     * @param newPhase the desired phase
     * @return a new {@link GameState} reflecting the phase change
     */
    public GameState setPhase(final GamePhase newPhase) {
        return GameState.from(this)
                .phase(newPhase)
                .build();
    }

    /**
     * Provides the identifier of the active player.
     *
     * @return the active player's identifier
     */
    public String getCurrentPlayer() {
        return currentPlayerId;
    }

    /**
     * Returns the immutable ordered timeline of cards currently placed.
     *
     * @return the ordered timeline
     */
    public List<Card> getTimeline() {
        return timeline;
    }

    /**
     * Retrieves the hand for the specified player.
     *
     * @param playerId the player identifier
     * @return an immutable list containing the cards in the player's hand
     */
    public List<Card> getPlayerHand(final String playerId) {
        return hands.getOrDefault(playerId, List.of());
    }

    /**
     * Obtains the current score for the provided player.
     *
     * @param playerId the player identifier
     * @return the player's score, defaulting to zero when unknown
     */
    public int getScore(final String playerId) {
        return scores.getOrDefault(playerId, 0);
    }

    /**
     * Provides an immutable view of the registered players in turn order.
     *
     * @return the immutable player list
     */
    public List<String> getPlayers() {
        return players;
    }

    /**
     * Supplies the full mapping of player identifiers to their hands.
     *
     * @return an immutable map containing hand data
     */
    public Map<String, List<Card>> getHands() {
        return hands;
    }

    /**
     * Supplies the score table for the session.
     *
     * @return the immutable mapping of player identifiers to scores
     */
    public Map<String, Integer> getScores() {
        return scores;
    }

    /**
     * Provides the recorded move history snapshots for undo/redo operations.
     *
     * @return the immutable history list
     */
    public List<GameSnapshot> getMoveHistory() {
        return moveHistory;
    }

    /**
     * Creates an immutable snapshot representing the current state.
     *
     * @return the created snapshot
     */
    public GameSnapshot createSnapshot() {
        return new GameSnapshot(currentPhase, currentPlayerId, timeline, hands, scores, players);
    }

    /**
     * Returns a new {@link GameState} produced from the supplied snapshot and history list.
     *
     * @param snapshot the snapshot containing the desired state information
     * @param history  the history to attach to the restored state
     * @return a new {@link GameState} reconstructed from the snapshot
     */
    public static GameState fromSnapshot(final GameSnapshot snapshot, final List<GameSnapshot> history) {
        Objects.requireNonNull(snapshot, "snapshot");
        Objects.requireNonNull(history, "history");
        return GameState.builder()
                .phase(snapshot.phase())
                .currentPlayer(snapshot.currentPlayerId())
                .timeline(snapshot.timeline())
                .hands(snapshot.hands())
                .scores(snapshot.scores())
                .players(snapshot.players())
                .history(history)
                .build();
    }

    /**
     * Builder for {@link GameState} instances.
     */
    public static final class Builder {
        private GamePhase phase = GamePhase.SETUP;
        private String currentPlayerId;
        private List<Card> timeline = List.of();
        private Map<String, List<Card>> hands = Map.of();
        private Map<String, Integer> scores = Map.of();
        private List<String> players = List.of();
        private List<GameSnapshot> history = List.of();

        private Builder() {
        }

        private Builder(final GameState state) {
            this.phase = state.currentPhase;
            this.currentPlayerId = state.currentPlayerId;
            this.timeline = state.timeline;
            this.hands = state.hands;
            this.scores = state.scores;
            this.players = state.players;
            this.history = state.moveHistory;
        }

        public Builder phase(final GamePhase phase) {
            this.phase = Objects.requireNonNull(phase, "phase");
            return this;
        }

        public Builder currentPlayer(final String currentPlayer) {
            this.currentPlayerId = currentPlayer;
            return this;
        }

        public Builder timeline(final List<Card> timeline) {
            this.timeline = new ArrayList<>(Objects.requireNonNull(timeline, "timeline"));
            return this;
        }

        public Builder hands(final Map<String, List<Card>> hands) {
            final Map<String, List<Card>> copy = new LinkedHashMap<>();
            hands.forEach((player, cards) -> copy.put(player, new ArrayList<>(cards)));
            this.hands = copy;
            return this;
        }

        public Builder scores(final Map<String, Integer> scores) {
            this.scores = new LinkedHashMap<>(Objects.requireNonNull(scores, "scores"));
            return this;
        }

        public Builder players(final List<String> players) {
            this.players = new ArrayList<>(Objects.requireNonNull(players, "players"));
            return this;
        }

        public Builder history(final List<GameSnapshot> history) {
            this.history = new ArrayList<>(Objects.requireNonNull(history, "history"));
            return this;
        }

        public GameState build() {
            if (players.isEmpty() && currentPlayerId != null) {
                throw new IllegalStateException("Cannot have a current player without a player list");
            }
            return new GameState(phase, currentPlayerId, timeline, hands, scores, players, history);
        }
    }

    /**
     * Immutable snapshot capturing a moment in the game's history.
     */
    public static final class GameSnapshot {
        private final GamePhase phase;
        private final String currentPlayerId;
        private final List<Card> timeline;
        private final Map<String, List<Card>> hands;
        private final Map<String, Integer> scores;
        private final List<String> players;

        private GameSnapshot(
                final GamePhase phase,
                final String currentPlayerId,
                final List<Card> timeline,
                final Map<String, List<Card>> hands,
                final Map<String, Integer> scores,
                final List<String> players) {
            this.phase = Objects.requireNonNull(phase, "phase");
            this.currentPlayerId = currentPlayerId;
            this.timeline = List.copyOf(Objects.requireNonNull(timeline, "timeline"));
            this.hands = createHandsCopy(Objects.requireNonNull(hands, "hands"));
            this.scores = Map.copyOf(Objects.requireNonNull(scores, "scores"));
            this.players = List.copyOf(Objects.requireNonNull(players, "players"));
        }

        public GamePhase phase() {
            return phase;
        }

        public String currentPlayerId() {
            return currentPlayerId;
        }

        public List<Card> timeline() {
            return timeline;
        }

        public Map<String, List<Card>> hands() {
            return hands;
        }

        public Map<String, Integer> scores() {
            return scores;
        }

        public List<String> players() {
            return players;
        }
    }
}
