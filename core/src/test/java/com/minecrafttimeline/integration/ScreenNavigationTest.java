package com.minecrafttimeline.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.minecrafttimeline.MinecraftTimelineGame;
import com.minecrafttimeline.core.testing.GdxNativeTestUtils;
import com.minecrafttimeline.screens.GameplayScreen;
import com.minecrafttimeline.screens.MainMenuScreen;
import com.minecrafttimeline.screens.ResultsScreen;
import com.minecrafttimeline.screens.ScreenManager;
import com.minecrafttimeline.screens.SettingsScreen;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Integration tests verifying screen transitions using {@link ScreenManager}.
 */
class ScreenNavigationTest {

    private MinecraftTimelineGame game;

    @BeforeAll
    static void bootHeadless() {
        GdxNativeTestUtils.ensureHeadlessApplication();
    }

    @BeforeEach
    void setUp() {
        game = new MinecraftTimelineGame();
        game.create();
    }

    @AfterEach
    void tearDown() {
        if (game != null) {
            game.dispose();
        }
    }

    @Test
    void shouldNavigateFromMainMenuToSettingsAndBack() {
        final ScreenManager manager = game.getScreenManager();
        manager.switchTo(SettingsScreen.class);
        assertThat(currentScreen()).isInstanceOf(SettingsScreen.class);
        manager.switchTo(MainMenuScreen.class);
        assertThat(currentScreen()).isInstanceOf(MainMenuScreen.class);
    }

    @Test
    void shouldNavigateFromMainMenuToGameplay() {
        final ScreenManager manager = game.getScreenManager();
        manager.startSinglePlayerGame();
        assertThat(currentScreen()).isInstanceOf(GameplayScreen.class);
    }

    @Test
    void shouldNavigateFromGameplayToResultsAndBack() {
        final ScreenManager manager = game.getScreenManager();
        manager.startSinglePlayerGame();
        assertThat(currentScreen()).isInstanceOf(GameplayScreen.class);
        assertThat(manager.getCurrentSession()).isNotNull();
        manager.showResults(manager.getCurrentSession());
        assertThat(currentScreen()).isInstanceOf(ResultsScreen.class);
        manager.showMainMenu();
        assertThat(currentScreen()).isInstanceOf(MainMenuScreen.class);
    }

    private com.badlogic.gdx.Screen currentScreen() {
        return game.getScreen();
    }
}
