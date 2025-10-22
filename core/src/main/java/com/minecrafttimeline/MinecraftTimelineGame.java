package com.minecrafttimeline;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.minecrafttimeline.core.card.Card;
import com.minecrafttimeline.core.card.CardManager;
import com.minecrafttimeline.core.util.AssetLoader;
import com.minecrafttimeline.core.util.Logger;
import com.minecrafttimeline.screens.GameplayScreen;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Main libGDX game entry point for the Minecraft Timeline card game.
 */
public class MinecraftTimelineGame extends Game {

    private static final int FPS_LOG_INTERVAL = 60;
    private int frameCounter;

    /** {@inheritDoc} */
    @Override
    public void create() {
        Logger.info("Game started");
        AssetLoader.getInstance().initialize();
        final String triviaPath = Gdx.files.internal("data/trivia.json").file().getAbsolutePath();
        final CardManager cardManager = CardManager.getInstance();
        cardManager.initialize(triviaPath);

        final List<Card> sortedCards = new ArrayList<>(cardManager.getAllCards());
        sortedCards.sort(Comparator.comparing(Card::getDate));
        final int timelineCount = Math.min(6, sortedCards.size());
        final int handCount = Math.min(5, Math.max(0, sortedCards.size() - timelineCount));
        final List<Card> timelineCards = new ArrayList<>(sortedCards.subList(0, timelineCount));
        final List<Card> handCards = new ArrayList<>(sortedCards.subList(timelineCount, timelineCount + handCount));

        setScreen(new GameplayScreen(handCards, timelineCards));
    }

    /** {@inheritDoc} */
    @Override
    public void resize(final int width, final int height) {
        super.resize(width, height);
    }

    /** {@inheritDoc} */
    @Override
    public void render() {
        frameCounter++;
        if (frameCounter >= FPS_LOG_INTERVAL) {
            frameCounter = 0;
            Logger.info("FPS: " + Gdx.graphics.getFramesPerSecond());
        }
        super.render();
    }

    /** {@inheritDoc} */
    @Override
    public void dispose() {
        super.dispose();
        AssetLoader.getInstance().dispose();
    }
}
