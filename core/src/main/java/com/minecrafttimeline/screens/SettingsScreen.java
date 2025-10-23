package com.minecrafttimeline.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.minecrafttimeline.components.Button;
import com.minecrafttimeline.components.Slider;
import com.minecrafttimeline.core.config.GameSettings;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Settings screen allowing players to tweak graphics, audio, gameplay and debug options.
 *
 * <pre>
 * +-----------------------------------------------------------+
 * | Settings                                                   |
 * |  Graphics:   Resolution [1920x1080]   Fullscreen [On]      |
 * |              VSync [On]                                   |
 * |  Audio:      Master [slider] Music [slider] SFX [slider]   |
 * |              Mute [Off]                                    |
 * |  Gameplay:   Difficulty [Normal] Animation [slider]        |
 * |  Debug:      FPS [On]  Debug Render [Off]                  |
 * |                                                           |
 * | [ Save & Exit ]   [ Reset to Defaults ]                    |
 * +-----------------------------------------------------------+
 * </pre>
 */
public final class SettingsScreen extends AbstractScreen {

    private static final float PANEL_WIDTH = 900f;
    private static final float PANEL_HEIGHT = 560f;
    private static final Color PANEL_COLOR = new Color(0f, 0f, 0f, 0.78f);
    private static final int[][] RESOLUTIONS = new int[][] {
            {1280, 720},
            {1600, 900},
            {1920, 1080}
    };

    private final ScreenManager screenManager;
    private final GameSettings settings;

    private final List<Button> buttons = new ArrayList<>();
    private BitmapFont font;
    private GlyphLayout layout = new GlyphLayout();
    private Texture pixelTexture;

    private Button resolutionButton;
    private Button fullscreenButton;
    private Button vsyncButton;
    private Button muteButton;
    private Button difficultyButton;
    private Button fpsButton;
    private Button debugButton;
    private Button saveButton;
    private Button resetButton;
    private Slider masterSlider;
    private Slider musicSlider;
    private Slider sfxSlider;
    private Slider animationSlider;
    private Slider activeSlider;

    private boolean pointerDown;

    /**
     * Creates a new settings screen instance.
     *
     * @param game          owning game; must not be {@code null}
     * @param screenManager screen manager; must not be {@code null}
     */
    public SettingsScreen(final MinecraftTimelineGame game, final ScreenManager screenManager) {
        super(game);
        this.screenManager = Objects.requireNonNull(screenManager, "screenManager must not be null");
        this.settings = Objects.requireNonNull(screenManager.getSettings(), "settings must not be null");
        backgroundColor.set(0.03f, 0.03f, 0.05f, 1f);
    }

