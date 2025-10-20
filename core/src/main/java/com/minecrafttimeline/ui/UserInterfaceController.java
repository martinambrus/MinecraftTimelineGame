package com.minecrafttimeline.ui;

/**
 * Placeholder controller for the user interface layer.
 */
public class UserInterfaceController {

    private boolean visible;

    /**
     * Displays the UI layer.
     */
    public void show() {
        visible = true;
    }

    /**
     * Hides the UI layer.
     */
    public void hide() {
        visible = false;
    }

    /**
     * Indicates whether the UI is visible.
     *
     * @return {@code true} when the UI is visible; {@code false} otherwise
     */
    public boolean isVisible() {
        return visible;
    }
}
