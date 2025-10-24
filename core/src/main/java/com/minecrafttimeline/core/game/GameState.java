package com.minecrafttimeline.core.game;

import com.minecrafttimeline.core.card.Card;
import com.minecrafttimeline.models.Player;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Container object encapsulating the mutable state of an active game.
 * <p>
 * The class favours immutability for external consumers by exposing defensive
 * copies of internal collections. Mutations occur through dedicated methods
 * that maintain invariants such as hand synchronisation and timeline ordering
 * (enforced by the rules engine).
 */
public final class GameState {

    private GamePhase currentPhase;
    private int currentPlayerIndex;
    private final List<Player> players;
    private final List<Card> timeline;
    private final List<Card> hand;
    private final List<Card> discardPile;
    private final List<Move> moveHistory;
    private long gameStartTime;

    /**
     * Creates a new state container initialised with the provided players.
     *
     * @param players list of players participating in the game; must not be {@code null}
     */
    public GameState(final List<Player> players) {
        this(GamePhase.SETUP, 0, players, List.of(), List.of(), List.of(), List.of(), System.currentTimeMillis());
    }

    private GameState(
            final GamePhase phase,
            final int currentPlayerIndex,
            final List<Player> players,
            final List<Card> timeline,
            final List<Card> hand,
            final List<Card> discardPile,
            final List<Move> moveHistory,
            final long gameStartTime) {
        this.currentPhase = Objects.requireNonNull(phase, "phase must not be null");
        this.currentPlayerIndex = currentPlayerIndex;
        this.players = new ArrayList<>(Objects.requireNonNull(players, "players must not be null"));
        this.timeline = new ArrayList<>(Objects.requireNonNull(timeline, "timeline must not be null"));
        this.hand = new ArrayList<>(Objects.requireNonNull(hand, "hand must not be null"));
        this.discardPile = new ArrayList<>(Objects.requireNonNull(discardPile, "discardPile must not be null"));
        this.moveHistory = new ArrayList<>(Objects.requireNonNull(moveHistory, "moveHistory must not be null"));
        this.gameStartTime = gameStartTime;
        updateHandSnapshot();
    }

    /**
     * Returns the current phase of the game.
     *
     * @return current game phase
     */
    public GamePhase getCurrentPhase() {
        return currentPhase;
    }

    /**
     * Returns the index of the currently active player.
     *
     * @return zero-based player index
     */
    public int getCurrentPlayerIndex() {
        return currentPlayerIndex;
    }

    /**
     * Returns an immutable view of the participating players.
     *
     * @return players list
     */
    public List<Player> getPlayers() {
        return Collections.unmodifiableList(players);
    }

    /**
     * Returns a defensive copy of the timeline.
     *
     * @return immutable list of cards in chronological order
     */
    public List<Card> getTimeline() {
        return List.copyOf(timeline);
    }

    /**
     * Returns a defensive copy of the active player's hand snapshot.
     *
     * @return immutable list of cards in the active player's hand
     */
    public List<Card> getHand() {
        return List.copyOf(hand);
    }

    /**
     * Returns a defensive copy of the discard pile.
     *
     * @return immutable list of discarded cards
     */
    public List<Card> getDiscardPile() {
        return List.copyOf(discardPile);
    }

    /**
     * Returns a defensive copy of the executed move history.
     *
     * @return immutable list of moves
     */
    public List<Move> getMoveHistory() {
        return List.copyOf(moveHistory);
    }

    /**
     * Returns the timestamp when the game started.
     *
     * @return epoch millis start time
     */
    public long getGameStartTime() {
        return gameStartTime;
    }

    /**
     * Updates the current phase of the game.
     *
     * @param phase new phase; must not be {@code null}
     */
    public void setPhase(final GamePhase phase) {
        currentPhase = Objects.requireNonNull(phase, "phase must not be null");
    }

    /**
     * Changes the active player index and refreshes the hand snapshot.
     *
     * @param playerIndex zero-based player index
     */
    public void setCurrentPlayerIndex(final int playerIndex) {
        if (playerIndex < 0 || playerIndex >= players.size()) {
            throw new IllegalArgumentException("playerIndex out of bounds: " + playerIndex);
        }
        currentPlayerIndex = playerIndex;
        updateHandSnapshot();
    }

