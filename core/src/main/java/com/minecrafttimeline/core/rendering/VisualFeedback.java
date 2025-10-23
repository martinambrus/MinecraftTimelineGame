package com.minecrafttimeline.core.rendering;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.minecrafttimeline.core.util.AssetLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Coordinates visual feedback effects for card interactions such as placements, invalid moves, and flips. Feedback
 * relies on {@link CardAnimation} instances driven by the shared {@link AnimationManager}.
 */
public class VisualFeedback {

    /**
     * Enumerates the various feedback styles supported by the gameplay screen.
     */
    public enum FeedbackType {
        /** Successful placement results in a green glow and scale pulse. */
        SUCCESS_PLACEMENT,
        /** Invalid placement triggers a red shake and soft fade-out. */
        INVALID_PLACEMENT,
        /** Correct date confirmation emits a sparkle overlay. */
        CORRECT_DATE,
        /** Card flip rotates the card around its center. */
        CARD_FLIP
    }

    private static final float SUCCESS_PULSE_DURATION = 0.18f;
    private static final float SUCCESS_GLOW_ALPHA = 0.45f;
    private static final float INVALID_SHAKE_DURATION = 0.45f;
    private static final float INVALID_SHAKE_AMPLITUDE = 5f;
    private static final float INVALID_FADE_REDUCTION = 0.5f;
    private static final float FLIP_DURATION = 0.35f;
    private static final float SPARKLE_DURATION = 0.4f;
    private static final float SPARKLE_ALPHA = 0.35f;

    private final AnimationManager animationManager;
    private final List<FeedbackBinding> bindings = new ArrayList<>();
    private final List<OverlayBinding> overlays = new ArrayList<>();
    private final Texture overlayTexture;

    /**
     * Creates a new feedback system bound to the supplied {@link AnimationManager}.
     *
     * @param manager animation manager orchestrating feedback tweens
     * @param assetLoader asset loader for retrieving reusable textures
     */
    public VisualFeedback(final AnimationManager manager, final AssetLoader assetLoader) {
        animationManager = Objects.requireNonNull(manager, "manager must not be null");
        final AssetLoader loader = Objects.requireNonNull(assetLoader, "assetLoader must not be null");
        overlayTexture = loader.getTexture("images/white_pixel.png");
    }

    /**
     * Requests that the specified feedback type plays for the given card.
     *
     * @param type     feedback type to display
     * @param card     card renderer affected by the feedback
     * @param position optional world position used by some feedback (may be {@code null})
     */
    public void displayFeedback(final FeedbackType type, final CardRenderer card, final Vector2 position) {
        if (type == null || card == null) {
            return;
        }
        switch (type) {
            case SUCCESS_PLACEMENT:
                createSuccessFeedback(card, position);
                break;
            case INVALID_PLACEMENT:
                createInvalidFeedback(card, position);
                break;
            case CORRECT_DATE:
                createSparkleFeedback(card, position);
                break;
            case CARD_FLIP:
                createFlipFeedback(card);
                break;
            default:
                break;
        }
    }

    private void createSuccessFeedback(final CardRenderer card, final Vector2 snapTarget) {
        final FeedbackBinding expand = createScaleBinding(card, 1f, 1.2f, SUCCESS_PULSE_DURATION, snapTarget);
        expand.animation.setType(AnimationType.PULSE);
        expand.animation.setOnComplete(() -> {
            expand.active = false;
            final FeedbackBinding shrink = createScaleBinding(card, 1.2f, 1f, SUCCESS_PULSE_DURATION, snapTarget);
            shrink.animation.setType(AnimationType.PULSE);
        });
        createOverlay(card, Color.GREEN, SUCCESS_GLOW_ALPHA, SUCCESS_PULSE_DURATION * 2f);
    }

