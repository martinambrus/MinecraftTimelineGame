package com.minecrafttimeline.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.minecrafttimeline.MinecraftTimelineGame;
import com.minecrafttimeline.components.Button;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Main menu screen hosting navigation buttons for the primary game modes.
 *
 * <pre>
 * +-----------------------------------------------------------+
 * |                       Minecraft Timeline                  |
 * |              "Test your Minecraft history knowledge!"     |
 * |                                                           |
 * |                  [ New Game (Single Player) ]             |
 * |                  [ New Game (Multiplayer) ]               |
 * |                  [ Load Game ]                            |
 * |                  [ Settings ]                             |
 * |                  [ Quit ]                                 |
 * +-----------------------------------------------------------+
 * </pre>
 */
public final class MainMenuScreen extends AbstractScreen {

    private static final float BUTTON_WIDTH = 520f;
    private static final float BUTTON_HEIGHT = 68f;
    private static final float BUTTON_SPACING = 20f;
    private static final float TITLE_Y_RATIO = 0.7f;
    private static final Color BACKGROUND_TOP = new Color(0.07f, 0.09f, 0.16f, 1f);
    private static final Color BACKGROUND_BOTTOM = new Color(0.02f, 0.02f, 0.05f, 1f);
    private static final Color PANEL_COLOR = new Color(0f, 0f, 0f, 0.75f);

    private final ScreenManager screenManager;
    private final List<Button> menuButtons = new ArrayList<>();
    private final List<Button> loadButtons = new ArrayList<>();
    private final List<String> savedGames = new ArrayList<>();

    private BitmapFont titleFont;
    private BitmapFont subtitleFont;
    private BitmapFont buttonFont;
    private Texture pixelTexture;
    private boolean showLoadDialog;
    private boolean pointerDown;
    private final GlyphLayout layout = new GlyphLayout();

    /**
     * Creates a new main menu screen.
     *
     * @param game          owning game; must not be {@code null}
     * @param screenManager screen manager used for navigation; must not be {@code null}
     */
    public MainMenuScreen(final MinecraftTimelineGame game, final ScreenManager screenManager) {
        super(game);
        this.screenManager = Objects.requireNonNull(screenManager, "screenManager must not be null");
        backgroundColor.set(0f, 0f, 0f, 1f);
    }

    @Override
    protected void buildUI() {
        titleFont = new BitmapFont();
        titleFont.getData().setScale(2.8f);
        subtitleFont = new BitmapFont();
        subtitleFont.getData().setScale(1.4f);
        buttonFont = new BitmapFont();
        buttonFont.getData().setScale(1.1f);

        createMenuButtons();
        refreshSavedGames();
    }

