package com.braveryalive.config;

/**
 * Constants used throughout the application for UI and configuration.
 */
public class Constants {
    // App versions
    public static final String APP_VERSION = "3.1.1";
    public static final String PROVEN_PATCH = "15.4.1";

    // Window dimensions
    public static final int WINDOW_WIDTH = 500;
    public static final int WINDOW_HEIGHT = 600;

    // Colors
    public static final int[] DEFAULT_BUTTON_COLOR = {255, 255, 255};
    public static final int[] HOVER_BUTTON_COLOR = {222, 241, 250};
    public static final int[] PRESSED_BUTTON_COLOR = {163, 219, 245};
    public static final int[] STROKE_COLOR_DEFAULT = {180, 180, 180};
    public static final int[] STROKE_COLOR_HOVER = {0, 125, 255};
    public static final int[] STROKE_COLOR_PRESSED = {0, 88, 180};

    // Positions
    public static final float BUTTON_Y = 550; // height - 50
    public static final float ROLL_BUTTON_X = WINDOW_WIDTH / 4f;
    public static final float COPY_BUTTON_X = (3 * WINDOW_WIDTH) / 4f;
    public static final float EXPORT_BUTTON_X = WINDOW_WIDTH / 2f;

    // Sizes
    public static final float BUTTON_SIZE = 50;

    // Text positions
    public static final float CATEGORY_TITLE_X = 30;
    public static final float CATEGORY_X = WINDOW_WIDTH / 2f;
    public static final float ITEMS_X = WINDOW_WIDTH / 3.5f;

    // Background rect positions
    public static final float RECT_X = 20;
    public static final float RECT_WIDTH = WINDOW_WIDTH - 40;
    public static final float RECT_HEIGHT_1 = 100;
    public static final float RECT_HEIGHT_2 = 300;
}
