package com.minecrafttimeline.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.files.FileHandle;
import com.minecrafttimeline.MinecraftTimelineGame;
import com.minecrafttimeline.core.card.Card;
import com.minecrafttimeline.core.card.CardDeck;
import com.minecrafttimeline.core.card.CardManager;
import com.minecrafttimeline.core.config.GameSettings;
import com.minecrafttimeline.core.game.GameSession;
import com.minecrafttimeline.models.Player;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Coordinates creation and reuse of screens while exposing helper methods for navigation.
 */
public final class ScreenManager {

    private static final int DEFAULT_SINGLE_PLAYER_COUNT = 1;
    private static final int DEFAULT_SINGLE_PLAYER_CARDS = 4;

    private final MinecraftTimelineGame game;
    private MainMenuScreen mainMenuScreen;
    private SettingsScreen settingsScreen;
    private LobbyScreen lobbyScreen;
    private GameSettings settings;
    private GameSession currentSession;

    /**
     * Creates a new screen manager bound to the supplied game.
     *
     * @param game owning game; must not be {@code null}
     */
    public ScreenManager(final MinecraftTimelineGame game) {
        this.game = Objects.requireNonNull(game, "game must not be null");
    }

    /**
     * Performs initial asset and configuration loading.
     */
    public void initialize() {
        final CardManager cardManager = CardManager.getInstance();
        if (!cardManager.isInitialized()) {
            if (!loadTriviaDatabase(cardManager)) {
                throw new IllegalStateException("Unable to load trivia database from available resources.");
            }
        }
        settings = GameSettings.load();
    }

    private boolean loadTriviaDatabase(final CardManager cardManager) {
        if (Gdx.files == null) {
            logTriviaLoadFailure("Gdx.files is not initialized; cannot load trivia database.", null);
            return false;
        }

        final FileHandle primaryHandle = Gdx.files.internal("data/trivia.json");
        if (tryLoadTrivia(cardManager, primaryHandle, "data/trivia.json (internal)")) {
            return true;
        }

        final FileHandle sampleHandle = Gdx.files.classpath("data/sample-trivia.json");
        return tryLoadTrivia(cardManager, sampleHandle, "data/sample-trivia.json (classpath)");
    }

    private boolean tryLoadTrivia(final CardManager cardManager, final FileHandle handle,
                                  final String sourceDescription) {
        if (handle == null) {
            return false;
        }
        if (!handle.exists()) {
            logTriviaLoadFailure("Trivia database not found: " + sourceDescription, null);
            return false;
        }
        try {
            cardManager.initialize(handle);
            return true;
        } catch (IllegalArgumentException exception) {
            logTriviaLoadFailure("Failed to load trivia database: " + sourceDescription, exception);
            return false;
        }
    }

    private void logTriviaLoadFailure(final String message, final Throwable exception) {
        if (Gdx.app != null) {
            if (exception != null) {
                Gdx.app.error("ScreenManager", message, exception);
            } else {
                Gdx.app.error("ScreenManager", message);
            }
        } else {
            if (exception != null) {
                exception.printStackTrace();
            } else {
                System.err.println("ScreenManager: " + message);
            }
        }
    }

    /**
     * Releases resources held by cached screens.
     */
    public void dispose() {
        if (mainMenuScreen != null) {
            mainMenuScreen.dispose();
        }
        if (settingsScreen != null) {
            settingsScreen.dispose();
        }
        if (lobbyScreen != null) {
            lobbyScreen.dispose();
        }
    }

    /**
     * Displays the main menu screen.
     */
    public void showMainMenu() {
        setScreen(getMainMenuScreen(), false);
    }

    /**
     * Retrieves the shared settings instance.
     *
     * @return settings reference
     */
    public GameSettings getSettings() {
        return settings;
    }

    /**
     * Persists the current settings state to disk.
     */
    public void saveSettings() {
        settings.save();
    }

    /**
     * Starts a single-player game using the current card database.
     */
    public void startSinglePlayerGame() {
        final List<String> names = List.of("Player 1");
        final GameSession session = createSession(names, DEFAULT_SINGLE_PLAYER_CARDS);
        showGameplay(session);
    }

