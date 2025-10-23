package com.minecrafttimeline.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.minecrafttimeline.components.Button;
import com.minecrafttimeline.components.TextInput;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Lobby screen simulating a multiplayer staging area.
 *
 * <pre>
 * +-----------------------------------------------------------+
 * | Game Lobby                                                |
 * |                                                           |
 * | Players:            Settings:                             |
 * |  - Player 1 (You)   Players: [ 2 ]                        |
 * |  - Player 2         Cards:   [ 4 ]                        |
 * |                                                           |
 * | Chat:                                                   | |
 * |  [messages...]                                          | |
 * |  [ text input..................... ] [ Start Game ]       |
 * |                                                           |
 * | [ Back to Menu ]                                         |
 * +-----------------------------------------------------------+
 * </pre>
 */
public final class LobbyScreen extends AbstractScreen {

    private static final float PANEL_WIDTH = 900f;
    private static final float PANEL_HEIGHT = 540f;
    private static final Color PANEL_COLOR = new Color(0f, 0f, 0f, 0.7f);

    private final ScreenManager screenManager;
    private final List<Button> buttons = new ArrayList<>();
    private final List<String> chatMessages = new ArrayList<>();

    private BitmapFont font;
    private Texture pixelTexture;
    private GlyphLayout layout = new GlyphLayout();
    private Button playerCountButton;
    private Button cardsButton;
    private Button startButton;
    private Button backButton;
    private TextInput chatInput;
    private LobbyInputProcessor inputProcessor;
    private int selectedPlayerCount = 2;
    private int cardsPerPlayer = 4;

    /**
     * Creates a new lobby screen.
     *
     * @param game          owning game; must not be {@code null}
     * @param screenManager screen manager; must not be {@code null}
     */
    public LobbyScreen(final MinecraftTimelineGame game, final ScreenManager screenManager) {
        super(game);
        this.screenManager = Objects.requireNonNull(screenManager, "screenManager must not be null");
        backgroundColor.set(0.05f, 0.05f, 0.08f, 1f);
    }

    @Override
    protected void buildUI() {
        font = new BitmapFont();
        font.getData().setScale(1.2f);

        final float centerX = viewport.getWorldWidth() / 2f;
        final float panelX = centerX - (PANEL_WIDTH / 2f);
        final float panelY = (viewport.getWorldHeight() - PANEL_HEIGHT) / 2f;

        playerCountButton = new Button(panelX + 480f, panelY + PANEL_HEIGHT - 180f, 180f, 54f, "Players: 2");
        playerCountButton.setOnClick(this::cyclePlayerCount);
        cardsButton = new Button(panelX + 480f, panelY + PANEL_HEIGHT - 250f, 180f, 54f, "Cards: 4");
        cardsButton.setOnClick(this::cycleCardsPerPlayer);
        startButton = new Button(panelX + PANEL_WIDTH - 220f, panelY + 40f, 180f, 54f, "Start Game");
        startButton.setOnClick(this::startGame);
        backButton = new Button(panelX + 40f, panelY + 40f, 180f, 54f, "Back to Menu");
        backButton.setOnClick(() -> setScreen(MainMenuScreen.class));

        chatInput = new TextInput(panelX + 40f, panelY + 120f, PANEL_WIDTH - 300f, 48f, "Type a message...", 120);

        buttons.clear();
        buttons.add(playerCountButton);
        buttons.add(cardsButton);
        buttons.add(startButton);
        buttons.add(backButton);

        inputProcessor = new LobbyInputProcessor();
        Gdx.input.setInputProcessor(inputProcessor);
    }

    private void cyclePlayerCount() {
        selectedPlayerCount++;
        if (selectedPlayerCount > 4) {
            selectedPlayerCount = 2;
        }
        playerCountButton.setText("Players: " + selectedPlayerCount);
    }

    private void cycleCardsPerPlayer() {
        cardsPerPlayer++;
        if (cardsPerPlayer > 5) {
            cardsPerPlayer = 3;
        }
        cardsButton.setText("Cards: " + cardsPerPlayer);
    }

    private void startGame() {
        final List<String> playerNames = new ArrayList<>();
        playerNames.add("Player 1 (You)");
        for (int i = 2; i <= selectedPlayerCount; i++) {
            playerNames.add("Player " + i);
        }
        screenManager.startMultiplayerGame(playerNames, cardsPerPlayer);
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
        if (touched) {
            for (final Button button : buttons) {
                button.onMouseDown(pointerX, pointerY);
            }
            if (chatInput.contains(pointerX, pointerY)) {
                chatInput.focus();
            } else {
                chatInput.unfocus();
            }
        } else {
            for (final Button button : buttons) {
                button.onMouseUp();
            }
        }
    }

    @Override
    protected void updateLogic(final float delta) {
        // No continuous animations.
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

        layout.setText(font, "Game Lobby");
        font.draw(batch, layout, panelX + (PANEL_WIDTH - layout.width) / 2f, panelY + PANEL_HEIGHT - 40f);

        renderPlayers(panelX + 40f, panelY + PANEL_HEIGHT - 120f);
        renderChat(panelX + 40f, panelY + 200f);

        for (final Button button : buttons) {
            button.render(batch, font);
        }
        chatInput.render(batch, font);
    }

    private void renderPlayers(final float x, final float startY) {
        float y = startY;
        font.draw(batch, "Players:", x, y);
        y -= 30f;
        font.draw(batch, "Player 1 (You)", x + 20f, y);
        for (int i = 2; i <= selectedPlayerCount; i++) {
            y -= 24f;
            font.draw(batch, "Player " + i, x + 20f, y);
        }
    }

    private void renderChat(final float x, final float y) {
        font.draw(batch, "Chat:", x, y);
        float messageY = y - 30f;
        for (int i = Math.max(0, chatMessages.size() - 6); i < chatMessages.size(); i++) {
            font.draw(batch, chatMessages.get(i), x + 20f, messageY);
            messageY -= 24f;
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        if (font != null) {
            font.dispose();
        }
        if (pixelTexture != null) {
            pixelTexture.dispose();
        }
        if (Gdx.input.getInputProcessor() == inputProcessor) {
            Gdx.input.setInputProcessor(null);
        }
    }

    @Override
    public void hide() {
        super.hide();
        if (Gdx.input.getInputProcessor() == inputProcessor) {
            Gdx.input.setInputProcessor(null);
        }
    }

    private final class LobbyInputProcessor extends InputAdapter {

        @Override
        public boolean keyTyped(final char character) {
            chatInput.onKeyPressed(character);
            return true;
        }

        @Override
        public boolean keyDown(final int keycode) {
            if (keycode == Input.Keys.BACKSPACE) {
                chatInput.onBackspace();
                return true;
            }
            if (keycode == Input.Keys.ENTER) {
                sendChatMessage();
                return true;
            }
            return false;
        }
    }

    private void sendChatMessage() {
        final String text = chatInput.getText().trim();
        if (!text.isEmpty()) {
            chatMessages.add("You: " + text);
            chatInput.setText("");
        }
    }
}