    /**
     * Inserts the provided card into the timeline at the specified position.
     *
     * @param card     card to add; must not be {@code null}
     * @param position zero-based insertion position (0..timeline size)
     */
    public void addCardToTimeline(final Card card, final int position) {
        Objects.requireNonNull(card, "card must not be null");
        if (position < 0 || position > timeline.size()) {
            throw new IllegalArgumentException("position out of bounds: " + position);
        }
        timeline.add(position, card);
    }

    /**
     * Adds the provided card to the discard pile.
     *
     * @param card card to discard; must not be {@code null}
     */
    public void addCardToDiscardPile(final Card card) {
        discardPile.add(Objects.requireNonNull(card, "card must not be null"));
    }

    /**
     * Removes the card at the specified position from the timeline.
     *
     * @param position zero-based position
     * @return removed card
     */
    public Card removeCardFromTimeline(final int position) {
        if (position < 0 || position >= timeline.size()) {
            throw new IllegalArgumentException("position out of bounds: " + position);
        }
        return timeline.remove(position);
    }

    /**
     * Retrieves the currently active player.
     *
     * @return current player instance
     */
    public Player getCurrentPlayer() {
        if (players.isEmpty()) {
            throw new IllegalStateException("No players registered");
        }
        return players.get(currentPlayerIndex);
    }

    /**
     * Returns the score of the player at the provided index.
     *
     * @param playerIndex zero-based index
     * @return player's score
     */
    public int getPlayerScore(final int playerIndex) {
        if (playerIndex < 0 || playerIndex >= players.size()) {
            throw new IllegalArgumentException("playerIndex out of bounds: " + playerIndex);
        }
        return players.get(playerIndex).getScore();
    }

    /**
     * Returns all visible cards (timeline plus the active hand) in a single immutable list.
     *
     * @return list of cards relevant to the active player
     */
    public List<Card> getAllCards() {
        final List<Card> result = new ArrayList<>(timeline.size() + hand.size());
        result.addAll(timeline);
        result.addAll(hand);
        return List.copyOf(result);
    }

    /**
     * Adds the provided move to the history list.
     *
     * @param move move to record; must not be {@code null}
     */
    public void addMoveToHistory(final Move move) {
        moveHistory.add(Objects.requireNonNull(move, "move must not be null"));
    }

    /**
     * Removes the most recent move from history.
     *
     * @return removed move
     */
    public Move removeLastMoveFromHistory() {
        if (moveHistory.isEmpty()) {
            throw new IllegalStateException("No moves to remove");
        }
        return moveHistory.remove(moveHistory.size() - 1);
    }

    /**
     * Indicates whether the game has reached a terminal state.
     *
     * @return {@code true} when the game phase is {@link GamePhase#GAME_OVER} or {@link GamePhase#RESULTS}
     */
    public boolean isGameOver() {
        return currentPhase == GamePhase.GAME_OVER || currentPhase == GamePhase.RESULTS;
    }

    /**
     * Synchronises the public hand snapshot with the current player's actual hand.
     */
    public void refreshHandSnapshot() {
        updateHandSnapshot();
    }

    /**
     * Persists the current game state to the specified file using JSON encoding.
     *
     * @param filename destination file name; must not be {@code null}
     * @throws IOException in case of I/O issues
     */
    public void saveToFile(final String filename) throws IOException {
        Objects.requireNonNull(filename, "filename must not be null");
        final JSONObject root = new JSONObject();
        root.put("currentPhase", currentPhase.name());
        root.put("currentPlayerIndex", currentPlayerIndex);
        root.put("gameStartTime", gameStartTime);
        root.put("players", playersToJson());
        root.put("timeline", cardsToJsonArray(timeline));
        root.put("hand", cardsToJsonArray(hand));
        root.put("discardPile", cardsToJsonArray(discardPile));
        root.put("moveHistory", movesToJsonArray());
        Files.writeString(Path.of(filename), root.toString(2), StandardCharsets.UTF_8);
    }

