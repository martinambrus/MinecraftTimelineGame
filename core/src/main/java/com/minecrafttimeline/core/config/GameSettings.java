package com.minecrafttimeline.core.config;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;
import java.util.Objects;

/**
 * Persistent configuration container for graphics, audio and gameplay preferences.
 */
public final class GameSettings {

    private static final String DEFAULT_DIFFICULTY = "Normal";
    private static final float DEFAULT_ANIMATION_SPEED = 1.0f;
    private static final float MIN_ANIMATION_SPEED = 0.5f;
    private static final float MAX_ANIMATION_SPEED = 2.0f;
    private static final String SETTINGS_FILE = "config.json";

    private int resolutionWidth = 1920;
    private int resolutionHeight = 1080;
    private boolean fullscreen = true;
    private boolean vsync = true;
    private int masterVolume = 100;
    private int musicVolume = 80;
    private int sfxVolume = 80;
    private boolean muteAll;
    private String difficulty = DEFAULT_DIFFICULTY;
    private float animationSpeed = DEFAULT_ANIMATION_SPEED;
    private boolean showFps;
    private boolean debugRender;

    /**
     * Loads configuration from disk, falling back to defaults when unavailable.
     *
     * @return settings instance
     */
    public static GameSettings load() {
        final FileHandle fileHandle = Gdx.files.local(SETTINGS_FILE);
        if (!fileHandle.exists()) {
            return new GameSettings();
        }
        try {
            final Json json = new Json();
            return json.fromJson(GameSettings.class, fileHandle);
        } catch (final Exception exception) {
            Gdx.app.error("GameSettings", "Unable to load config.json, using defaults", exception);
            return new GameSettings();
        }
    }

    /**
     * Persists the current configuration to disk.
     */
    public void save() {
        final Json json = new Json();
        json.setOutputType(JsonWriter.OutputType.json);
        final FileHandle fileHandle = Gdx.files.local(SETTINGS_FILE);
        json.toJson(this, fileHandle);
    }

    /**
     * Restores all configuration values to defaults.
     */
    public void resetToDefaults() {
        resolutionWidth = 1920;
        resolutionHeight = 1080;
        fullscreen = true;
        vsync = true;
        masterVolume = 100;
        musicVolume = 80;
        sfxVolume = 80;
        muteAll = false;
        difficulty = DEFAULT_DIFFICULTY;
        animationSpeed = DEFAULT_ANIMATION_SPEED;
        showFps = false;
        debugRender = false;
    }

    /**
     * Applies a new window resolution.
     *
     * @param width  pixel width; must be positive
     * @param height pixel height; must be positive
     */
    public void setResolution(final int width, final int height) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Resolution must be positive");
        }
        resolutionWidth = width;
        resolutionHeight = height;
    }

    public int getResolutionWidth() {
        return resolutionWidth;
    }

    public int getResolutionHeight() {
        return resolutionHeight;
    }

    public boolean isFullscreen() {
        return fullscreen;
    }

    public void setFullscreen(final boolean fullscreen) {
        this.fullscreen = fullscreen;
    }

    public boolean isVsync() {
        return vsync;
    }

    public void setVsync(final boolean vsync) {
        this.vsync = vsync;
    }

    public int getMasterVolume() {
        return masterVolume;
    }

    public void setMasterVolume(final int masterVolume) {
        this.masterVolume = clampVolume(masterVolume);
    }

    public int getMusicVolume() {
        return musicVolume;
    }

    public void setMusicVolume(final int musicVolume) {
        this.musicVolume = clampVolume(musicVolume);
    }

    public int getSfxVolume() {
        return sfxVolume;
    }

    public void setSfxVolume(final int sfxVolume) {
        this.sfxVolume = clampVolume(sfxVolume);
    }

    public boolean isMuteAll() {
        return muteAll;
    }

    public void setMuteAll(final boolean muteAll) {
        this.muteAll = muteAll;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(final String difficulty) {
        this.difficulty = Objects.requireNonNullElse(difficulty, DEFAULT_DIFFICULTY);
    }

    public float getAnimationSpeed() {
        return animationSpeed;
    }

    public void setAnimationSpeed(final float animationSpeed) {
        if (animationSpeed < MIN_ANIMATION_SPEED || animationSpeed > MAX_ANIMATION_SPEED) {
            throw new IllegalArgumentException("animationSpeed out of range");
        }
        this.animationSpeed = animationSpeed;
    }

    public boolean isShowFps() {
        return showFps;
    }

    public void setShowFps(final boolean showFps) {
        this.showFps = showFps;
    }

    public boolean isDebugRender() {
        return debugRender;
    }

    public void setDebugRender(final boolean debugRender) {
        this.debugRender = debugRender;
    }

    private int clampVolume(final int value) {
        if (value < 0) {
            return 0;
        }
        if (value > 100) {
            return 100;
        }
        return value;
    }
}
