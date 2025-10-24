package com.minecrafttimeline.screens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.minecrafttimeline.core.card.CardManager;
import com.minecrafttimeline.core.config.GameSettings;
import com.minecrafttimeline.core.game.GameSession;
import com.minecrafttimeline.core.game.GameState;
import com.minecrafttimeline.core.game.TurnManager;
import com.minecrafttimeline.core.rendering.ViewportConfig;
import com.minecrafttimeline.models.Player;
import java.util.List;
import java.util.Objects;

/**
 * Heads-up display drawn over gameplay providing scores, turn status and progress information.
 */
public final class HUD {

    private static final float PADDING = 20f;
    private static final float SCORE_PANEL_WIDTH = 360f;
    private static final float SCORE_PANEL_HEIGHT = 140f;
    private static final float TURN_PANEL_HEIGHT = 60f;
    private static final float PROGRESS_HEIGHT = 14f;

    private static final Color PANEL_COLOR = new Color(0f, 0f, 0f, 0.45f);
    private static final Color PROGRESS_BG = new Color(0.1f, 0.1f, 0.1f, 0.75f);
    private static final Color PROGRESS_FILL = new Color(0.3f, 0.8f, 0.3f, 0.9f);

    private static Texture pixelTexture;

    private final BitmapFont font = new BitmapFont();
    private final BitmapFont smallFont = new BitmapFont();
    private final GlyphLayout layout = new GlyphLayout();
    private final GameSettings settings;

    /**
     * Creates a new HUD bound to the provided settings.
     *
     * @param settings settings reference controlling visibility; must not be {@code null}
     */
    public HUD(final GameSettings settings) {
        this.settings = Objects.requireNonNull(settings, "settings must not be null");
        font.setColor(Color.WHITE);
        smallFont.setColor(Color.LIGHT_GRAY);
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

    /**
     * Renders the HUD elements onto the supplied sprite batch.
     *
     * @param batch       sprite batch; must not be {@code null}
     * @param gameSession session containing state; must not be {@code null}
     */
    public void render(final SpriteBatch batch, final GameSession gameSession) {
        Objects.requireNonNull(batch, "batch must not be null");
        Objects.requireNonNull(gameSession, "gameSession must not be null");
        final Texture pixel = obtainPixel();

        final GameState state = gameSession.getGameState();
        final TurnManager turnManager = gameSession.getTurnManager();
        final List<Player> players = state.getPlayers();

        final float screenWidth = ViewportConfig.BASE_WIDTH;
        final float screenHeight = ViewportConfig.BASE_HEIGHT;
        final float scorePanelX = PADDING;
        final float scorePanelY = screenHeight - PADDING - SCORE_PANEL_HEIGHT;

        batch.setColor(PANEL_COLOR);
        batch.draw(pixel, scorePanelX, scorePanelY, SCORE_PANEL_WIDTH, SCORE_PANEL_HEIGHT);

        float textY = scorePanelY + SCORE_PANEL_HEIGHT - 20f;
        for (final Player player : players) {
            font.draw(batch, player.getName() + ": " + player.getScore(), scorePanelX + 14f, textY);
            textY -= 28f;
        }

        final float cardsInHand = state.getHand().size();
        final float handPanelWidth = 220f;
        final float handPanelX = screenWidth - PADDING - handPanelWidth;
        final float handPanelY = screenHeight - PADDING - TURN_PANEL_HEIGHT;
        batch.setColor(PANEL_COLOR);
        batch.draw(pixel, handPanelX, handPanelY, handPanelWidth, TURN_PANEL_HEIGHT);
        font.draw(batch, "Hand: " + (int) cardsInHand + " cards", handPanelX + 16f, handPanelY + TURN_PANEL_HEIGHT - 20f);

        final String turnText = turnManager != null
                ? "Current Turn: " + turnManager.getCurrentPlayer().getName()
                : "Current Turn: --";
        layout.setText(font, turnText);
        final float turnX = (screenWidth - layout.width) / 2f;
        final float turnY = screenHeight - PADDING - 20f;
        font.draw(batch, layout, turnX, turnY);

        final int totalCards = CardManager.getInstance().getTotalCardCount();
        final int placedCards = state.getTimeline().size();
        final float progress = totalCards == 0 ? 0f : Math.min(1f, (float) placedCards / totalCards);
        final float progressWidth = screenWidth - (2f * PADDING);
        final float progressX = PADDING;
        final float progressY = PADDING;
        batch.setColor(PROGRESS_BG);
        batch.draw(pixel, progressX, progressY, progressWidth, PROGRESS_HEIGHT);
        batch.setColor(PROGRESS_FILL);
        batch.draw(pixel, progressX, progressY, progressWidth * progress, PROGRESS_HEIGHT);
        batch.setColor(Color.WHITE);

        smallFont.draw(batch,
                "Progress: " + placedCards + " / " + totalCards,
                progressX,
                progressY + PROGRESS_HEIGHT + 16f);
    }

    /**
     * Releases HUD-specific resources.
     */
    public void dispose() {
        font.dispose();
        smallFont.dispose();
        if (pixelTexture != null) {
            pixelTexture.dispose();
            pixelTexture = null;
        }
    }
}