    /**
     * Rehydrates a game state from a file written by {@link #saveToFile(String)}.
     *
     * @param filename source file name; must not be {@code null}
     * @return reconstructed game state
     * @throws IOException in case of I/O issues
     */
    public static GameState loadFromFile(final String filename) throws IOException {
        Objects.requireNonNull(filename, "filename must not be null");
        final String content = Files.readString(Path.of(filename), StandardCharsets.UTF_8);
        final JSONObject root = new JSONObject(content);
        final GamePhase phase = GamePhase.valueOf(root.getString("currentPhase"));
        final int currentPlayerIndex = root.getInt("currentPlayerIndex");
        final long gameStartTime = root.getLong("gameStartTime");
        final List<Player> players = playersFromJson(root.getJSONArray("players"));
        final List<Card> timeline = cardsFromJsonArray(root.getJSONArray("timeline"));
        final List<Card> hand = cardsFromJsonArray(root.getJSONArray("hand"));
        final List<Card> discardPile = root.has("discardPile")
                ? cardsFromJsonArray(root.getJSONArray("discardPile"))
                : List.of();
        final List<Move> moveHistory = movesFromJsonArray(root.getJSONArray("moveHistory"));
        return new GameState(phase, currentPlayerIndex, players, timeline, hand, discardPile, moveHistory, gameStartTime);
    }

    private JSONArray playersToJson() {
        final JSONArray array = new JSONArray();
        for (final Player player : players) {
            final JSONObject obj = new JSONObject();
            obj.put("id", player.getId());
            obj.put("name", player.getName());
            obj.put("score", player.getScore());
            obj.put("hand", cardsToJsonArray(player.getHand()));
            array.put(obj);
        }
        return array;
    }

    private static List<Player> playersFromJson(final JSONArray array) {
        final List<Player> result = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            final JSONObject obj = array.getJSONObject(i);
            final List<Card> hand = cardsFromJsonArray(obj.getJSONArray("hand"));
            result.add(new Player(obj.getString("id"), obj.getString("name"), obj.getInt("score"), hand));
        }
        return result;
    }

    private JSONArray cardsToJsonArray(final List<Card> cards) {
        final JSONArray array = new JSONArray();
        for (final Card card : cards) {
            array.put(cardToJson(card));
        }
        return array;
    }

    private static List<Card> cardsFromJsonArray(final JSONArray array) {
        final List<Card> cards = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            cards.add(cardFromJson(array.getJSONObject(i)));
        }
        return cards;
    }

    private JSONArray movesToJsonArray() {
        final JSONArray array = new JSONArray();
        for (final Move move : moveHistory) {
            final JSONObject obj = new JSONObject();
            obj.put("card", cardToJson(move.getCard()));
            obj.put("position", move.getPositionInTimeline());
            obj.put("correct", move.isCorrect());
            obj.put("playerIndex", move.getPlayerIndex());
            obj.put("timestamp", move.getTimestamp());
            array.put(obj);
        }
        return array;
    }

    private static List<Move> movesFromJsonArray(final JSONArray array) {
        final List<Move> moves = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            final JSONObject obj = array.getJSONObject(i);
            final Card card = cardFromJson(obj.getJSONObject("card"));
            moves.add(new Move(card, obj.getInt("position"), obj.getBoolean("correct"), obj.getInt("playerIndex"), obj.getLong("timestamp")));
        }
        return moves;
    }

    private static JSONObject cardToJson(final Card card) {
        final JSONObject obj = new JSONObject();
        obj.put("id", card.getId());
        obj.put("title", card.getTitle());
        obj.put("date", card.getDate().toString());
        obj.put("trivia", card.getTrivia());
        obj.put("image", card.getImageAssetPath());
        obj.put("version", card.getMinecraftVersion());
        return obj;
    }

    private static Card cardFromJson(final JSONObject obj) {
        return new Card(
                obj.getString("id"),
                obj.getString("title"),
                LocalDate.parse(obj.getString("date")),
                obj.getString("trivia"),
                obj.getString("image"),
                obj.getString("version"));
    }

    private void updateHandSnapshot() {
        hand.clear();
        if (!players.isEmpty()) {
            final Player current = players.get(Math.min(Math.max(currentPlayerIndex, 0), players.size() - 1));
            hand.addAll(current.getHand());
        }
    }
}