    /**
     * Starts a multiplayer game with the provided player names and cards per player.
     *
     * @param playerNames    participating player names; must not be {@code null}
     * @param cardsPerPlayer cards to deal to each player
     */
    public void startMultiplayerGame(final List<String> playerNames, final int cardsPerPlayer) {
        Objects.requireNonNull(playerNames, "playerNames must not be null");
        final GameSession session = createSession(playerNames, cardsPerPlayer);
        showGameplay(session);
    }

    /**
     * Loads a saved session from disk and displays the gameplay screen.
     *
     * @param filename save file name; must not be {@code null}
     */
    public void loadGame(final String filename) {
        try {
            final GameSession session = GameSession.loadGame(filename);
            showGameplay(session);
        } catch (final Exception exception) {
            Gdx.app.error("ScreenManager", "Failed to load game: " + filename, exception);
        }
    }

    /**
     * Displays the results screen for the concluded game session.
     *
     * @param session finished session; must not be {@code null}
     */
    public void showResults(final GameSession session) {
        currentSession = session;
        setScreen(new ResultsScreen(game, session, this), true);
    }

    /**
     * Retrieves (or lazily creates) the main menu instance.
     *
     * @return main menu screen
     */
    public MainMenuScreen getMainMenuScreen() {
        if (mainMenuScreen == null) {
            mainMenuScreen = new MainMenuScreen(game, this);
        }
        return mainMenuScreen;
    }

    /**
     * Retrieves (or lazily creates) the settings screen instance.
     *
     * @return settings screen
     */
    public SettingsScreen getSettingsScreen() {
        if (settingsScreen == null) {
            settingsScreen = new SettingsScreen(game, this);
        }
        return settingsScreen;
    }

    /**
     * Retrieves (or lazily creates) the lobby screen instance.
     *
     * @return lobby screen
     */
    public LobbyScreen getLobbyScreen() {
        if (lobbyScreen == null) {
            lobbyScreen = new LobbyScreen(game, this);
        }
        return lobbyScreen;
    }

    /**
     * Switches to a screen identified by its class.
     *
     * @param screenClass target screen class; must not be {@code null}
     */
    public void switchTo(final Class<? extends Screen> screenClass) {
        Objects.requireNonNull(screenClass, "screenClass must not be null");
        if (screenClass == MainMenuScreen.class) {
            showMainMenu();
        } else if (screenClass == SettingsScreen.class) {
            setScreen(getSettingsScreen(), false);
        } else if (screenClass == LobbyScreen.class) {
            setScreen(getLobbyScreen(), false);
        } else {
            throw new IllegalArgumentException("Unsupported screen class: " + screenClass.getName());
        }
    }

    private void showGameplay(final GameSession session) {
        currentSession = session;
        setScreen(new GameplayScreen(game, session, this), true);
    }

    private void setScreen(final Screen newScreen, final boolean disposePrevious) {
        final Screen previousScreen = game.getScreen();
        game.setScreen(newScreen);
        if (disposePrevious && previousScreen != null && previousScreen != newScreen) {
            previousScreen.dispose();
        }
    }

    private GameSession createSession(final List<String> playerNames, final int cardsPerPlayer) {
        final CardManager cardManager = CardManager.getInstance();
        final List<Card> cards = new ArrayList<>(cardManager.getAllCards());
        final CardDeck deck = new CardDeck(cards);
        deck.shuffle();

        final List<Player> players = new ArrayList<>();
        for (final String name : playerNames) {
            final String displayName = name == null || name.isBlank() ? "Player" : name;
            players.add(new Player(UUID.randomUUID().toString(), displayName.trim()));
        }
        if (players.isEmpty()) {
            while (players.size() < DEFAULT_SINGLE_PLAYER_COUNT) {
                players.add(new Player(UUID.randomUUID().toString(), "Player " + (players.size() + 1)));
            }
        }
        final GameSession session = new GameSession();
        session.setCardsPerPlayer(cardsPerPlayer);
        session.startNewGame(players, deck);
        currentSession = session;
        return session;
    }

    /**
     * Returns the currently active session if one exists.
     *
     * @return current session or {@code null}
     */
    public GameSession getCurrentSession() {
        return currentSession;
    }
}