    private void createInvalidFeedback(final CardRenderer card, final Vector2 snapTarget) {
        final Vector2 basePosition = snapTarget != null ? snapTarget : card.getPosition();
        final FeedbackBinding shakeBinding = createBinding(card, new CardAnimation(0f, 1f, INVALID_SHAKE_DURATION,
                EasingFunctions::easeOutCubic), basePosition);
        shakeBinding.animation.setType(AnimationType.SHAKE);
        shakeBinding.applier = (renderer, binding, value) -> {
            final float offset = (float) Math.sin(value * Math.PI * 6f) * INVALID_SHAKE_AMPLITUDE * (1f - value);
            renderer.setPosition(binding.baseX + offset, binding.baseY);
        };
        shakeBinding.animation.setOnComplete(() -> {
            shakeBinding.active = false;
            rendererRestorePosition(shakeBinding);
        });

        final FeedbackBinding fadeBinding = createBinding(card, new CardAnimation(0f, 1f, INVALID_SHAKE_DURATION,
                EasingFunctions::easeOutQuad));
        fadeBinding.animation.setType(AnimationType.DISAPPEAR);
        fadeBinding.applier = (renderer, binding, value) -> renderer.setOpacity(1f - (INVALID_FADE_REDUCTION * value));
        fadeBinding.animation.setOnComplete(() -> {
            fadeBinding.active = false;
            card.setOpacity(1f);
        });
        createOverlay(card, Color.RED, 0.35f, INVALID_SHAKE_DURATION);
    }

    private void createFlipFeedback(final CardRenderer card) {
        final float startRotation = card.getRotation();
        final FeedbackBinding binding = createBinding(card, new CardAnimation(startRotation, startRotation + 180f,
                FLIP_DURATION, EasingFunctions::easeInOutQuad));
        binding.animation.setType(AnimationType.FLIP);
        binding.applier = (renderer, feedbackBinding, value) -> renderer.setRotation(value);
        binding.animation.setOnComplete(() -> {
            binding.active = false;
            card.setRotation(startRotation + 180f);
        });
    }

    private void createSparkleFeedback(final CardRenderer card, final Vector2 position) {
        createOverlay(card, Color.YELLOW, SPARKLE_ALPHA, SPARKLE_DURATION);
    }

    private FeedbackBinding createScaleBinding(final CardRenderer card, final float startScale, final float endScale,
            final float duration) {
        return createScaleBinding(card, startScale, endScale, duration, null);
    }

    private FeedbackBinding createScaleBinding(final CardRenderer card, final float startScale, final float endScale,
            final float duration, final Vector2 basePositionOverride) {
        final CardAnimation animation = new CardAnimation(startScale, endScale, duration, EasingFunctions::easeOutQuad);
        final FeedbackBinding binding = createBinding(card, animation, basePositionOverride);
        binding.animation.setType(AnimationType.PULSE);
        binding.applier = (renderer, feedbackBinding, value) -> {
            final Vector2 center = feedbackBinding.lockedToBasePosition
                    ? feedbackBinding.staticCenter
                    : renderer.getCenter();
            final float centerX = center.x;
            final float centerY = center.y;
            final float newWidth = feedbackBinding.baseWidth * value;
            final float newHeight = feedbackBinding.baseHeight * value;
            final float newX = centerX - (newWidth / 2f);
            final float newY = centerY - (newHeight / 2f);
            renderer.setSize(newWidth, newHeight);
            renderer.setPosition(newX, newY);
        };
        binding.animation.setOnComplete(() -> {
            binding.active = false;
            restoreSize(binding);
        });
        return binding;
    }

    private FeedbackBinding createBinding(final CardRenderer card, final CardAnimation animation) {
        return createBinding(card, animation, null);
    }

    private FeedbackBinding createBinding(final CardRenderer card, final CardAnimation animation,
            final Vector2 basePositionOverride) {
        animationManager.addAnimation(animation);
        final FeedbackBinding binding = new FeedbackBinding(card, animation, basePositionOverride);
        bindings.add(binding);
        return binding;
    }

    private void restoreSize(final FeedbackBinding binding) {
        final Vector2 currentCenter = binding.lockedToBasePosition
                ? null
                : binding.card.getCenter().cpy();
        binding.card.setSize(binding.baseWidth, binding.baseHeight);
        if (binding.lockedToBasePosition) {
            binding.card.setPosition(binding.baseX, binding.baseY);
        } else if (currentCenter != null) {
            final float newX = currentCenter.x - (binding.baseWidth / 2f);
            final float newY = currentCenter.y - (binding.baseHeight / 2f);
            binding.card.setPosition(newX, newY);
        }
    }