    private void createMenuButtons() {
        menuButtons.clear();
        final float centerX = viewport.getWorldWidth() / 2f;
        final float startY = viewport.getWorldHeight() / 2f + (BUTTON_HEIGHT * 2);
        final String[] labels = new String[] {
                "New Game (Single Player)",
                "New Game (Multiplayer)",
                "Load Game",
                "Settings",
                "Quit"
        };
        for (int i = 0; i < labels.length; i++) {
            final float buttonX = centerX - (BUTTON_WIDTH / 2f);
            final float buttonY = startY - (i * (BUTTON_HEIGHT + BUTTON_SPACING));
            final Button button = new Button(buttonX, buttonY, BUTTON_WIDTH, BUTTON_HEIGHT, labels[i]);
            switch (labels[i]) {
                case "New Game (Single Player)" -> button.setOnClick(screenManager::startSinglePlayerGame);
                case "New Game (Multiplayer)" -> button.setOnClick(() -> setScreen(LobbyScreen.class));
                case "Load Game" -> button.setOnClick(() -> {
                    showLoadDialog = true;
                    refreshSavedGames();
                });
                case "Settings" -> button.setOnClick(() -> setScreen(SettingsScreen.class));
                case "Quit" -> button.setOnClick(() -> Gdx.app.exit());
                default -> {
                }
            }
            menuButtons.add(button);
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

    private void refreshSavedGames() {
        savedGames.clear();
        final FileHandle saveRoot = Gdx.files.local("saves");
        if (!saveRoot.exists()) {
            saveRoot.mkdirs();
        }
        for (final FileHandle file : saveRoot.list()) {
            if (!file.isDirectory() && "json".equalsIgnoreCase(file.extension())) {
                savedGames.add(file.path());
            }
        }
        buildLoadButtons();
    }

    private void buildLoadButtons() {
        loadButtons.clear();
        float y = viewport.getWorldHeight() / 2f + 80f;
        for (final String save : savedGames) {
            final Button button = new Button(viewport.getWorldWidth() / 2f - 220f, y, 440f, 54f, save);
            button.setOnClick(() -> {
                showLoadDialog = false;
                screenManager.loadGame(save);
            });
            loadButtons.add(button);
            y -= 64f;
        }
        final Button backButton = new Button(viewport.getWorldWidth() / 2f - 150f, y - 40f, 300f, 48f, "Back");
        backButton.setOnClick(() -> showLoadDialog = false);
        loadButtons.add(backButton);
    }

    @Override
    protected void handleInput() {
        final boolean touched = Gdx.input.isTouched();
        final float pointerX = getWorldCursorPosition().x;
        final float pointerY = getWorldCursorPosition().y;
        final List<Button> targetButtons = showLoadDialog ? loadButtons : menuButtons;
        for (final Button button : targetButtons) {
            button.onMouseMoved(pointerX, pointerY);
        }
        if (touched && !pointerDown) {
            pointerDown = true;
            for (final Button button : targetButtons) {
                button.onMouseDown(pointerX, pointerY);
            }
        } else if (!touched && pointerDown) {
            pointerDown = false;
            for (final Button button : targetButtons) {
                button.onMouseUp();
            }
        }
    }

    @Override
    protected void updateLogic(final float delta) {
        // No continuous logic required for menu.
    }

    @Override
    protected void renderScreen(final float delta) {
        final Texture pixel = obtainPixel();
        final float width = viewport.getWorldWidth();
        final float height = viewport.getWorldHeight();

        batch.setColor(BACKGROUND_TOP);
        batch.draw(pixel, 0f, height / 2f, width, height / 2f);
        batch.setColor(BACKGROUND_BOTTOM);
        batch.draw(pixel, 0f, 0f, width, height / 2f);
        batch.setColor(Color.WHITE);

        final float titleY = height * TITLE_Y_RATIO;
        layout.setText(titleFont, "Minecraft Timeline");
        titleFont.setColor(Color.WHITE);
        titleFont.draw(batch, layout, (width - layout.width) / 2f, titleY);

        layout.setText(subtitleFont, "Test your Minecraft history knowledge!");
        subtitleFont.setColor(Color.LIGHT_GRAY);
        subtitleFont.draw(batch, layout, (width - layout.width) / 2f, titleY - 70f);

        for (final Button button : menuButtons) {
            button.render(batch, buttonFont);
        }

        if (showLoadDialog) {
            renderLoadDialog(pixel, width, height);
        }
    }

    @Override
    public void resize(final int width, final int height) {
        super.resize(width, height);
        createMenuButtons();
        buildLoadButtons();
    }

    private void renderLoadDialog(final Texture pixel, final float width, final float height) {
        final float panelWidth = 560f;
        final float panelHeight = 360f;
        final float panelX = (width - panelWidth) / 2f;
        final float panelY = (height - panelHeight) / 2f;

        batch.setColor(new Color(0f, 0f, 0f, 0.6f));
        batch.draw(pixel, 0f, 0f, width, height);
        batch.setColor(PANEL_COLOR);
        batch.draw(pixel, panelX, panelY, panelWidth, panelHeight);
        batch.setColor(Color.WHITE);

        final String title = "Load Saved Game";
        layout.setText(buttonFont, title);
        buttonFont.setColor(Color.WHITE);
        buttonFont.draw(batch, layout, panelX + (panelWidth - layout.width) / 2f, panelY + panelHeight - 40f);

        if (savedGames.isEmpty()) {
            layout.setText(buttonFont, "No saves found");
            buttonFont.draw(batch, layout, panelX + (panelWidth - layout.width) / 2f, panelY + panelHeight / 2f);
        }

        for (final Button button : loadButtons) {
            button.render(batch, buttonFont);
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        if (titleFont != null) {
            titleFont.dispose();
        }
        if (subtitleFont != null) {
            subtitleFont.dispose();
        }
        if (buttonFont != null) {
            buttonFont.dispose();
        }
        if (pixelTexture != null) {
            pixelTexture.dispose();
        }
    }
}