    @Override
    protected void buildUI() {
        font = new BitmapFont();
        font.getData().setScale(1.1f);

        final float panelX = (viewport.getWorldWidth() - PANEL_WIDTH) / 2f;
        final float panelY = (viewport.getWorldHeight() - PANEL_HEIGHT) / 2f;

        resolutionButton = new Button(panelX + 280f, panelY + PANEL_HEIGHT - 120f, 200f, 48f,
                "Resolution: " + settings.getResolutionWidth() + "x" + settings.getResolutionHeight());
        resolutionButton.setOnClick(this::cycleResolution);
        fullscreenButton = new Button(panelX + 500f, panelY + PANEL_HEIGHT - 120f, 160f, 48f, "Fullscreen: " + flagText(settings.isFullscreen()));
        fullscreenButton.setOnClick(() -> {
            settings.setFullscreen(!settings.isFullscreen());
            fullscreenButton.setText("Fullscreen: " + flagText(settings.isFullscreen()));
        });
        vsyncButton = new Button(panelX + 680f, panelY + PANEL_HEIGHT - 120f, 140f, 48f, "VSync: " + flagText(settings.isVsync()));
        vsyncButton.setOnClick(() -> {
            settings.setVsync(!settings.isVsync());
            vsyncButton.setText("VSync: " + flagText(settings.isVsync()));
        });

        masterSlider = new Slider(panelX + 260f, panelY + PANEL_HEIGHT - 190f, 400f, 36f, 0f, 100f, settings.getMasterVolume());
        masterSlider.setOnChange(value -> settings.setMasterVolume(Math.round(value)));
        musicSlider = new Slider(panelX + 260f, panelY + PANEL_HEIGHT - 240f, 400f, 36f, 0f, 100f, settings.getMusicVolume());
        musicSlider.setOnChange(value -> settings.setMusicVolume(Math.round(value)));
        sfxSlider = new Slider(panelX + 260f, panelY + PANEL_HEIGHT - 290f, 400f, 36f, 0f, 100f, settings.getSfxVolume());
        sfxSlider.setOnChange(value -> settings.setSfxVolume(Math.round(value)));

        muteButton = new Button(panelX + 680f, panelY + PANEL_HEIGHT - 260f, 160f, 48f, "Mute: " + flagText(settings.isMuteAll()));
        muteButton.setOnClick(() -> {
            settings.setMuteAll(!settings.isMuteAll());
            muteButton.setText("Mute: " + flagText(settings.isMuteAll()));
        });

        difficultyButton = new Button(panelX + 260f, panelY + PANEL_HEIGHT - 360f, 220f, 48f, "Difficulty: " + settings.getDifficulty());
        difficultyButton.setOnClick(this::cycleDifficulty);
        animationSlider = new Slider(panelX + 260f, panelY + PANEL_HEIGHT - 420f, 400f, 36f, 0.5f, 2.0f, settings.getAnimationSpeed());
        animationSlider.setOnChange(settings::setAnimationSpeed);

        fpsButton = new Button(panelX + 260f, panelY + PANEL_HEIGHT - 480f, 160f, 48f, "FPS: " + flagText(settings.isShowFps()));
        fpsButton.setOnClick(() -> {
            settings.setShowFps(!settings.isShowFps());
            fpsButton.setText("FPS: " + flagText(settings.isShowFps()));
        });
        debugButton = new Button(panelX + 440f, panelY + PANEL_HEIGHT - 480f, 220f, 48f, "Debug Render: " + flagText(settings.isDebugRender()));
        debugButton.setOnClick(() -> {
            settings.setDebugRender(!settings.isDebugRender());
            debugButton.setText("Debug Render: " + flagText(settings.isDebugRender()));
        });

        saveButton = new Button(panelX + PANEL_WIDTH - 260f, panelY + 40f, 200f, 56f, "Save & Exit");
        saveButton.setOnClick(this::saveAndExit);
        resetButton = new Button(panelX + 40f, panelY + 40f, 220f, 56f, "Reset to Defaults");
        resetButton.setOnClick(this::resetToDefaults);

        buttons.clear();
        buttons.add(resolutionButton);
        buttons.add(fullscreenButton);
        buttons.add(vsyncButton);
        buttons.add(muteButton);
        buttons.add(difficultyButton);
        buttons.add(fpsButton);
        buttons.add(debugButton);
        buttons.add(saveButton);
        buttons.add(resetButton);
    }

    private void cycleResolution() {
        int index = 0;
        for (int i = 0; i < RESOLUTIONS.length; i++) {
            if (RESOLUTIONS[i][0] == settings.getResolutionWidth() && RESOLUTIONS[i][1] == settings.getResolutionHeight()) {
                index = i;
                break;
            }
        }
        index = (index + 1) % RESOLUTIONS.length;
        settings.setResolution(RESOLUTIONS[index][0], RESOLUTIONS[index][1]);
        resolutionButton.setText("Resolution: " + RESOLUTIONS[index][0] + "x" + RESOLUTIONS[index][1]);
    }

    private void cycleDifficulty() {
        final String current = settings.getDifficulty();
        final String next = switch (current) {
            case "Easy" -> "Normal";
            case "Normal" -> "Hard";
            default -> "Easy";
        };
        settings.setDifficulty(next);
        difficultyButton.setText("Difficulty: " + next);
    }

