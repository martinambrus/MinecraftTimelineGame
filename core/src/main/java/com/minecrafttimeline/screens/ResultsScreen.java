package com.minecrafttimeline.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.minecrafttimeline.MinecraftTimelineGame;
import com.minecrafttimeline.components.Button;
import com.minecrafttimeline.core.game.GameSession;
import com.minecrafttimeline.core.game.GameState;
import com.minecrafttimeline.core.game.Move;
import com.minecrafttimeline.models.Player;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Results screen displayed when a session concludes.
 *
 * <pre>
 * +-----------------------------------------------------------+
 * |                        Game Over!                         |
 * |                Player X wins with score Y!                |
 * |                                                           |
 * |  Player         Score                                    |
 * |  ---------------------------------------------           |
 * |  Player 1       12                                       |
 * |  Player 2       10                                       |
 * |                                                           |
 * |  Accuracy: 85%      Time: 12.4 min    Cards: 25           |
 * |                                                           |
 * |              [ Play Again ]   [ Main Menu ]               |
 * +-----------------------------------------------------------+
 * </pre>
 */
public final class ResultsScreen extends AbstractScreen {

    private static final float PANEL_WIDTH = 780f;
    private static final float PANEL_HEIGHT = 520f;
    private static final Color PANEL_COLOR = new Color(0f, 0f, 0f, 0.8f);

    private final GameSession gameSession;
    private final ScreenManager screenManager;
    private final List<Button> buttons = new ArrayList<>();

    private BitmapFont titleFont;
    private BitmapFont infoFont;
    private Texture pixelTexture;
    private final GlyphLayout layout = new GlyphLayout();
    private boolean pointerDown;

    /**
     * Creates a new results screen for the specified session.
     *
     * @param game         owning game instance; must not be {@code null}
     * @param gameSession  finished session; must not be {@code null}
     * @param screenManager manager used to start new games; must not be {@code null}
     */
    public ResultsScreen(
            final MinecraftTimelineGame game,
            final GameSession gameSession,
            final ScreenManager screenManager) {
        super(game);
        this.gameSession = Objects.requireNonNull(gameSession, "gameSession must not be null");
        this.screenManager = Objects.requireNonNull(screenManager, "screenManager must not be null");
        backgroundColor.set(0.02f, 0.02f, 0.02f, 1f);
    }

    @Override
    protected void buildUI() {
        titleFont = new BitmapFont();
        titleFont.getData().setScale(2.2f);
        infoFont = new BitmapFont();
        infoFont.getData().setScale(1.2f);

        final float centerX = viewport.getWorldWidth() / 2f;
        final float buttonY = viewport.getWorldHeight() / 2f - 160f;

        final Button playAgain = new Button(centerX - 220f, buttonY, 200f, 60f, "Play Again");
        playAgain.setOnClick(this::restartGame);
        final Button mainMenu = new Button(centerX + 20f, buttonY, 200f, 60f, "Main Menu");
        mainMenu.setOnClick(() -> setScreen(MainMenuScreen.class));
        buttons.add(playAgain);
        buttons.add(mainMenu);
    }

    private void restartGame() {
        final List<String> playerNames = new ArrayList<>();
        for (final Player player : gameSession.getGameState().getPlayers()) {
            playerNames.add(player.getName());
        }
        if (playerNames.size() <= 1) {
            screenManager.startSinglePlayerGame();
        } else {
            screenManager.startMultiplayerGame(playerNames, 4);
        }
    }

    private Texture obtainPixel() {
        if (pixelTexture == null) {
            final Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
            pixmap.setColor(Color.WHITE);
            pixmap.fill();
            pixelTexture = new Texture(pixmap);
            pixmap.dispose();
        }
        return pixelTexture;
    }

    @Override
    protected void handleInput() {
        final boolean touched = Gdx.input.isTouched();
        final float pointerX = getWorldCursorPosition().x;
        final float pointerY = getWorldCursorPosition().y;
        for (final Button button : buttons) {
            button.onMouseMoved(pointerX, pointerY);
        }
        if (touched && !pointerDown) {
            pointerDown = true;
            for (final Button button : buttons) {
                button.onMouseDown(pointerX, pointerY);
            }
        } else if (!touched && pointerDown) {
            pointerDown = false;
            for (final Button button : buttons) {
                button.onMouseUp();
            }
        }
    }

    @Override
    protected void updateLogic(final float delta) {
        // No per-frame updates required.
    }

    @Override
    protected void renderScreen(final float delta) {
        final Texture pixel = obtainPixel();
        final float width = viewport.getWorldWidth();
        final float height = viewport.getWorldHeight();
        final float panelX = (width - PANEL_WIDTH) / 2f;
        final float panelY = (height - PANEL_HEIGHT) / 2f;

        batch.setColor(new Color(0f, 0f, 0f, 0.6f));
        batch.draw(pixel, 0f, 0f, width, height);
        batch.setColor(PANEL_COLOR);
        batch.draw(pixel, panelX, panelY, PANEL_WIDTH, PANEL_HEIGHT);
        batch.setColor(Color.WHITE);

        layout.setText(titleFont, "Game Over!");
        titleFont.draw(batch, layout, (width - layout.width) / 2f, panelY + PANEL_HEIGHT - 40f);

        final Player winner = gameSession.getWinner();
        final String winnerText = winner == null
                ? "It's a tie!"
                : winner.getName() + " wins with score " + winner.getScore() + "!";
        layout.setText(infoFont, winnerText);
        infoFont.draw(batch, layout, (width - layout.width) / 2f, panelY + PANEL_HEIGHT - 120f);

        renderScoreTable(panelX, panelY + PANEL_HEIGHT - 200f);
        renderStats(panelX + 40f, panelY + 120f);

        for (final Button button : buttons) {
            button.render(batch, infoFont);
        }
    }

    private void renderScoreTable(final float startX, final float startY) {
        final List<Player> players = new ArrayList<>(gameSession.getGameState().getPlayers());
        players.sort(Comparator.comparingInt(Player::getScore).reversed());
        float y = startY;
        infoFont.draw(batch, "Player", startX + 40f, y);
        infoFont.draw(batch, "Score", startX + 360f, y);
        y -= 26f;
        for (final Player player : players) {
            infoFont.draw(batch, player.getName(), startX + 40f, y);
            infoFont.draw(batch, String.valueOf(player.getScore()), startX + 360f, y);
            y -= 26f;
        }
    }

    private void renderStats(final float x, final float y) {
        final GameState state = gameSession.getGameState();
        final List<Move> moves = state.getMoveHistory();
        int correctMoves = 0;
        for (final Move move : moves) {
            if (move.isCorrect()) {
                correctMoves++;
            }
        }
        final int totalMoves = moves.size();
        final float accuracy = totalMoves == 0 ? 0f : (correctMoves * 100f) / totalMoves;
        final long elapsedMillis = System.currentTimeMillis() - state.getGameStartTime();
        final float elapsedMinutes = elapsedMillis / 60000f;
        final int cardsPlaced = state.getTimeline().size();

        infoFont.draw(batch, String.format("Accuracy: %.0f%%", accuracy), x, y);
        infoFont.draw(batch, String.format("Time taken: %.1f min", elapsedMinutes), x + 220f, y);
        infoFont.draw(batch, "Cards placed: " + cardsPlaced, x + 460f, y);
    }

    @Override
    public void dispose() {
        super.dispose();
        if (titleFont != null) {
            titleFont.dispose();
        }
        if (infoFont != null) {
            infoFont.dispose();
        }
        if (pixelTexture != null) {
            pixelTexture.dispose();
        }
    }
}
