package de.in.dicom.tools;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.util.prefs.Preferences;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Utility class for managing application settings via Java Preferences.
 *
 * @author TiJaWo68 in cooperation with Gemini 3 Flash using Antigravity
 */
public class Settings {

    private static final Logger LOGGER = LogManager.getLogger(Settings.class);
    private static final Preferences PREFS = Preferences.userNodeForPackage(DicomEditor.class);

    private static final String KEY_WINDOW_X = "window.x";
    private static final String KEY_WINDOW_Y = "window.y";
    private static final String KEY_WINDOW_WIDTH = "window.width";
    private static final String KEY_WINDOW_HEIGHT = "window.height";
    private static final String KEY_FONT_SIZE = "table.font.size";
    private static final String KEY_LAST_PATH = "filechooser.lastpath";

    private static final int DEFAULT_FONT_SIZE = 12;
    private static final int MIN_FONT_SIZE = 6;
    private static final int MAX_FONT_SIZE = 48;

    public static Rectangle getWindowBounds() {
        int x = PREFS.getInt(KEY_WINDOW_X, -1);
        int y = PREFS.getInt(KEY_WINDOW_Y, -1);
        int width = PREFS.getInt(KEY_WINDOW_WIDTH, 800);
        int height = PREFS.getInt(KEY_WINDOW_HEIGHT, 1024);

        if (x == -1 || y == -1) {
            return null;
        }

        Rectangle bounds = new Rectangle(x, y, width, height);
        if (isValidBounds(bounds)) {
            return bounds;
        }
        LOGGER.info("Saved window bounds are invalid or off-screen, discarding.");
        return null;
    }

    public static void setWindowBounds(Rectangle bounds) {
        if (bounds == null)
            return;
        PREFS.putInt(KEY_WINDOW_X, bounds.x);
        PREFS.putInt(KEY_WINDOW_Y, bounds.y);
        PREFS.putInt(KEY_WINDOW_WIDTH, bounds.width);
        PREFS.putInt(KEY_WINDOW_HEIGHT, bounds.height);
    }

    public static int getFontSize() {
        int size = PREFS.getInt(KEY_FONT_SIZE, DEFAULT_FONT_SIZE);
        if (size < MIN_FONT_SIZE || size > MAX_FONT_SIZE) {
            LOGGER.info("Saved font size {} is out of range, using default.", size);
            return DEFAULT_FONT_SIZE;
        }
        return size;
    }

    public static void setFontSize(int size) {
        if (size >= MIN_FONT_SIZE && size <= MAX_FONT_SIZE) {
            PREFS.putInt(KEY_FONT_SIZE, size);
        }
    }

    public static String getLastPath() {
        return PREFS.get(KEY_LAST_PATH, null);
    }

    public static void setLastPath(String path) {
        if (path != null) {
            PREFS.put(KEY_LAST_PATH, path);
        }
    }

    private static boolean isValidBounds(Rectangle bounds) {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        for (GraphicsDevice gd : ge.getScreenDevices()) {
            if (gd.getDefaultConfiguration().getBounds().intersects(bounds)) {
                return true;
            }
        }
        return false;
    }
}