    private void rendererRestorePosition(final FeedbackBinding binding) {
        binding.card.setPosition(binding.baseX, binding.baseY);
    }

    private void createOverlay(final CardRenderer card, final Color baseColor, final float maxAlpha,
            final float duration) {
        final CardAnimation animation = new CardAnimation(0f, 1f, duration, EasingFunctions::easeInOutQuad);
        animationManager.addAnimation(animation);
        final OverlayBinding binding = new OverlayBinding(card, animation, baseColor.cpy(), maxAlpha);
        overlays.add(binding);
    }

    /**
     * Applies feedback transformations by polling the underlying animations. Must be called once per frame after the
     * {@link AnimationManager} has been updated.
     */
    public void update() {
        for (int i = 0; i < bindings.size(); ) {
            final FeedbackBinding binding = bindings.get(i);
            if (binding.active && !binding.animation.isDone()) {
                final float value = binding.animation.getCurrentValue();
                if (binding.applier != null) {
                    binding.applier.apply(binding.card, binding, value);
                }
                i++;
            } else {
                final int lastIndex = bindings.size() - 1;
                bindings.set(i, bindings.get(lastIndex));
                bindings.remove(lastIndex);
            }
        }

        for (int i = 0; i < overlays.size(); ) {
            final OverlayBinding overlay = overlays.get(i);
            if (!overlay.animation.isDone()) {
                final float progress = overlay.animation.getProgress();
                overlay.alpha = progress < 0.5f
                        ? (progress / 0.5f) * overlay.maxAlpha
                        : ((1f - progress) / 0.5f) * overlay.maxAlpha;
                i++;
            } else {
                overlay.alpha = 0f;
                final int lastIndex = overlays.size() - 1;
                overlays.set(i, overlays.get(lastIndex));
                overlays.remove(lastIndex);
            }
        }
    }

    /**
     * Renders transient overlay effects on top of the cards.
     *
     * @param batch shared sprite batch used by the gameplay screen
     */
    public void render(final SpriteBatch batch) {
        if (batch == null) {
            return;
        }
        final Color originalColor = batch.getColor();
        for (int i = 0; i < overlays.size(); i++) {
            final OverlayBinding overlay = overlays.get(i);
            if (overlay.alpha <= 0f) {
                continue;
            }
            final float x = overlay.card.getBounds().x;
            final float y = overlay.card.getBounds().y;
            final float width = overlay.card.getBounds().width;
            final float height = overlay.card.getBounds().height;
            batch.setColor(overlay.color.r, overlay.color.g, overlay.color.b, overlay.alpha);
            batch.draw(overlayTexture, x - 4f, y - 4f, width + 8f, height + 8f);
        }
        batch.setColor(originalColor);
    }

    private static final class FeedbackBinding {

        final CardRenderer card;
        final CardAnimation animation;
        final float baseX;
        final float baseY;
        final float baseWidth;
        final float baseHeight;
        final boolean lockedToBasePosition;
        final Vector2 staticCenter = new Vector2();
        FeedbackApplier applier;
        boolean active = true;

        FeedbackBinding(final CardRenderer card, final CardAnimation animation, final Vector2 basePositionOverride) {
            this.card = card;
            this.animation = animation;
            if (basePositionOverride != null) {
                baseX = basePositionOverride.x;
                baseY = basePositionOverride.y;
                lockedToBasePosition = true;
            } else {
                baseX = card.getPosition().x;
                baseY = card.getPosition().y;
                lockedToBasePosition = false;
            }
            baseWidth = card.getSize().x;
            baseHeight = card.getSize().y;
            if (lockedToBasePosition) {
                staticCenter.set(baseX + (baseWidth / 2f), baseY + (baseHeight / 2f));
            }
        }
    }

    @FunctionalInterface
    private interface FeedbackApplier {
        void apply(CardRenderer renderer, FeedbackBinding binding, float value);
    }

    private static final class OverlayBinding {

        final CardRenderer card;
        final CardAnimation animation;
        final Color color;
        final float maxAlpha;
        float alpha;

        OverlayBinding(final CardRenderer card, final CardAnimation animation, final Color color, final float maxAlpha) {
            this.card = card;
            this.animation = animation;
            this.color = color;
            this.maxAlpha = maxAlpha;
        }
    }
}