    private void saveAndExit() {
        screenManager.saveSettings();
        setScreen(MainMenuScreen.class);
    }

    private void resetToDefaults() {
        settings.resetToDefaults();
        resolutionButton.setText("Resolution: " + settings.getResolutionWidth() + "x" + settings.getResolutionHeight());
        fullscreenButton.setText("Fullscreen: " + flagText(settings.isFullscreen()));
        vsyncButton.setText("VSync: " + flagText(settings.isVsync()));
        muteButton.setText("Mute: " + flagText(settings.isMuteAll()));
        difficultyButton.setText("Difficulty: " + settings.getDifficulty());
        fpsButton.setText("FPS: " + flagText(settings.isShowFps()));
        debugButton.setText("Debug Render: " + flagText(settings.isDebugRender()));
        masterSlider.setValue(settings.getMasterVolume());
        musicSlider.setValue(settings.getMusicVolume());
        sfxSlider.setValue(settings.getSfxVolume());
        animationSlider.setValue(settings.getAnimationSpeed());
    }

    private String flagText(final boolean value) {
        return value ? "On" : "Off";
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
            activeSlider = determineSlider(pointerX, pointerY);
            if (activeSlider != null) {
                activeSlider.onMouseDragged(pointerX);
            }
        } else if (touched && pointerDown) {
            if (activeSlider != null) {
                activeSlider.onMouseDragged(pointerX);
            }
        } else if (!touched && pointerDown) {
            pointerDown = false;
            for (final Button button : buttons) {
                button.onMouseUp();
            }
            activeSlider = null;
        }
    }

    private Slider determineSlider(final float pointerX, final float pointerY) {
        if (masterSlider.contains(pointerX, pointerY)) {
            return masterSlider;
        }
        if (musicSlider.contains(pointerX, pointerY)) {
            return musicSlider;
        }
        if (sfxSlider.contains(pointerX, pointerY)) {
            return sfxSlider;
        }
        if (animationSlider.contains(pointerX, pointerY)) {
            return animationSlider;
        }
        return null;
    }

    @Override
    protected void updateLogic(final float delta) {
        // No continuous updates required.
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

        layout.setText(font, "Settings");
        font.draw(batch, layout, panelX + (PANEL_WIDTH - layout.width) / 2f, panelY + PANEL_HEIGHT - 40f);

        drawSectionTitle("Graphics", panelX + 40f, panelY + PANEL_HEIGHT - 100f);
        layout.setText(font, "Resolution: " + settings.getResolutionWidth() + "x" + settings.getResolutionHeight());
        font.draw(batch, layout, panelX + 60f, panelY + PANEL_HEIGHT - 130f);

        drawSectionTitle("Audio", panelX + 40f, panelY + PANEL_HEIGHT - 200f);
        font.draw(batch, "Master Volume", panelX + 60f, panelY + PANEL_HEIGHT - 200f);
        font.draw(batch, "Music Volume", panelX + 60f, panelY + PANEL_HEIGHT - 250f);
        font.draw(batch, "SFX Volume", panelX + 60f, panelY + PANEL_HEIGHT - 300f);

        drawSectionTitle("Gameplay", panelX + 40f, panelY + PANEL_HEIGHT - 360f);
        font.draw(batch, "Animation Speed", panelX + 60f, panelY + PANEL_HEIGHT - 380f);

        drawSectionTitle("Debug", panelX + 40f, panelY + PANEL_HEIGHT - 460f);

        masterSlider.render(batch);
        musicSlider.render(batch);
        sfxSlider.render(batch);
        animationSlider.render(batch);

        for (final Button button : buttons) {
            button.render(batch, font);
        }
    }

    private void drawSectionTitle(final String title, final float x, final float y) {
        font.setColor(Color.SKY);
        font.draw(batch, title, x, y);
        font.setColor(Color.WHITE);
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
    }
}
